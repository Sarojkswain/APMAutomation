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

import com.ca.tas.artifact.IArtifactExtension;
import com.ca.tas.artifact.IBuiltArtifact;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.ITasArtifactFactory;
import com.ca.tas.artifact.TasArtifact;
import com.ca.tas.resolver.ITasResolver;

/**
 * WurlitzerArtifact class.
 *
 * Class encapsulates logic handling the Wurlitzer artifact.
 *
 * @author Jan Zak (zakja01@ca.com)
 */
public class WurlitzerArtifact implements ITasArtifactFactory {

    private static final String GROUP_ID = "com.ca.apm.coda-projects.test-projects";
    private static final String ARTIFACT_ID = "Wurlitzer";
    private static final IBuiltArtifact.TasExtension EXTENSION = IBuiltArtifact.TasExtension.ZIP;

    private final ITasResolver resolver;

    public WurlitzerArtifact(ITasResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public ITasArtifact createArtifact(String version) {
        return new TasArtifact.Builder(ARTIFACT_ID).groupId(GROUP_ID)
            .version((version == null) ? resolver.getDefaultVersion() : version)
            .extension(EXTENSION).build();
    }

    public ITasArtifact createArtifact(String version, String classifier,
        IArtifactExtension extension) {
        return new TasArtifact.Builder(ARTIFACT_ID).groupId(GROUP_ID)
            .version((version == null) ? resolver.getDefaultVersion() : version)
            .classifier(classifier == null ? "" : classifier)
            .extension(extension == null ? EXTENSION : extension).build();
    }

    @Override
    public ITasArtifact createArtifact() {
        return createArtifact(null);
    }
}
