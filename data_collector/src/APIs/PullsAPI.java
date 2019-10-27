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

public class PullsAPI {

	public static void downloadIndividualPulls(String project, String url) {

		generatePullsIds(project);
		
		String pathIndividual = Util.getIndividualPullsFolder(project);
		String path = Util.getPullsFolder(project);
		List<String> ids = IO.readAnyFile(path + "pulls_ids.txt");
		List<String> failedIds = new ArrayList<>();
		List<String> sucessfullIds = new ArrayList<>();

		try {

			for (String line : ids) {
				String[] l = line.split(",");
				String id = "";

				if (l.length == 1) {
					id = l[0];
				} else {
					id = l[1];
				}

				String command = LocalPaths.CURL + " -i -u " + Tokens.USERNAME + ":" + Tokens.PASSWORD
						+ " \"https://api.github.com/repos/" + url + "/pulls/" + id + "\"";

				boolean f = JSONManager.getJSON(pathIndividual + id + ".json", command, false);

				if (f) {
					failedIds.add(id);
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}

		System.out.println("Failed ids: " + failedIds.toString());
	}

	public static void downloadCommentsInReviews(String project, String url) {
		
		System.out.println("Download Comments in Reviews");

		String pathPullsComments = Util.getCommentsPullsFolder(project);
		List<String> pullsIds = IO.readAnyFile(Util.getPullsFolder(project) + "pulls_ids.txt");

		for (String l : pullsIds) {

			String[] line = l.split(",");
			String id = line[1];
			
			String subPath = pathPullsComments + id;
			
			Util.checkDirectory(subPath);
			
			for (int i = 1; i <= 50; i++) {
				
				String command = LocalPaths.CURL + " -i -u " + Tokens.USERNAME + ":" + Tokens.PASSWORD
						+ " \"https://api.github.com/repos/" + url + "/pulls/" + id + "/comments?page=" + i + "\"";
				

				boolean empty = JSONManager.getJSON(pathPullsComments + id + "/comments_" + i + ".json", command, true);

				if (empty) {
					break;
				}
			}

		}

	}

	public static void generatePullsCalls(String project, String url) {
	
		System.out.println("Generating Pulls Calls");
	
		String path = Util.getGeneralPullsFolder(project);
	
		for (int i = 1; i < 2000; i++) {
	
			String command = LocalPaths.CURL + " -i -u " + Tokens.USERNAME + ":" + Tokens.PASSWORD
					+ " \"https://api.github.com/repos/" + url + "/pulls" + "?state=all&page=" + i + "\"";
	
			boolean empty = JSONManager.getJSON(path + i + ".json", command, false);
	
			if (empty) {
				break;
			}
		}
	
	}
	
	private static void generatePullsIds(String project) {

		String f = "";
		try {

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String path = Util.getIndividualPullsFolder(project);
			List<String> files = IO.filesOnFolder(path);
			List<String> pulls = new ArrayList<>();
			List<String> pullsInfo = new ArrayList<>();
			List<String> hashs = new ArrayList<>();

			for (String file : files) {

				if (!file.contains("json")) {
					continue;
				}

				f = file;
				String fileData = new String(Files.readAllBytes(Paths.get(path + file)));
				LinkedTreeMap pull = gson.fromJson(fileData, LinkedTreeMap.class);

				String login = "";
				String hash = "";

				if (pull.containsKey("user")) {

					LinkedTreeMap user = (LinkedTreeMap) pull.get("user");
					if (user == null) {
						continue;
					}

					login = (String) user.get("login");

				} else {
					continue;
				}

				boolean merged = false;

				if (pull.containsKey("merged")) {

					merged = (boolean) pull.get("merged");

					if (pull.containsKey("merge_commit_sha")) {
						hash = (String) pull.get("merge_commit_sha");
						hashs.add(login + "," + hash + "," + merged);
					}
				}

				if (pull.containsKey("head")) {
					LinkedTreeMap head = (LinkedTreeMap) pull.get("head");

					if (head.containsKey("ref")) {
						String ref = (String) head.get("ref");

						if (ref.contains("gh-pages")) {
							continue;
						}
					}

				}

				String number = "";

				if (pull.containsKey("number")) {
					number = pull.get("number") + "";
					number = number.replace(".", "");
					number = number.substring(0, number.length() - 1);

					pulls.add(login + "," + number + "," + merged);
				}

				pullsInfo.add(login + "," + number + "," + hash + "," + merged);

			}

			IO.writeAnyFile(Util.getPullsFolder(project) + "pulls_ids.txt", pulls);
			IO.writeAnyFile(Util.getPullsFolder(project) + "pulls_hashs.txt", hashs);
			IO.writeAnyFile(Util.getPullsFolder(project) + "pulls_info.txt", pullsInfo);

		} catch (Exception e) {
			System.out.println(f);
			e.printStackTrace();
		}

	}


}
