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
package com.ca.apm.tests.artifact;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.tas.artifact.IThirdPartyArtifact;
import com.ca.tas.artifact.IBuiltArtifact.TasExtension;

public enum HammondDataVersion implements IThirdPartyArtifact {
    ATT_em1("att", "em1"), ATT_em2("att", "em2"), ATT_em3("att", "em3"), FLD_mainframe("fld", "mainframe");

    private static final String GROUP_ID = "com.ca.apm.coda.em-performance.hammond";

    private final DefaultArtifact artifact;
    private final String version;
    private final String artifactId;
    private final String classifier;
    private final String type = TasExtension.ZIP.getValue();

    /**
     * @param artifactId Artifact's Id
     * @param classifier Artifact's classifier
     */
    HammondDataVersion(String artifactId, String classifier) {
        this.version = "1.0";
        this.artifactId = artifactId;
        this.classifier = classifier;
        this.artifact = new DefaultArtifact(GROUP_ID, artifactId, classifier, this.type, this.version);
    }

    /** {@inheritDoc} */
    @Override
    public Artifact getArtifact() {
        return new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(),
                artifact.getClassifier(), artifact.getExtension(), artifact.getVersion());
    }

    /** {@inheritDoc} */
    @Override
    public String getFilename() {
        return String.format("%s-%s-%s.%s", artifactId, version, classifier, type);
    }

    public String getVersion() {
        return version;
    }
}
