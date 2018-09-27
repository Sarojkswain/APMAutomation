/*
 * Copyright (c) 2014 CA. All rights reserved.
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

package com.ca.apm.systemtest.fld.artifact.thirdparty;

import com.ca.tas.artifact.IThirdPartyArtifact;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

/**
 * Supported versions of Apache Tomcat binary distribution.
 * 
 * @author Pospichal, Pavel
 */
public enum TomcatVersion implements IThirdPartyArtifact {
    v55("5.5.34"), v60("6.0.36"), v70("7.0.57"), v80("8.0.17");

    private static final String ARTIFACT_ID = "tomcat";

    // mandatory fields
    private final DefaultArtifact artifact;
    private final String version;
    // optional fields
    private final String type = "zip";

    TomcatVersion(String version) {
        this.version = version;
        this.artifact = new DefaultArtifact(GROUP_ID, ARTIFACT_ID, this.type, this.version);
    }

    @Override
    public Artifact getArtifact() {
        return new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(),
            artifact.getClassifier(), artifact.getExtension(), artifact.getVersion());
    }

    @Override
    public String getFilename() {
        return String.format("%s-%s.%s", ARTIFACT_ID, this.version, this.type);
    }

}
