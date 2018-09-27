/*
 * Copyright (c) 2015 CA. All rights reserved.
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

package com.ca.apm.tests.tibco.artifact;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.tas.artifact.IThirdPartyArtifact;

/**
 * List down the available versions of tibco components.
 * Vashistha Singh (sinva01.ca.com)
 *
 */
public enum TibcoSoftwareComponentVersions implements IThirdPartyArtifact {
    TibcoRVWindowsx64v8_4_0("com.ca.apm.binaries.tibco", "tibco-rv", "8.4.0", "windows-x64", "zip"), TibcoTRAWindowsx64v5_8_0(
        "com.ca.apm.binaries.tibco", "tibco-tra", "5.8.0", "windows-x64", "zip"), TibcoBWWindowsx64v5_11_0(
        "com.ca.apm.binaries.tibco", "tibco-bw", "5.11.0", "windows-x64", "zip"), TibcoEMSWindowsx64v6_3_0(
        "com.ca.apm.binaries.tibco", "tibco-ems", "6.3.0", "windows-x64", "zip"), TibcoAdminWindowsx64v5_8_0(
        "com.ca.apm.binaries.tibco", "tibco-tibcoadmin", "5.8.0", "windows-x64", "zip");

    private final DefaultArtifact artifact;
    private String version;

    TibcoSoftwareComponentVersions(String groupId, String artifactId, String version,
        String classifier, String extension) {
        artifact = new DefaultArtifact(groupId, artifactId, classifier, extension, version);
        this.version = version;
    }

    @Override
    public Artifact getArtifact() {
        return artifact;
    }

    public String getVersion(){
        String[] temp = this.version.split("\\.");
        if(temp.length > 2) {
            String concatVer = temp[0] + "." + temp[1];
            return concatVer;
        }
        return this.version;
    }

    @Override
    public String getFilename() {
        return String.format("%s-%s-%s.%s", artifact.getArtifactId(), artifact.getVersion(),
            artifact.getClassifier(), artifact.getExtension());
    }
}
