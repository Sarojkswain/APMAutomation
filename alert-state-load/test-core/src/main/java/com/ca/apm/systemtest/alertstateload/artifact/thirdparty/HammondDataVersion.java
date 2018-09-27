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
package com.ca.apm.systemtest.alertstateload.artifact.thirdparty;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.artifact.IThirdPartyArtifact;

public enum HammondDataVersion implements IThirdPartyArtifact {

    AlertStatusLoad("alert-status-load", null, HammondDataVersion.GROUP_ID_systemtest_perf_alertstatus, HammondDataVersion.VERSION_systemtest_perf_alertstatus);

    private static final String GROUP_ID_systemtest_perf_alertstatus = "com.ca.apm.systemtest.perf.alertstatus";
    private static final String VERSION_systemtest_perf_alertstatus = "1.0.0";

    private final DefaultArtifact artifact;
    private final String artifactId;
    private final String classifier;
    private final String groupId;
    private final String version;
    private final String type = TasExtension.ZIP.getValue();

    /**
     * @param artifactId
     * @param classifier
     * @param groupId
     * @param version
     */
    private HammondDataVersion(String artifactId, String classifier, String groupId, String version) {
        this.artifactId = artifactId;
        this.classifier = classifier;
        this.groupId = groupId;
        this.version = version;
        this.artifact = new DefaultArtifact(this.groupId, this.artifactId, this.classifier, this.type, this.version);
    }

    /** {@inheritDoc} */
    @Override
    public Artifact getArtifact() {
        return new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getClassifier(), artifact.getExtension(), artifact.getVersion());
    }

    /** {@inheritDoc} */
    @Override
    public String getFilename() {
        return classifier == null ? String.format("%s-%s.%s", artifactId, version, type) : String.format("%s-%s-%s.%s", artifactId, version, classifier, type);
    }

    public String getVersion() {
        return version;
    }

}
