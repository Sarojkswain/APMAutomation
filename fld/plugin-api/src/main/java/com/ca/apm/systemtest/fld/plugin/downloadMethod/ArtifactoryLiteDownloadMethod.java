/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.downloadMethod;

import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;

/**
 * @author keyja01
 *
 */
public interface ArtifactoryLiteDownloadMethod extends DownloadMethod {

    public static final String DEFAULT_ARTIFACTORY_URL = "http://artifactory-emea-cz.ca.com:8081/artifactory/repo";

    public static final String KEY_REPO_BASE = "repo_base";
    
    public ArtifactFetchResult fetchTempArtifact(String url, String groupId, String artifactId, String version, String classifier, String type);
}
