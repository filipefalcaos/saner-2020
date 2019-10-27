package start;

import java.util.ArrayList;
import java.util.List;

import APIs.CommentsAPI;
import APIs.CommitsAPI;
import APIs.IssuesAPI;
import APIs.PullsAPI;
import utils.URLs;

public class Main {

	public static void main(String[] args) {

		List<String> projects = new ArrayList<>();

		projects.add("elasticsearch");
		projects.add("spring-boot");
		projects.add("netty");
		projects.add("bazel");
		projects.add("presto");
		projects.add("Signal-Android");
		projects.add("okhttp");
		projects.add("RxJava");
		projects.add("guava");

		projects.add("elasticsearch-hadoop");
		projects.add("HikariCP");
		projects.add("ExoPlayer");
		projects.add("MaterialDrawer");
		projects.add("Hystrix");
		projects.add("material-dialogs");

		projects.add("glide");
		projects.add("fresco");

		for (int i = 0; i < projects.size(); i++) {

			String project = projects.get(i);

			System.out.println(project);

			CommitsAPI.downloadAllCommits(project, URLs.getUrl(project));
			CommitsAPI.downloadAllIndividualCommits(project, URLs.getUrl(project));

			IssuesAPI.generateRepositoryIssuesCall(project, URLs.getUrl(project));
			IssuesAPI.generateIndividualIssuesCall(project, URLs.getUrl(project));

			IssuesAPI.generateCommentsCalls(project, URLs.getUrl(project));
			CommentsAPI.downloadGroupOfCommitComments(project, URLs.getUrl(project));
			CommentsAPI.downloadIndividualCommitComments(project, URLs.getUrl(project));

			PullsAPI.generatePullsCalls(project, URLs.getUrl(project));
			PullsAPI.downloadIndividualPulls(project, URLs.getUrl(project));
			PullsAPI.downloadCommentsInReviews(project, URLs.getUrl(project));

		}

	}

}
