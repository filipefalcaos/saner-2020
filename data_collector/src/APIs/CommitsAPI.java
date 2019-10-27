package APIs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import generators.Commit;
import utils.IO;
import utils.JSONManager;
import utils.LocalPaths;
import utils.Tokens;
import utils.Util;

/*
 * Created by Caio Barbosa
 */

public class CommitsAPI {

	public static void downloadIndividualCommitsByHash(List<String> hashs, String url, String path) {

		List<String> hashsToDownload = new ArrayList<>();

		boolean flag = true;
		for (String hash : hashs) {
			File f = new File(path + hash + ".json");
			if (!f.exists()) {
				flag = false;
				hashsToDownload.add(hash);
			}
		}

		if (flag) {
			return;
		}

		for (String hash : hashs) {

			String command = LocalPaths.CURL + " -i -u " + Tokens.USERNAME + ":" + Tokens.PASSWORD
					+ " \"https://api.github.com/repos/" + url + "/commits/" + hash + "\"";
			JSONManager.getJSON(path + hash + ".json", command, true);

		}

	}

	public static void downloadAllCommits(String project, String url) {
		final String path = Util.getCommitsFolderPath(project);
		for (int j = 1; j < 1100; j++) {

			final String command = LocalPaths.CURL + " -i -u " + Tokens.USERNAME + ":" + Tokens.PASSWORD
					+ " \"https://api.github.com/repos/" + url + "/commits?page=" + j + "\"";
			boolean empty = false;

			empty = JSONManager.getJSON(path + j + ".json", command, true);

			if (empty) {
				break;
			}

		}

	}

	public static void downloadAllIndividualCommits(String project, String url) {
		Commit.collectHashsFromUsers(project);
		List<String> hashs = IO.readAnyFile(Util.getCommitsPath(project) + "all_hashs.txt");
		downloadIndividualCommitsByHash(hashs, url, Util.getIndividualCommitsPath(project));
	}

}
