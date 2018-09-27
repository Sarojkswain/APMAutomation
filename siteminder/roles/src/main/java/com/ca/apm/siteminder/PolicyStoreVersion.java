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
package com.ca.apm.siteminder;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.tas.artifact.IThirdPartyArtifact;
import com.ca.tas.type.Platform;

public enum PolicyStoreVersion  implements IThirdPartyArtifact {

    v125x86w("1.1");

    private static final String SITEMINDER_GROUP_ID = GROUP_ID + ".siteminder";
    private static final String ARTIFACT_ID = "smps_auto";

    private DefaultArtifact artifact;
    private String version;
    private String type = "zip";

    /**
     * Default constructor for specific version passed.
     *
     * @param version Artifact's version
     */
    PolicyStoreVersion(String version)
    {
        this(version, Platform.WINDOWS);
    }

    /**
     * 
     * @param versionParam
     * @param platformParam
     * @param bitnessParam
     */
    PolicyStoreVersion(final String versionParam, final Platform platformParam) {
        this.version = versionParam;
        this.artifact = new DefaultArtifact(SITEMINDER_GROUP_ID, ARTIFACT_ID, this.type , this.version);
    }

    @Override
    public Artifact getArtifact() {
        return new DefaultArtifact(
            artifact.getGroupId(),
            artifact.getArtifactId(),
            artifact.getExtension(),
            artifact.getVersion());
    }

    @Override
    public String getFilename() {
        return String.format("%s-%s.%s", ARTIFACT_ID, this.version, this.type);
    }

}
