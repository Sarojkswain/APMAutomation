/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.downloader;

import java.io.File;

/**
 * @author KEYJA01
 *
 */
public class ArtifactFetchResult {
	private File file;
	private String buildId;

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getBuildId() {
		return buildId;
	}

	public void setBuildId(String buildId) {
		this.buildId = buildId;
	}

	@Override
	public String toString() {
		return "ArtifactFetchResult{" +
			"file=" + file +
			", buildId='" + buildId + '\'' +
			'}';
	}
}
