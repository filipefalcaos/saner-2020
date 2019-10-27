package APIs;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import utils.Tokens;
import utils.IO;
import utils.JSONManager;
import utils.LocalPaths;
import utils.Util;

public class IssuesAPI {

	public static void generateRepositoryIssuesCall(String project, String url) {

		// repos/:owner/:repo/issues
		String path = Util.getGeneralIssuesPath(project);

		for (int i = 1; i < 1000; i++) {
			String command = LocalPaths.CURL + " -i -u " + Tokens.USERNAME + ":" + Tokens.PASSWORD
					+ " \"https://api.github.com/repos/" + url + "/issues?state=all&page=" + i + "\"";

			boolean empty = JSONManager.getJSON(path + i + ".json", command, false);

			if (empty) {
				break;
			}

		}

	}

	public static void generateCommentsCalls(String project, String url) {
	
		String path = Util.getIssuesCommentsPath(project);
	
		for (int i = 1; i < 10000; i++) {
	
			String command = LocalPaths.CURL + " -i -u " + Tokens.USERNAME + ":" + Tokens.PASSWORD
					+ " \"https://api.github.com/repos/" + url + "/issues/comments?page=" + i + "\"";
	
			boolean empty = JSONManager.getJSON(path + "comments_" + i + ".json", command, false);
	
			if (empty) {
				break;
			}
	
		}
	
	}

	public static void generateIndividualIssuesCall(String project, String url) {
	
		generateIssuesIds(project);
		
		String path = Util.getIssuesPath(project);
	
		List<String> ids = IO.readAnyFile(path + "issues_ids.txt");
	
		List<String> commands = new ArrayList<>();
	
		for (String id : ids) {
	
			File f = new File(Util.getIndividualIssuesFolder(project));
			if (!f.exists()) {
				f.mkdirs();
			}
	
			String command = LocalPaths.CURL + " -i -u " + Tokens.USERNAME + ":" + Tokens.PASSWORD
					+ " \"https://api.github.com/repos/" + url + "/issues/" + id + "\"";
	
			JSONManager.getJSON(path + "individual/" + id + ".json", command, false);
	
		}
	
	}
	
	private static void generateIssuesIds(String project) {

		try {

			List<String> ids = new ArrayList<>();
			String path = Util.getIssuesPath(project);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			List<String> files = IO.filesOnFolder(Util.getGeneralIssuesPath(project));

			for (String file : files) {

				if (!file.contains("json") || file.contains("ids")) {
					continue;
				}

				String fileData = new String(Files.readAllBytes(Paths.get(Util.getGeneralIssuesPath(project) + file)));
				List<LinkedTreeMap> issues = gson.fromJson(fileData, List.class);

				for (LinkedTreeMap<?, ?> c : issues) {

					if (c.containsKey("pull_request")) {
						continue;
					}

					String id = c.get("number") + "";
					id = id.replace(".", "");
					id = id.substring(0, id.length() - 1);
					ids.add(id);

				}

			}

			IO.writeAnyFile(path + "issues_ids.txt", ids);

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}



}
