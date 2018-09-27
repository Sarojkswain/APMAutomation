/**
 *
 */
package com.ca.apm.systemtest.fld.plugin.downloadMethod;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactManagerException;

/**
 * @author keyja01
 */
@Component("httpURLDownloader")
public class HttpURLDownloaderImpl implements HttpURLDownloader {
    protected static final Logger log = LoggerFactory.getLogger(HttpURLDownloaderImpl.class);

    @Value("${fld.file.download.cache.url:}")
    protected String fileDownloadCacheUrl;

    @Override
    public ArtifactFetchResult download(String artifactURL, File destinationDirectory,
        boolean useCache) throws ArtifactManagerException {

        if (artifactURL == null) {
            final String msg = "Missing artifact specification";
            log.error(msg);
            throw new ArtifactManagerException(msg);
        }

        try {
            String downloadUrl = artifactURL;
            if (useCache && this.fileDownloadCacheUrl != null
                && this.fileDownloadCacheUrl.trim().length() > 0) {
                downloadUrl = fileDownloadCacheUrl + "?url=" + URLEncoder
                    .encode(artifactURL, "utf-8");
                log.info("Downloading from cache with URL {}", downloadUrl);
            }

            URL url = new URL(downloadUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.connect();
            InputStream in = conn.getInputStream();

            //String filename = url.getFile();
            String filename = artifactURL;
            int idx = filename.lastIndexOf("/");
            filename = filename.substring(idx + 1);
            File outputFile = new File(destinationDirectory, filename);
            try (FileOutputStream out = new FileOutputStream(outputFile)) {
                FileCopyUtils.copy(in, out);
                out.flush();
            }

            // TODO parse the build ID from the URL for truss builds and return in the result
            ArtifactFetchResult result = new ArtifactFetchResult();
            result.setFile(outputFile.getAbsoluteFile());

            return result;
        } catch (Exception e) {
            final String msg = ErrorUtils.logExceptionFmt(log, e,
                "Failed to download from {1}. Exception: {0}", artifactURL);
            if (useCache) {
                if (StringUtils.isNotBlank(fileDownloadCacheUrl)) {
                    log.warn("Trying to download {} from cache after previous failure.",
                        artifactURL);
                    return download(artifactURL, destinationDirectory, false);
                } else {
                    log.error("Cannot download {} from cache because cache URL is blank.",
                        artifactURL);
                }
            }
            throw new ArtifactManagerException(msg, e);
        }
    }

    public boolean checkIfFileExists(String fileUrl) {
        try {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con = (HttpURLConnection) new URL(fileUrl).openConnection();
            con.setRequestMethod("HEAD");
            boolean ok = (con.getResponseCode() == HttpURLConnection.HTTP_OK);
            log.info("URL exists: {}", ok);
            return ok;
        } catch (Exception e) {
            ErrorUtils.logExceptionFmt(log, e, "Error while checking URL {1}. Exception: {0}",
                fileUrl);
            return false;
        }
    }

    public String getFileDownloadCacheUrl() {
        return fileDownloadCacheUrl;
    }
}
