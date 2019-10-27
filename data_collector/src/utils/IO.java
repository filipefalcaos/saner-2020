package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by Caio Barbosa
 */

public class IO {

	public static List<String> readAnyFile(String filename) {

		List<String> lines = new ArrayList<String>();
		BufferedReader reader = null;
		try {

			File f = new File(filename);

			if (!f.exists()) {
				return new ArrayList<>();
			}

			reader = new BufferedReader(new FileReader(filename));

			// read file line by line
			String line = reader.readLine();

			while (line != null) {
				lines.add(line);
				line = reader.readLine();
			}

			reader.close();

		} catch (Exception e) {
			e.printStackTrace();
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();

				}
			}
		}

		return lines;
	}

	public static void writeAnyFile(String path, List<String> lines) {
		// TODO Auto-generated method stub

		String text = "";
		for (String line : lines) {
			text += line + "\n";
		}

		Writer wr = null;

		try {
			wr = new FileWriter(path);
			wr.write(text);
			wr.close();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				if (wr != null) {
					wr.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}

	}

	public static void writeAnyString(String path, String text) {
		// TODO Auto-generated method stub

		Writer wr = null;

		try {
			wr = new FileWriter(path);
			wr.write(text);
			wr.close();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				if (wr != null) {
					wr.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}

	}

	public static List<String> filesOnFolder(String path) {

		List<String> fileNames = new ArrayList<String>();

		if (path == null) {
			System.out.println("Folder name is null");
		}

		try {
			// filesNamesPath = path;
			File f = new File(path);

			File[] files = f.listFiles();

			if (files != null) {

				for (File file : files) {
					fileNames.add(file.getName());
				}

			}

		} catch (Exception e) {
			System.out.println(path);
			e.printStackTrace();
		}

		return fileNames;
	}

}
