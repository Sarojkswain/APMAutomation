/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.downloader;

import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactManager.RepositoryType;

/**
 * @author KEYJA01
 *
 */
public class TrussSpecification {
    private String artifactUrl;
    private String buildId;
    private String buildNumber;
    private String product;
    private String osArchitecture;
    private String codeName;
    
    /**
     * 
     */
    public TrussSpecification() {
    }

    public String getBuildId() {
        return buildId;
    }

    public void setBuildId(String buildId) {
        this.buildId = buildId;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getOsArchitecture() {
        return osArchitecture;
    }

    public void setOsArchitecture(String osArchitecture) {
        this.osArchitecture = osArchitecture;
    }

    public String getCodeName() {
        return codeName;
    }

    public void setCodeName(String codeName) {
        this.codeName = codeName;
    }

    public String getArtifactUrl() {
        return artifactUrl;
    }

    public void setArtifactUrl(String artifactUrl) {
        this.artifactUrl = artifactUrl;
    }

    public RepositoryType getArtifactRepoType() {
        return RepositoryType.TRUSS;
    }
}
