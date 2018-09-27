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

import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.IThirdPartyArtifact;
import com.ca.tas.artifact.TasArtifact;
import org.eclipse.aether.artifact.Artifact;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Dot NET Framework installer
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public enum DotNetFxVersion implements IThirdPartyArtifact {

    VER_4_0("4.0", "dotnetfx-4.0-x86_64.exe", "/q /norestart", "/uninstall /x86 /x64 /q /norestart"),
    VER_4_5("4.5.2", "dotnetfx-4.5-x86_64.exe", "/q /norestart", "/uninstall /x86 /x64 /q /norestart");

    private static final String GROUP_ID = "com/ca/apm/binaries/dotnetfx";
    private final ITasArtifact installerArtifact;
    private final String installArgs;
    private final String uninstallArgs;

    DotNetFxVersion(String version, String installer, String installArgs, String uninstallArgs) {
        installerArtifact = new TasArtifact.Builder(installer).groupId(GROUP_ID + "/" + version).build();

        this.installArgs = installArgs;
        this.uninstallArgs = uninstallArgs;
    }

    public String getInstallArgs() {
        return installArgs;
    }

    public String getUninstallArgs() {
        return uninstallArgs;
    }

    @Override
    public Artifact getArtifact() {
        return installerArtifact.getArtifact();
    }

    @Override
    public String getFilename() {
        return installerArtifact.getArtifact().getArtifactId();
    }

    /**
     * Non-standard artifact without POM
     *
     * @param artifactory artifactory URL
     * @return URL of the non-standard artifact
     */
    public URL getArtifactUrl(URL artifactory, Artifact artifact) {
        String artifactUrlTmp = artifactory.toExternalForm();
        artifactUrlTmp += "/" + artifact.getGroupId() + "/" + artifact.getArtifactId();
        try {
            return new URL(artifactUrlTmp);
        } catch (MalformedURLException var6) {
            throw new IllegalArgumentException("Artifact URL resolution failed.", var6);
        }
    }
}