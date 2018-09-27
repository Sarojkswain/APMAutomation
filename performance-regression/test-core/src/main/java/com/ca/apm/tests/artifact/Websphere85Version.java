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
 * MSSQL Server installer
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public enum Websphere85Version implements IThirdPartyArtifact {

    VER_85_JAVA7("InstalMgr/1.5.2", "InstalMgr-1.5.2-WIN_WAS_8.5.zip",
            "was/was-8.5-java",
            "WS_SDK_JAVA_TEV7.0_1OF3_WAS_8.5.zip",
            "WS_SDK_JAVA_TEV7.0_2OF3_WAS_8.5.zip",
            "WS_SDK_JAVA_TEV7.0_3OF3_WAS_8.5.zip",
            "was/was-8.5",
            "WAS_V8.5_1_OF_3.zip",
            "WAS_V8.5_2_OF_3.zip",
            "WAS_V8.5_3_OF_3.zip"
    );

    private static final String GROUP_ID = "com/ca/apm/binaries/ibm";
    private final ITasArtifact installMgrTasArtifact;
    private final ITasArtifact javaZip1TasArtifact;
    private final ITasArtifact javaZip2TasArtifact;
    private final ITasArtifact javaZip3TasArtifact;
    private final ITasArtifact wasZip1TasArtifact;
    private final ITasArtifact wasZip2TasArtifact;
    private final ITasArtifact wasZip3TasArtifact;

    Websphere85Version(String installMgrRelPath, String installMgrZip, String javaRelPath, String javaZip1, String javaZip2, String javaZip3,
                       String wasRelPath, String wasZip1, String wasZip2, String wasZip3) {
        installMgrTasArtifact = new TasArtifact.Builder(installMgrZip).groupId(GROUP_ID + "/" + installMgrRelPath).build();
        javaZip1TasArtifact = new TasArtifact.Builder(javaZip1).groupId(GROUP_ID + "/" + javaRelPath).build();
        javaZip2TasArtifact = new TasArtifact.Builder(javaZip2).groupId(GROUP_ID + "/" + javaRelPath).build();
        javaZip3TasArtifact = new TasArtifact.Builder(javaZip3).groupId(GROUP_ID + "/" + javaRelPath).build();
        wasZip1TasArtifact = new TasArtifact.Builder(wasZip1).groupId(GROUP_ID + "/" + wasRelPath).build();
        wasZip2TasArtifact = new TasArtifact.Builder(wasZip2).groupId(GROUP_ID + "/" + wasRelPath).build();
        wasZip3TasArtifact = new TasArtifact.Builder(wasZip3).groupId(GROUP_ID + "/" + wasRelPath).build();
    }

    @Override
    public Artifact getArtifact() {
        return null;
    }

    @Override
    public String getFilename() {
        return null;
    }

    public Artifact getInstallMgrArtifact() {
        return installMgrTasArtifact.getArtifact();
    }

    public Artifact getJavaZip1Artifact() {
        return javaZip1TasArtifact.getArtifact();
    }

    public Artifact getJavaZip2Artifact() {
        return javaZip2TasArtifact.getArtifact();
    }

    public Artifact getJavaZip3Artifact() {
        return javaZip3TasArtifact.getArtifact();
    }

    public Artifact getWasZip1Artifact() {
        return wasZip1TasArtifact.getArtifact();
    }

    public Artifact getWasZip2Artifact() {
        return wasZip2TasArtifact.getArtifact();
    }

    public Artifact getWasZip3Artifact() {
        return wasZip3TasArtifact.getArtifact();
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