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

package com.ca.apm.tests.artifact;

import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.IThirdPartyArtifact;
import com.ca.tas.artifact.TasArtifact;

import org.eclipse.aether.artifact.Artifact;

/**
 * ThirdPartyArtifactTemplate class
 *
 * Template for creating third party artifacts
 *
 * @author Jan Pojer (pojja01@ca.com)
 */
public enum ThirdPartyArtifactTemplate implements IThirdPartyArtifact {

    ENUM_NAME("artifactId", "version", "classifier", TasExtension.ZIP);

    private static final String GROUP_ID = IThirdPartyArtifact.GROUP_ID;
    private final ITasArtifact tasArtifact;

    ThirdPartyArtifactTemplate(String artifactId, String version, String classifier, TasExtension type) {
        tasArtifact = new TasArtifact.Builder(artifactId)
            .groupId(GROUP_ID)
            .version(version)
            .extension(type)
            .classifier(classifier)
            .build();
    }

    @Override
    public String getFilename() {
        Artifact artifact = getArtifact();
        return String
            .format("%s-%s-%s.%s", artifact.getArtifactId(), artifact.getVersion(), artifact.getClassifier(), artifact.getExtension());
    }

    @Override
    public Artifact getArtifact() {
        return tasArtifact.getArtifact();
    }
}