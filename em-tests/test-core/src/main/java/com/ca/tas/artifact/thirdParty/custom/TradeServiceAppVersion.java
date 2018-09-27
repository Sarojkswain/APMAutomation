/*
 * Copyright (c) 2014 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.tas.artifact.thirdParty.custom;

import java.util.HashMap;
import java.util.Map;

import com.ca.tas.artifact.IThirdPartyArtifact;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import static com.ca.tas.artifact.IBuiltArtifact.TasExtension;

/**
 * Supported versions of <b>Trade Service Application</b> for testing purposes.
 * 
 * @author Pospichal, Pavel <pospa02@ca.com>
 */
public enum TradeServiceAppVersion implements IThirdPartyArtifact {

    v10("1.0.0");

    private static final String GROUP_ID = "com.ca.apm.coda";
    private static final String ARTIFACT_ID = "trade-service-app";
    private final String version;

    // optional fields
    private final TasExtension type = TasExtension.EAR;

    /**
     * @param version Artifact's version
     */
    TradeServiceAppVersion(String version) {
        this.version = version;
    }

    @Override
    public Artifact getArtifact() {
        return new DefaultArtifact(GROUP_ID, ARTIFACT_ID, type.getValue(), version);
    }
    
    public Map<String, Artifact> getBusinessTransactionCEMExports() {
        Map<String, Artifact> businessTransactionCEMExports = new HashMap<>(1);
        businessTransactionCEMExports.put("default", new DefaultArtifact(GROUP_ID,
                "trade-service-app-bt-exports", TasExtension.ZIP.getValue(), version));
        return businessTransactionCEMExports;
    }

    @Override
    public String getFilename() {
        return String.format("%s-%s.%s", ARTIFACT_ID, version, type);
    }

}
