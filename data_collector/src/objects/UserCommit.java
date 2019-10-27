package objects;

import java.util.Date;
import java.util.List;

public class UserCommit {

	private String sha;
	private String date;
	private String authorName;
	private String authorEmail;
	private String authorLogin;
	private boolean testInclusion;
	private boolean pullCommit;
	private String natureClassification;
	private String sizeClassification;
	private double additions;
	private double deletions;
	private double linesChanged;
	private double modifiedFiles;
	private List<String> files;

	public String getSha() {
		return sha;
	}

	public void setSha(String sha) {
		this.sha = sha;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public String getAuthorEmail() {
		return authorEmail;
	}

	public void setAuthorEmail(String authorEmail) {
		this.authorEmail = authorEmail;
	}

	public String getAuthorLogin() {
		return authorLogin;
	}

	public void setAuthorLogin(String authorLogin) {
		this.authorLogin = authorLogin;
	}

	public boolean hasTestInclusion() {
		return testInclusion;
	}

	public void setTestInclusion(boolean testInclusion) {
		this.testInclusion = testInclusion;
	}

	public String getNatureClassification() {
		return natureClassification;
	}

	public void setNatureClassification(String natureClassification) {
		this.natureClassification = natureClassification;
	}

	public String getSizeClassification() {
		return sizeClassification;
	}

	public void setSizeClassification(String sizeClassification) {
		this.sizeClassification = sizeClassification;
	}

	public boolean isPullCommit() {
		return pullCommit;
	}

	public void setPullCommit(boolean pullCommit) {
		this.pullCommit = pullCommit;
	}

	public double getAdditions() {
		return additions;
	}

	public void setAdditions(double additions) {
		this.additions = additions;
	}

	public double getDeletions() {
		return deletions;
	}

	public void setDeletions(double deletions) {
		this.deletions = deletions;
	}

	public double getLinesChanged() {
		return linesChanged;
	}

	public void setLinesChanged(double linesChanged) {
		this.linesChanged = linesChanged;
	}

	public boolean isTestInclusion() {
		return testInclusion;
	}

	public double getModifiedFiles() {
		return modifiedFiles;
	}

	public void setModifiedFiles(double modifiedFiles) {
		this.modifiedFiles = modifiedFiles;
	}

	public List<String> getFiles() {
		return files;
	}

	public void setFiles(List<String> files) {
		this.files = files;
	}

	@Override
	public String toString() {
		return "UserCommit [sha=" + sha + ", date=" + date + ", authorName=" + authorName + ", authorEmail="
				+ authorEmail + ", authorLogin=" + authorLogin + ", testInclusion=" + testInclusion + ", pullCommit="
				+ pullCommit + ", natureClassification=" + natureClassification + ", sizeClassification="
				+ sizeClassification + ", additions=" + additions + ", deletions=" + deletions + ", linesChanged="
				+ linesChanged + ", modifiedFiles=" + modifiedFiles + ", files=" + files + "]";
	}

	
	
}
