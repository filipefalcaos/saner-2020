package run;

import com.beust.jcommander.JCommander;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.TextProgressMonitor;
import szz.SZZ;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
public class Run {

    private String input;
    private String output;
    private String repository;
    private String url;
    private String diffj;
    private boolean help;

    public Run(String input, String output, String repository, String url, String diffj, boolean help) {

        this.input = input;
        this.output = output;
        this.repository = repository;
        this.url = url;
        this.diffj = diffj;
        this.help = help;

    }

    /**
     * Gets the command line args and starts the szz process.
     *
     * @param jCommander a instance of JCommander to parse command line args
     */
    public void run(JCommander jCommander) throws FileNotFoundException, UnsupportedEncodingException, GitAPIException {

        if (help || this.input.equals("") || this.output.equals("") ||
                (this.repository.equals("") && this.url.equals(""))) {

            // Wrong parameters were passed to the command line interface
            // Show command line usage
            jCommander.usage();

        } else if (this.repository.equals("") && !this.url.equals("")) {

            try {

                // Clone the Git repository
                System.out.println("Cloning " + this.url + "...");
                Git git = Git.cloneRepository()
                        .setProgressMonitor(new TextProgressMonitor(new PrintWriter(System.out)))
                        .setURI(this.url)
                        .setDirectory(new File(System.getProperty("user.dir") + "/git_projects/" +
                                new File(this.input).getName()))
                        .call();

                // Set the cloned repository path
                this.repository = git.getRepository().getDirectory().getParentFile().getAbsolutePath();
            } catch (JGitInternalException e) {

                // Repository already cloned
                // Set the repository path
                System.out.println("Git repository already cloned!");
                this.repository = new File(System.getProperty("user.dir") + "/git_projects/" +
                        new File(this.input).getName()).getAbsolutePath();
            }

        }

        // Print config
        System.out.println("Git repository: " + this.repository);
        System.out.println("Bug reports: " + this.input);
        System.out.println("Bug-introducing changes: " + this.output + "\n");

        // Get files from the input directory
        File input_dir = new File(this.input);
        File[] files = input_dir.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

        // Create output dir
        File output_dir = new File(this.output);
        output_dir.mkdirs();

        // Creates the output file
        PrintWriter writer =
                new PrintWriter(output_dir + "/" + input_dir.getName() + ".csv", "UTF-8");
        writer.println("bug_id, fix_commit, report_commit, insertion_points");

        // Assert files are not null
        assert files != null;

        for (int i = 0; i < files.length; i++) {

            // Get current file
            File file = files[i];

            if (file.isFile()) {

                // Init bug-introducing changes
                ArrayList<String> bugIntroducingChanges;
                Set<String> distinctBugIntroducingChanges = new HashSet<>();

                // Create a JSON parser and parse the input file
                JsonParser parser = new JsonParser();
                JsonObject jsonObject = parser.parse(new BufferedReader(new FileReader(file))).getAsJsonObject();

                // Get the report commit and the bug id
                String bug_id = jsonObject.get("issueID").getAsString();
                String report_commit = jsonObject.get("commitReport").getAsJsonObject().
                        get("commitReport").getAsString();

                // Get fix commits
                JsonArray fixCommits = jsonObject.get("commitFix").getAsJsonArray();
                ArrayList<String> fixCommitsHashes = new ArrayList<>();

                for (JsonElement fixCommit : fixCommits) {
                    fixCommitsHashes.add(fixCommit.getAsJsonObject().get("commitFix").getAsString());
                }

                // Print stats
                System.out.println("Bug: " + bug_id + " (" + (i + 1) + "/" + files.length + ")");
                System.out.println("Fix Commits: " + fixCommits);
                System.out.println("Report Commit: " + report_commit);
                System.out.println("Repository: " + this.repository);

                // Log
                System.out.println("Running SZZ...");

                for (String fixCommitHash : fixCommitsHashes) {

                    // Start SZZ with the given config
                    SZZ szz = new SZZ(fixCommitHash, report_commit, this.repository);

                    // Get the bug-introducing changes
                    bugIntroducingChanges = szz.start();
                    distinctBugIntroducingChanges.addAll(bugIntroducingChanges);

                }

                // Get distinct bug-introducing changes
                bugIntroducingChanges = new ArrayList<>(distinctBugIntroducingChanges);
                System.out.println("Bug-introducing changes: " + bugIntroducingChanges + "\n");

                // Write the bug-introducing changes
                writer.println(bug_id + ", " +
                        "\"" + fixCommitsHashes.toString().replace("[", "").
                        replace("]", "").replace(" ", "") +
                        "\"" + ", " + report_commit + ", " + "\"" +
                        bugIntroducingChanges.toString().replace("[", "").
                                replace("]", "").replace(" ", "") + "\"");

            }

        }

        writer.close();

    }

}
