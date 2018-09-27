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

package com.ca.apm.systemtest.atcqa.artifact.thirdparty;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.artifact.IThirdPartyArtifact;

/**
 * Supported versions of <b>Trade Service Application</b> for testing purposes.
 * 
 * @author Pospichal, Pavel <pospa02@ca.com>
 */
public enum ReportingServiceAppVersion implements IThirdPartyArtifact {

    v_10_5_2_4("10.5.2.4"), v_99_99_aquarius("99.99.aquarius-SNAPSHOT");

    private static final String GROUP_ID = "com.ca.apm.testing";
    private static final String ARTIFACT_ID = "ReportingService";

    private final String version;

    // optional fields
    private final TasExtension type = TasExtension.WAR;

    /**
     * @param version Artifact's version
     */
    private ReportingServiceAppVersion(String version) {
        this.version = version;
    }

    @Override
    public Artifact getArtifact() {
        return new DefaultArtifact(GROUP_ID, ARTIFACT_ID, type.getValue(), version);
    }

    @Override
    public String getFilename() {
        return String.format("%s-%s.%s", ARTIFACT_ID, version, type);
    }

}
