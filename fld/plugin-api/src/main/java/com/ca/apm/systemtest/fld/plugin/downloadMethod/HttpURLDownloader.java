/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.downloadMethod;

import java.io.File;

import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactManagerException;

/**
 * @author keyja01
 *
 */
public interface HttpURLDownloader {
    /**
     * Downloads the specified URL into the destination directory
     * @param url
     * @param destinationDirectory
     * @param useCache
     * @return
     */
    public ArtifactFetchResult download(String url, File destinationDirectory, boolean useCache) throws ArtifactManagerException;
    public boolean checkIfFileExists(String fileUrl);
}
