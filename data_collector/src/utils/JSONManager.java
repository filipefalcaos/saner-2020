package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/*
 * Created by Caio Barbosa
 */

public class JSONManager {

	public static boolean getJSON(String path, String command, boolean replace) {

		File file = new File(path);

		if (!replace) {
			if (file.exists()) {
				return false;
			}
		}

		try {

			boolean wait = true;
			long time = 0;
			String t = "";
			String message = "";
			boolean sha = false;

			while (wait) {

				Process p = Runtime.getRuntime().exec(command);

				BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

				String a = null;
				String json = "";
				boolean read = false;

				while ((a = input.readLine()) != null) {

					if (read) {

						message += a;
						if (a.equals("")) {
							continue;
						}

						if (a.contains("https://developer.github.com/v3/#pagination")) {
							System.out.println("Pagination");
							return true;
						}

						if (a.contains("sha")) {
							sha = true;
						}

						json += a + "\n";
					}

					if (a.contains("X-GitHub-Request-Id")) {
						read = true;
					}

					if (!sha) {
						if (a.contains("400 Bad Request")) {
							System.out.println(message);
							System.out.println("400");
							return true;
						}
					}

					if (!read) {
						if (!sha) {
							if (a.contains("Status:")) {
								if (a.contains("200")) {
									wait = false;
								} else {
									if (a.contains("500")) {
										System.out.println("500");
										return true;
									}

									if (a.contains("404")) {
										System.out.println("404");
										return true;
									}

									System.out.println(a);

									wait = true;
								}
							}
						}

						if (a.contains("X-RateLimit-Remaining:")) {
							if (wait) {
								String r = a.replace("X-RateLimit-Remaining: ", "");
								long remaining = Long.valueOf(r);

								System.out.println(remaining);
								if (remaining > 5) {
									wait = false;
								}
							}
						}

						if (a.contains("X-RateLimit-Reset:")) {
							t = a.replace("X-RateLimit-Reset: ", "");
							time = Long.valueOf(t + "000");

							if (wait) {
								break;
							}
						}
					}

				}

				if (wait) {

					Calendar calendar = Calendar.getInstance();

					if (t.equals("")) {
						return false;
					}

					calendar.setTimeInMillis(Long.valueOf(t) * 1000);

					SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss dd MM yyyy");
					simpleDateFormat.setTimeZone(calendar.getTimeZone());

					System.out.println("Cooldown until: " + simpleDateFormat.format(calendar.getTime()));
					long current = System.currentTimeMillis();

					calendar.setTimeInMillis(current);

					simpleDateFormat = new SimpleDateFormat("HH:mm:ss dd MM yyyy");
					simpleDateFormat.setTimeZone(calendar.getTimeZone());

					System.out.println("Now: " + simpleDateFormat.format(calendar.getTime()));

					while (current <= time) {
						current = System.currentTimeMillis();
					}

					System.out.println("Restarting...");
					wait = false;
				} else {

					if (json.length() < 10) {
						return true;
					}

					String filePath = path.substring(0, path.lastIndexOf("/"));
					File f = new File(filePath);
					if (!f.exists()) {
						f.mkdirs();
					}

					IO.writeAnyString(path, json);
					System.out.println(path);
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;

	}

}
