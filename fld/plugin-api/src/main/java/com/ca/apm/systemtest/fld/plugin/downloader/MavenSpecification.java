/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.downloader;

import java.util.StringTokenizer;

import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactManager.RepositoryType;

/**
 * Encapsulate the maven coordinates of an artifact
 * 
 * @author KEYJA01
 *
 */
public class MavenSpecification {
	private String groupId;
	private String artifactId;
	private String version;
	private String classifier;
	private String packaging;
    private String artifactUrl;

	public MavenSpecification(String groupId, String artifactId, String version, String packaging, String classifier) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.packaging = packaging;
		this.classifier = classifier;
	}

	public MavenSpecification(String artifact) {
		StringTokenizer st = new StringTokenizer(artifact, ":");
		groupId = st.nextToken();
		artifactId = st.nextToken();
		version = st.nextToken();

		if (st.hasMoreTokens()) {
			packaging = st.nextToken();
		}
		
		if (st.hasMoreTokens()) {
			classifier = st.nextToken();
		}
	}
	

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getClassifier() {
		return classifier;
	}

	public void setClassifier(String classifier) {
		this.classifier = classifier;
	}

	public String getPackaging() {
		return packaging;
	}

	public void setPackaging(String packaging) {
		this.packaging = packaging;
	}

    public String getArtifactUrl() {
        return artifactUrl;
    }

    public void setArtifactUrl(String artifactUrl) {
        this.artifactUrl = artifactUrl;
    }

    public RepositoryType getArtifactRepoType() {
        return RepositoryType.ARTIFACTORY;
    }
}
