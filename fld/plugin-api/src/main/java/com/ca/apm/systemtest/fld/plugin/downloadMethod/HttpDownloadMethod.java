package com.ca.apm.systemtest.fld.plugin.downloadMethod;

import java.io.File;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactManagerException;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil;

/**
 * Download method to be used when direct URLs are to be downloaded. 
 * 
 * @author shadm01
 * 
 */
@Component("HttpDownloadMethod")
public class HttpDownloadMethod extends AbstractDownloadMethod {
    @Override
    public Path downloadAgent(String trussServer, String codeName, String buildId, String buildNumber, SystemUtil.OperatingSystemFamily platform) {
        return null;
    }

    @Override
    public ArtifactFetchResult fetchResultFromDownloadSource(String noInstallerSpecification, File tmpDirectory, String appServerName) {
        return null;
    }

    /**
     * Fetches artifact from <code>downloadUrl</code>.
     * When downloading from cache (i.e. <code>useCache</code> is <code>true</code>) remember, that this implementation 
     * considers <code>downloadUrl</code> has form of <code>http://hostname/here/goes/some/path/to/file.ext</code>, i.e. to get the file name 
     * for searching in the cache it takes the last segment after the last '/' and considers it as the file name. It means URLs containing 
     * queries (e.g. http://hostname/some/path/getFile?name=file.ext) will not work properly with caching.
     * 
     * @param   downloadUrl            download URL
     * @param   destinationDirectory   folder to download to
     * @param   useCache               <code>true</code> to search in cache first, otherwise <code>false</code>
     * 
     * @return  fetched artifact result
     * @throws ArtifactManagerException  
     * 
     */
    public ArtifactFetchResult fetch(String downloadUrl, File destinationDirectory, boolean useCache) throws ArtifactManagerException {
        return super.fetch(downloadUrl, destinationDirectory, useCache);
    }
}
