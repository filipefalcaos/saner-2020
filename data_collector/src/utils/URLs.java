package utils;

public class URLs {

	public static final String elasticsearch = "elastic/elasticsearch";
	public static final String spring_boot = "spring-projects/spring-boot";
	public static final String netty = "netty/netty";
	public static final String bazel = "bazelbuild/bazel";
	public static final String presto = "prestodb/presto";
	public static final String signal_android = "signalapp/Signal-Android";
	public static final String okhttp = "square/okhttp";
	public static final String elasticsearch_hadoop = "elastic/elasticsearch-hadoop";
	public static final String hikaricp = "brettwooldridge/HikariCP";
	public static final String exoplayer = "google/ExoPlayer";
	public static final String materialdrawer = "mikepenz/MaterialDrawer";
	public static final String hystrix = "Netflix/Hystrix";
	public static final String material_dialogs = "afollestad/material-dialogs";
	public static final String guava = "google/guava";
	public static final String glide = "bumptech/glide";
	public static final String fresco = "facebook/fresco";
	public static final String rxjava = "ReactiveX/RxJava";

	public static String getUrl(String project) {

		project = project.replace("-", "_");
		project = project.toLowerCase();

		System.out.println(project);
		
		switch (project) {
		case "elasticsearch":
			return elasticsearch;
		case "spring_boot":
			return spring_boot;
		case "netty":
			return netty;
		case "bazel":
			return bazel;
		case "presto":
			return presto;
		case "signal_android":
			return signal_android;
		case "okhttp":
			return okhttp;
		case "elasticsearch_hadoop":
			return elasticsearch_hadoop;
		case "hikaricp":
			return hikaricp;
		case "exoplayer":
			return exoplayer;
		case "materialdrawer":
			return materialdrawer;
		case "hystrix":
			return hystrix;
		case "material_dialogs":
			return material_dialogs;
		case "guava":
			return guava;
		case "glide":
			return glide;
		case "fresco":
			return fresco;
		case "rxjava":
			return rxjava;
		}

		return "";
	}

}
