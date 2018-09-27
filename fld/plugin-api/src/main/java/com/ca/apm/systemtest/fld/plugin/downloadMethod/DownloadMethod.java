package com.ca.apm.systemtest.fld.plugin.downloadMethod;

import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactManagerException;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil;

import java.io.File;
import java.nio.file.Path;

/**
 * Common interface for downloader implementations.
 * 
 * Supported download methods:
 *  - Direct HTTP (http(s)://)
 *  - Truss
 *  - Maven
 * 
 * 
 * @author shadm01
 */
public interface DownloadMethod {
    
    Path downloadAgent(String trussServer, String codeName, String buildId, String buildNumber, SystemUtil.OperatingSystemFamily platform);
    
    ArtifactFetchResult fetchResultFromDownloadSource(String noInstallerSpecification, File tmpDirectory, String appServerName);

    /**
     * Fetches an artifact using HTTP - suitable for things like .zip files with configuration,
     * library jar files, esp. where we
     * don't need to take into account the platform, etc
     *
     * @param artifactSpecification
     * @param destinationDirectory
     * @param useCacheDownloader
     * @return
     * @throws ArtifactManagerException
     */
    ArtifactFetchResult fetch(String artifactSpecification, File destinationDirectory, boolean useCacheDownloader) throws ArtifactManagerException;
//    ArtifactFetchResult fetch(String artifactSpecification, File destinationDirectory, String repositoryUrl, boolean useCache) throws ArtifactManagerException;
}
