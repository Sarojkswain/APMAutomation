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

/**
 * BrowserAgentSecondaryTestappArtifact class represents war file artifacts for Secondary
 * Test Applications for BrowserAgent Automation
 * 
 * @author pojja01, gupra04
 *
 */

package com.ca.apm.tests.artifact.testapp;

import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.artifact.ITasArtifact;

import com.ca.tas.artifact.IThirdPartyArtifact;
import com.ca.tas.artifact.TasArtifact;

import org.eclipse.aether.artifact.Artifact;

public enum BrowserAgentSecondaryTestappArtifact implements IThirdPartyArtifact {

    CLICK_LISTENER_TEST2_V1_0("ClickListenerTest_2"), DEMO_WEBAPP_V1_0("demowebapp"), DWR_V1_0(
        "dwr"), INFI_V1_0("infi"), SESSION_TEST_V1_0("SessionTest"), URCHIN_TRACKER_V1_0(
        "UrchinTracker");

    private static final String GROUP_ID = "com.ca.apm.binaries";
    private static final String VERSION = "1.0";
    private static final String ARTIFACT_ID = "BrowserAgent";

    private final ITasArtifact tasArtifact;

    BrowserAgentSecondaryTestappArtifact(final String classifier) {
        this(classifier, TasExtension.WAR);
    }

    BrowserAgentSecondaryTestappArtifact(final String classifier, final TasExtension type) {
        this(VERSION, classifier, type);
    }

    BrowserAgentSecondaryTestappArtifact(final String version, final String classifier,
        final TasExtension type) {
        this(ARTIFACT_ID, VERSION, classifier, type);
    }

    BrowserAgentSecondaryTestappArtifact(final String artifactId, final String version,
        final String classifier, final TasExtension type) {
        this.tasArtifact =
            new TasArtifact.Builder(artifactId).groupId(GROUP_ID).version(version)
                .classifier(classifier).version(version).extension(type).build();
    }

    @Override
    public String getFilename() {
        final Artifact artifact = getArtifact();
        return String.format("%s-%s-%s.%s", artifact.getArtifactId(), artifact.getVersion(),
            artifact.getClassifier(), artifact.getExtension());
    }

    @Override
    public Artifact getArtifact() {
        return this.tasArtifact.getArtifact();
    }
}
