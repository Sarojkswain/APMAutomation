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
 * .NETStockTrader DB Script for configuring DB and creating necessary tables in MSSQL DB
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public enum MsSqlTradeDbScriptVersion implements IThirdPartyArtifact {

    VER_55("stocktrader55_mssql_db_scripts", TasExtension.ZIP, "stocktrader_mssql_create", "create_databases_and_users.bat", "recreate_tables.bat");

    private static final String GROUP_ID = IThirdPartyArtifact.GROUP_ID + ".ibm.stocktrader";
    private final DefaultArtifact artifact;

    private final String unpackDir;
    private final String confFileName;
    private final String tablesFileName;


    /**
     * @param archiveFileName name of the file in Artifactory
     * @param extension       file extension
     * @param unpackDir       directory that contains all the files inside the archive
     * @param confFileName    name of the BAT file inside the archive for configuring DB parameters
     * @param tablesFileName  name of the BAT file inside the archive for configuring tables
     */
    MsSqlTradeDbScriptVersion(String archiveFileName, TasExtension extension, String unpackDir, String confFileName, String tablesFileName) {
        artifact = new DefaultArtifact(GROUP_ID, archiveFileName, extension.getValue(), null);
        this.unpackDir = unpackDir;
        this.confFileName = confFileName;
        this.tablesFileName = tablesFileName;
    }

    public String getUnpackDir() {
        return unpackDir;
    }

    public String getConfFileName() {
        return confFileName;
    }

    public String getTablesFileName() {
        return tablesFileName;
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