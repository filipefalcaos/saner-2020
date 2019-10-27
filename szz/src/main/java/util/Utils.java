package util;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
public class Utils {

    /**
     * Get the files changed in a given commit.
     *
     * @param commit a String that contains the Git commit hash
     * @param repository a String that contains the Git repository path
     * @param commitHead a String that contains the hash of the last commit in the Git repository
     * @return a list of Strings that contain the path of the changed files
     */
	public static List<String> getChangedFiles(String commit, String repository, String commitHead) {

		ArrayList<String> results = new ArrayList<String>();
		String command;
		String output;

		if (commit.equals(commitHead)) {
			command = "git diff-tree --no-commit-id --name-only -r " + commit + " HEAD";
		} else {
			command = "git log -m -1 --name-only --pretty=" + "format:"+ " " + commit;
		}

        try {

            Process process = Runtime.getRuntime().exec(command, null, new File(repository));
			BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));

			while ((output = input.readLine()) != null) {

				if (output.contains(".java")) {
                    results.add(output);
                }

            }

            input.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;

    }

    /**
     * Execute a given command in a given Git repository.
     *
     * @param command the String that contains the command
     * @param repository a String that contains the Git repository path
     * @return the output of the command when executed in the repository
     */
	public static String executeCommandInRepository(String command, String repository) {

		File f = new File(repository); // Git repository
		Process process = null; // Command process
		StringBuilder output = new StringBuilder(); // Command process output

		// Run the given command in the given repository
		try {
			process = Runtime.getRuntime().exec(command, null, f);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Open a stream to get the output of the process
		assert process != null;
		InputStream inputStream = process.getInputStream();

		{
			int n;

			// Read the output characters and store in a String
			try {

				while ((n = inputStream.read()) != -1) {
					output.append((char) n);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return output + "\n";

	}

    /**
     * Get the commit prior to a given commit.
     *
     * @param commit a String that contains the Git commit hash
     * @param repository a String that contains the Git repository path
     * @return a String that contains the hash of the prior Git commit
     */
	public static String getPreviousCommit(String commit, String repository) {

        // Get previous commit
        String previousCommitCommand = "git log --pretty=%P -n 1 " + commit;
        String previousCommit = Utils.executeCommandInRepository(previousCommitCommand, repository);
        return previousCommit.split(" ")[0];

    }

    /**
     * Get the authoring date of a given commit.
     *
     * @param commit a String that contains the Git commit hash
     * @param repository a String that contains the Git repository path
     * @return a LocalDateTime that contains the authoring date of the commit
     */
    public static LocalDateTime getCommitDate(String commit, String repository) {

        // Default date format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");

        // Get report commit date
        String getDateCommitCommand = "git show -s --format=%ai " + commit;
        String dateStr = Utils.executeCommandInRepository(getDateCommitCommand, repository);
        dateStr = dateStr.replace("\n", "");
        return LocalDateTime.parse(dateStr, formatter);

    }

}