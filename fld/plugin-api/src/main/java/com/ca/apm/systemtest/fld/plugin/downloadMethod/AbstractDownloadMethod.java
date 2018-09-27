package com.ca.apm.systemtest.fld.plugin.downloadMethod;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactManagerException;

/**
 * Created by shadm01 on 07-Jul-15.
 */
public abstract class AbstractDownloadMethod implements DownloadMethod {
    protected static final Logger L = LoggerFactory.getLogger(AbstractDownloadMethod.class);

    protected String DEFAULT_AGENT_SPECIFICATION;

    protected String mirror = null;
    protected Map<String, String> baseMap = new HashMap<>();
    @Value("${fld.file.download.cache.url:}")
    protected String fileDownloadCacheUrl;
    private HttpURLDownloader httpURLDownloader;

    protected String findBase(String branch) {
        String base = null;
        base = baseMap.get(branch);
        if (base == null) {
            base = baseMap.get("default");
        }

        return base;
    }

    public String getFileDownloadCacheUrl() {
        return fileDownloadCacheUrl;
    }

    public void setFileDownloadCacheUrl(String cacheUrl) {
        this.fileDownloadCacheUrl = cacheUrl;
    }

    public ArtifactFetchResult fetch(String artifactSpecification, File destinationDirectory,
        boolean useCacheDownloader) throws ArtifactManagerException {
        return httpURLDownloader
            .download(artifactSpecification, destinationDirectory, useCacheDownloader);
    }

    protected String getDefaultAgentSpecification() {
        return DEFAULT_AGENT_SPECIFICATION;
    }

    protected void setDefaultAgentSpecification(String spec) {
        DEFAULT_AGENT_SPECIFICATION = spec;
    }

    @Autowired
    public void setHttpURLDownloader(HttpURLDownloader httpURLDownloader) {
        this.httpURLDownloader = httpURLDownloader;
    }


}
