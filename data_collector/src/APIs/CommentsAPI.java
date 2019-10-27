package APIs;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonValue;

import utils.Tokens;
import utils.IO;
import utils.JSONManager;
import utils.LocalPaths;
import utils.Util;

public class CommentsAPI {

	public static void downloadGroupOfCommitComments(String project, String url) {

		System.out.println("Downloading Group of Commit Comments");

		String path = Util.getCommitCommentsFolder(project) + "general/";

		for (int i = 1; i < 10000; i++) {

			String command = LocalPaths.CURL + " -i -u " + Tokens.USERNAME + ":" + Tokens.PASSWORD
					+ " \"https://api.github.com/repos/" + url + "/comments?page=" + i + "\"";

			boolean empty = JSONManager.getJSON(path + "comments_" + i + ".json", command, false);

			if (empty) {
				break;
			}

		}

	}

	public static void downloadIndividualCommitComments(String project, String url) {

		generateCommentsIds(project);
		
		System.out.println("Download Individual Commit Comments");

		String path = Util.getIndividualCommitCommentsFolder(project);
		List<String> ids = IO.readAnyFile(Util.getCommitCommentsFolder(project) + "comments_ids.txt");

		for (String id : ids) {

			String command = LocalPaths.CURL + " -i -u " + Tokens.USERNAME + ":" + Tokens.PASSWORD
					+ " \"https://api.github.com/repos/" + url + "/comments/" + id + "\"";

			JSONManager.getJSON(path + id + ".json", command, false);

		}

	}
	
	private static void generateCommentsIds(String project) {

		String path = Util.getCommitCommentsFolder(project) + "general/";

		List<String> files = IO.filesOnFolder(path);

		List<String> ids = new ArrayList<>();

		for (String file : files) {
			try {
				String fileData = new String(Files.readAllBytes(Paths.get(path + file)));
				JsonArray object = Json.parse(fileData).asArray();

				for (JsonValue ob : object) {
					String id = ob.asObject().get("id").toString();
					ids.add(id);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		IO.writeAnyFile(Util.getCommitCommentsFolder(project) + "comments_ids.txt", ids);
	}

}
