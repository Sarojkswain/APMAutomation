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
 * Trade6
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public enum Trade6Version implements IThirdPartyArtifact {

    VER_6("trade6_with_py_scripts", TasExtension.ZIP, "trade.ear", "trade6_singleServer.py", "resource_scripts.py");

    private static final String GROUP_ID = "com/ca/apm/coda/ws-trade6/java7";
    private final DefaultArtifact artifact;

    private final String applicationEar;
    private final String installScript;
    private final String resourcesScript;


    /**
     * @param archiveFileName name of the file in Artifactory
     * @param extension       file extension.
     * @param applicationEar  name of the EAR file inside the archive
     * @param installScript   name of the Script file for installing the webapp inside the archive
     * @param resourcesScript name of the resources Script file inside the archive
     */
    Trade6Version(String archiveFileName, TasExtension extension, String applicationEar, String installScript,
                  String resourcesScript) {
        artifact = new DefaultArtifact(GROUP_ID, archiveFileName, extension.getValue(), null);
        this.applicationEar = applicationEar;
        this.installScript = installScript;
        this.resourcesScript = resourcesScript;
    }

    public String getApplicationEar() {
        return applicationEar;
    }

    public String getInstallScript() {
        return installScript;
    }

    public String getResourcesScript() {
        return resourcesScript;
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
        artifactUrlTmp += "/" + this.getArtifact().getGroupId() + "/" + this.getFilename();
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