package generators;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import utils.IO;
import utils.Util;

public class Commit {

	public static void collectHashsFromUsers(String project) {

		String path = Util.getCommitsFolderPath(project);

		List<String> files = IO.filesOnFolder(path);

		List<String> allHashs = new ArrayList<>();

		getCommitHash(path, files, allHashs);

		IO.writeAnyFile(Util.getCommitsPath(project) + "all_hashs.txt", allHashs);

	}

	private static void getCommitHash(String path, List<String> files, List<String> allHashs) {
		// TODO Auto-generated method stub
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		for (String file : files) {
			try {
				String fileData = new String(Files.readAllBytes(Paths.get(path + file)));
				List<LinkedTreeMap> commits = new ArrayList<>();
				try {
					commits = gson.fromJson(fileData, List.class);
				} catch (Exception e) {
					LinkedTreeMap c = gson.fromJson(fileData, LinkedTreeMap.class);
					commits.add(c);
				}

				for (LinkedTreeMap commit : commits) {

					String hash = "";
					String authorName = "";

					if (commit.containsKey("author")) {
						LinkedTreeMap a = (LinkedTreeMap) commit.get("author");
						if (commit.containsKey("sha")) {
							hash = (String) commit.get("sha");
						}
					}

					if (!allHashs.contains(hash)) {
						allHashs.add(hash);
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(path + file);
			}

		}

	}

}
