package szz;

import util.LinesChanged;
import util.Utils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Copyright 2018 Filipe Falcão Batista dos Santos
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
public class SZZ {

    private String fix_commit;
    private String report_commit;
    private String repository;

    public SZZ(String fix_commit, String report_commit, String repository) {

        this.fix_commit = fix_commit;
        this.report_commit = report_commit;
        this.repository = repository;

    }

    public ArrayList<String> start() {

        // List of bug-introducing changes
        ArrayList<String> bugIntroducingChanges = new ArrayList<>();

        // Get the repository fist commit
        String firstCommitCommand = "git rev-list --max-parents=0 HEAD";
        String firstCommit = Utils.executeCommandInRepository(firstCommitCommand, this.repository).
                replace("\n", "");

        // Get the files changed in the fix commit
        List<String> changedFiles = Utils.getChangedFiles(this.fix_commit, this.repository, firstCommit);

        for (String changedFile : changedFiles) {

            // Get the lines removed in the modified file
            LinesChanged linesChangedCollector = new LinesChanged();
            Set<String> linesRemoved = linesChangedCollector.getLinesRemoved(this.fix_commit,
                    changedFile, this.repository).get(0);

            // Print stats
            // System.out.println("File: " + changedFile);
            // System.out.println("Lines Changed: " + linesRemoved);

            if (linesRemoved.size() != 0) {

                // Get previous commit
                String previousCommit = Utils.getPreviousCommit(this.fix_commit, this.repository);

                // Run the git checkout command
                String checkoutCommand = "git checkout -f " + previousCommit;
                Utils.executeCommandInRepository(checkoutCommand, repository);

                // Run the git annotate command
                String annotateCommand = "git annotate -l " + changedFile + " " + previousCommit;
                String annotateOutput = Utils.executeCommandInRepository(annotateCommand, this.repository);

                // Get lines from git annotate
                String[] annotateLines = annotateOutput.split("\n");

                for (String annotateLine : annotateLines) {

                    if (linesRemoved.contains(annotateLine.split("\t")[3].split("\\)")[0])) {

                        // Bug-introducing commit hash
                        String bugHash = annotateLine.split("\t")[0];

                        // If not already found, add bug-introducing commit
                        if (!bugIntroducingChanges.contains(bugHash)) {
                            bugIntroducingChanges.add(bugHash);
                        }

                    }

                }

            }

        }

        // Get report commit date
        LocalDateTime dateReport = Utils.getCommitDate(this.report_commit, this.repository);

        // Check if the bug-introducing changes were made before the report commit
        for (Iterator<String> i = bugIntroducingChanges.iterator(); i.hasNext(); ) {

            // Get bug-introducing commit date
            String currentBugIntroducingChange = i.next();
            LocalDateTime dateBug = Utils.getCommitDate(currentBugIntroducingChange, this.repository);

            // Check if the bug-introducing change was made before the report commit
            if (dateBug.isAfter(dateReport)) {
                i.remove();
            }

        }

        return bugIntroducingChanges;

    }

}
