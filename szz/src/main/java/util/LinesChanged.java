package util;

import java.util.*;
import java.util.regex.*;

/**
 * Copyright 2018 Eduardo Gabriel Nunes de Farias, Filipe Falcão Batista dos Santos
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class LinesChanged {

    private Set<String> getIntervalLinesChanged(String diff, String commit) {

        Set<String> intervalLines = new LinkedHashSet<>();
        String[] gitDiff = diff.split("\n");

        int intLine = 0;
        int endLine = 0;

        for (String line : gitDiff) {
            endLine++;

            if (commit.contains("^")) {

                if (line.startsWith("+"))
                    endLine--;

            } else {

                if (line.startsWith("-"))
                    endLine--;

            }

            if (line.matches("@@.*@@.*|diff --git.*")) {

                if (line.contains("@@")) {
                    if (intLine != 0) {
                        intervalLines.add((intLine + 3) + "-" + (endLine - 4));
                    }
                    if (commit.contains("^")) {

                        line = line.replaceAll("@@", "").replaceAll(" ", "").split("[+]")[0].split(",")[0].split("[-]")[1];
                        intLine = Integer.parseInt(line);
                        endLine = intLine;

                    } else {

                        line = line.replaceAll("@@", "").replaceAll(" ", "").split("[+]")[1];
                        line = line.split(",")[0];
                        intLine = Integer.parseInt(line);
                        endLine = intLine;

                    }
                } else if (line.contains("diff --git")) {

                    if (intLine != 0)
                        intervalLines.add((intLine + 3) + "-" + (endLine - 4));

                    endLine = 0;
                }
            }
        }

        if (intLine != 0) {
            intervalLines.add((intLine + 3) + "-" + (endLine - 4));// +3 and -4
        }

        return intervalLines;

    }

    public ArrayList<Set<String>> getLinesRemoved(String commit, String file, String repository) {

        int minor;
        int major;
        String mergeCommit = null;

        // Get the diff of the given commit
        String diff = Utils.executeCommandInRepository("git show " + commit + " -- " + file, repository);

        // Get the diff of the given commit if it is a merge
        if (diff.equals("\n")) {

            // Set the merge commit
            mergeCommit = commit;

            // Get previous commits
            String previousCommitsCommand = "git log --pretty=%P -n 1 " + commit;
            commit = Utils.executeCommandInRepository(previousCommitsCommand, repository).replaceAll("\n","");
            commit = commit.replace(" ", "...");

            // Get the diff of a merge commit
            diff = Utils.executeCommandInRepository("git diff " + commit + " -- " + file, repository);

        }

        List<String> changesRemoved = new ArrayList<String>();
        Set<String> changesLineRemoved = new LinkedHashSet<String>();
        ArrayList<Set<String>> temp = new ArrayList<Set<String>>();

        Pattern pattern = Pattern.compile("^(\\-)(.*)");
        String[] split = diff.split("@@ -.*?\\,[\\d]+ @.*");

        if (getIntervalLinesChanged(diff, commit + "^").size() != 0 && !diff.contains("@@@")) {

            ArrayList<String> listIntervals = new ArrayList<>(getIntervalLinesChanged(diff, (commit + "^")));

            for (int i = 0; i < listIntervals.size(); i++) {

                String[] aux = listIntervals.get(i).split("-");
                String s2 = split[i + 1];

                minor = Integer.parseInt(aux[0]);
                major = Integer.parseInt(aux[1]);

                for (String stringFinder : s2.split("\n")) {

                    if (stringFinder.startsWith("-")) {

                        Matcher matcher = pattern.matcher(stringFinder);
                        matcher.find();
                        changesRemoved.add(matcher.group(2));

                    }

                }

                for (Iterator<String> iterator = changesRemoved.iterator(); iterator.hasNext(); ) {

                    /*
                     * Let's talk about this regex. If we don't, believe me, you will never understand that
                     * crap again.
                     *
                     * (1) The first expression removes the lines that start with a *. This is necessary for
                     * the case where a commit changes only one part of a multi-line comment.
                     *
                     * (2) The second expression removes the lines starting the classical single line comment.
                     *
                     * (3) The third expression removes a hole multi-line comment. This is necessary for the
                     * case where a commit deletes a hole multi-line comment, for example.
                     */
                    String regex = "(\\*.*)|(\\/\\/.*)|(/\\*([^*]|[\\r\\n]|(\\*+([^*/]|[\\r\\n])))*\\*+/)";
                    String value = iterator.next();

                    if (value.replace("\t", "").replace(" ", "").
                            length() <= 3 || value.replaceAll(regex, "").trim().length() <= 3) {
                        iterator.remove();
                    }

                }

                String[] setFileToRemovedAnalysis;

                if (mergeCommit != null) {
                    setFileToRemovedAnalysis = Utils.executeCommandInRepository(
                            "git show "  + mergeCommit + "^:" + file, repository).split("\n");
                } else {
                    setFileToRemovedAnalysis = Utils.executeCommandInRepository(
                            "git show "  + commit + "^:" + file, repository).split("\n");
                }

                for (String aux2 : changesRemoved) {

                    if (major > setFileToRemovedAnalysis.length) {
                        major = setFileToRemovedAnalysis.length;
                    }

                    for (int inter = minor - 1; inter < major; inter++) {

                        if (minor <= 0) {
                            minor = 0;
                        }

                        if (setFileToRemovedAnalysis[inter].replace(" ", "").
                                contains((aux2.replace(" ", "")))) {
                            changesLineRemoved.add(String.valueOf(inter + 1));
                        }

                    }

                }

                temp.add(changesLineRemoved);
            }
        } else {

            if (diff.contains("@@@")) {
                changesLineRemoved.add("@@@");
            } else {
                changesLineRemoved.add("");
            }

            temp.add(changesLineRemoved);

        }

        return temp;

    }

}