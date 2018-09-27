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

package com.ca.apm.tests.artifact;

import com.ca.tas.artifact.IBuiltArtifact;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.ITasArtifactFactory;
import com.ca.tas.artifact.TasArtifact;
import com.ca.tas.resolver.ITasResolver;

/**
 * BuildArtifactTemplate
 *
 * Template for artifacts created by Jenkins builds
 *
 * @author ...
 */
public class BuildArtifactTemplate implements ITasArtifactFactory {

    private static final IBuiltArtifact.TasExtension EXTENSION = IBuiltArtifact.TasExtension.WAR;
    private static final String GROUP_ID = "groupId";
    private static final String ARTIFACT_ID = "artifactId";

    private final ITasResolver resolver;

    public BuildArtifactTemplate(ITasResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public ITasArtifact createArtifact(String version) {
        return new TasArtifact.Builder(getArtifactId()).groupId(GROUP_ID)
            .version((version == null) ? resolver.getDefaultVersion() : version)
            .extension(EXTENSION).build();
    }

    @Override
    public ITasArtifact createArtifact() {
        return createArtifact(null);
    }

    private String getArtifactId() {
        return ARTIFACT_ID;
    }
}
