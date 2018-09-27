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
 * DB Scripts for configuring TradeDb in Oracle DB
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public enum OracleTradeDbScriptVersion implements IThirdPartyArtifact {

    VER_55("tradedb-configure-oracle-dbscript", TasExtension.ZIP, "configure_db.sql", "configure_user.sql");

    private static final String GROUP_ID = IThirdPartyArtifact.GROUP_ID + ".ibm.stocktrader";
    private final DefaultArtifact artifact;

    private final String dbSqlFileName;
    private final String userSqlFileName;


    /**
     * @param archiveFileName name of the file in Artifactory
     * @param extension       file extension
     * @param dbSqlFileName   name of the SQL file inside the archive for configuring DB parameters
     * @param userSqlFileName name of the SQL file inside the archive for creating a user and granting rights
     */
    OracleTradeDbScriptVersion(String archiveFileName, TasExtension extension, String dbSqlFileName, String userSqlFileName) {
        artifact = new DefaultArtifact(GROUP_ID, archiveFileName, extension.getValue(), null);
        this.dbSqlFileName = dbSqlFileName;
        this.userSqlFileName = userSqlFileName;
    }

    public String getDbSqlFileName() {
        return dbSqlFileName;
    }

    public String getUserSqlFileName() {
        return userSqlFileName;
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