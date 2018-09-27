/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.downloadMethod;

import java.io.File;
import java.util.Map;

import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactManagerException;

/**
 * @author keyja01
 *
 */
public interface TrussDownloadMethod extends DownloadMethod {
    public ArtifactFetchResult fetch(String artifactSpecification, File destinationDirectory, Map<String, Object> parameters, boolean useCache) throws ArtifactManagerException;
    public String getDownloadUrl(String noInstallerSpecification, String appServer) throws ArtifactManagerException;
}
