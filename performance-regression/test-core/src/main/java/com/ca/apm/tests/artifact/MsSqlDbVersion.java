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
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.IThirdPartyArtifact;
import com.ca.tas.artifact.TasArtifact;
import org.eclipse.aether.artifact.Artifact;

/**
 * MSSQL Server installer
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public enum MsSqlDbVersion implements IThirdPartyArtifact {

    VER_2008("sqlserver2008", "10.0.1600.22", TasExtension.ZIP, "mssqlserver2008", "MSSQLConfigurationFile_install.ini", "MSSQLConfigurationFile_uninstall.ini", "setup.exe");

    private static final String GROUP_ID = IThirdPartyArtifact.GROUP_ID + ".microsoft";
    private final ITasArtifact tasArtifact;
    private final String unpackDir;
    private final String installResponseFile;
    private final String uninstallResponseFile;
    private final String installerFile;

    /**
     * @param artifactId            name of the artifact
     * @param version               version number
     * @param type                  extension
     * @param unpackDir             directory that contains all the files inside the archive
     * @param installResponseFile   Installation response file name inside the archive
     * @param uninstallResponseFile Uninstallation response file name inside the archive
     * @param installerFile         Installer file name inside the archive
     */
    MsSqlDbVersion(String artifactId, String version, TasExtension type, String unpackDir, String installResponseFile, String uninstallResponseFile, String installerFile) {
        tasArtifact = new TasArtifact.Builder(artifactId).groupId(GROUP_ID).version(version).extension(type).build();
        this.unpackDir = unpackDir;
        this.installResponseFile = installResponseFile;
        this.uninstallResponseFile = uninstallResponseFile;
        this.installerFile = installerFile;
    }

    public String getUnpackDir() {
        return unpackDir;
    }

    public String getInstallResponseFile() {
        return installResponseFile;
    }

    public String getUninstallResponseFile() {
        return uninstallResponseFile;
    }

    public String getInstallerFile() {
        return installerFile;
    }

    @Override
    public String getFilename() {
        Artifact artifact = getArtifact();
        return String
                .format("%s-%s-%s.%s", artifact.getArtifactId(), artifact.getVersion(), artifact.getExtension());
    }

    @Override
    public Artifact getArtifact() {
        return tasArtifact.getArtifact();
    }
}