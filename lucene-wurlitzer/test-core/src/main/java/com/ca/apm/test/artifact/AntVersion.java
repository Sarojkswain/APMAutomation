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
package com.ca.apm.test.artifact;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.tas.artifact.IBuiltArtifact;
import com.ca.tas.artifact.IThirdPartyArtifact;

/**
 * Enumeration of Ant versions available in Artifactory.
 * 
 * @author Jan Zak (zakja01@ca.com)
 */
public enum AntVersion implements IThirdPartyArtifact {
    v1_7_1("1.7.1");

    private static final String ARTIFACT_ID = "apache-ant";
    private final IBuiltArtifact.TasExtension extension = IBuiltArtifact.TasExtension.TAR_GZ;
    private final String version;
    private final DefaultArtifact artifact;

    /**
     * @param version
     */
    AntVersion(final String version) {
        this.version = version;
        this.artifact =
            new DefaultArtifact(GROUP_ID, ARTIFACT_ID, this.extension.getValue(), this.version);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ca.tas.artifact.ITasArtifact#getArtifact()
     */
    @Override
    public Artifact getArtifact() {
        return new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(),
            artifact.getClassifier(), artifact.getExtension(), artifact.getVersion());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ca.tas.artifact.IThirdPartyArtifact#getFilename()
     */
    @Override
    public String getFilename() {
        // e.g. apache-ant-1.7.1.tar.gz
        return String.format("%s-%s.%s", ARTIFACT_ID, this.version, this.extension.getValue());
    }

}
