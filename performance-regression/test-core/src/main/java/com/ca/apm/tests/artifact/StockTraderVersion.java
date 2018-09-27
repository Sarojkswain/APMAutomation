/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 *
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */
package com.ca.apm.tests.artifact;

import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.artifact.IThirdPartyArtifact;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * StockTrader
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public enum StockTraderVersion implements IThirdPartyArtifact {

    VER_55("WLS_StockTrader", TasExtension.ZIP, "WLS_TradeEAR.ear", "WLS_TradeBSLEAR.ear",
            "create_weblogic_datasource.py", "delete_weblogic_datasource.py", "weblogic_datasource.properties");

    private static final String GROUP_ID = IThirdPartyArtifact.GROUP_ID + ".ibm.stocktrader";
    private final DefaultArtifact artifact;

    private final String applicationEar;
    private final String applicationBslEar;
    private final String createDatasourceScript;
    private final String deleteDatasourceScript;
    private final String propertiesFile;


    /**
     * @param archiveFileName        name of the file in Artifactory
     * @param extension              file extension.
     * @param applicationEar         name of the EAR file inside the archive
     * @param applicationBslEar      name of the BSL EAR file inside the archive
     * @param createDatasourceScript name of the Script file for creating DS inside the archive
     * @param deleteDatasourceScript name of the Script file for deleting DS inside the archive
     * @param propertiesFile         name of the properties file inside the archive
     */
    StockTraderVersion(String archiveFileName, TasExtension extension, String applicationEar, String applicationBslEar,
                       String createDatasourceScript, String deleteDatasourceScript, String propertiesFile) {
        artifact = new DefaultArtifact(GROUP_ID, archiveFileName, extension.getValue(), null);
        this.applicationEar = applicationEar;
        this.applicationBslEar = applicationBslEar;
        this.createDatasourceScript = createDatasourceScript;
        this.deleteDatasourceScript = deleteDatasourceScript;
        this.propertiesFile = propertiesFile;
    }

    public String getApplicationEar() {
        return applicationEar;
    }

    public String getApplicationBslEar() {
        return applicationBslEar;
    }

    public String getCreateDatasourceScript() {
        return createDatasourceScript;
    }

    public String getDeleteDatasourceScript() {
        return deleteDatasourceScript;
    }

    public String getPropertiesFile() {
        return propertiesFile;
    }

    @Override
    public String getFilename() {
        Artifact artifact = getArtifact();
        return String.format("%s.%s", artifact.getArtifactId(), artifact.getExtension());
    }

    /**
     * Non-standard artifact without POM
     *
     * @param artifactory artifactory URL
     * @return URL of the non-standard artifact
     */
    public URL getArtifactUrl(URL artifactory) {
        String artifactUrlTmp = artifactory.toExternalForm();
        artifactUrlTmp += "/" + this.getArtifact().getGroupId().replaceAll("\\.", "/") + "/" + this.getFilename();
        try {
            return new URL(artifactUrlTmp);
        } catch (MalformedURLException var6) {
            throw new IllegalArgumentException("Artifact URL resolution failed.", var6);
        }
    }

    @Override
    public Artifact getArtifact() {
        return artifact;
    }
}