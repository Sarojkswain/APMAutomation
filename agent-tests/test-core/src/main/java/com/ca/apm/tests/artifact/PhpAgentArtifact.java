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

import org.apache.http.util.Args;

import com.ca.tas.artifact.IBuiltArtifact;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.ITasArtifactFactory;
import com.ca.tas.artifact.TasArtifact;
import com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform;
import com.ca.tas.resolver.ITasResolver;

/**
 * PhpAgentArtifact class.
 *
 * Class encapsulates logic handling the PHP Agent artifact.
 *
 * @author Aleem Ahmad (ahmal01@ca.com)
 */
public class PhpAgentArtifact implements ITasArtifactFactory {

    private static final String GROUP_ID = "com.ca.apm.agent.PHPAgent";
    private static final String ARTIFACT_ID = "PHPAgent-dist";
    private final String classifier;
    private final IBuiltArtifact.TasExtension extension;

    private final ITasResolver resolver;

    public PhpAgentArtifact(ArtifactPlatform platform, ITasResolver resolver) {
        Args.notNull(platform, "Platform");
        Args.notNull(resolver, "Resolver");

        this.resolver = resolver;

        switch (platform) {
            case LINUX:
            case LINUX_AMD_64:
                classifier = "linux-amd64";
                extension = IBuiltArtifact.TasExtension.TAR_GZ;
                break;
            default:
                throw new IllegalArgumentException(String.format(
                    "Provided platform (%s) is not supported for PhpAgentArtifact installer.",
                    platform));
        }
    }

    @Override
    public ITasArtifact createArtifact(String version) {
        return new TasArtifact.Builder(ARTIFACT_ID).groupId(GROUP_ID)
            .version((version == null) ? resolver.getDefaultVersion() : version)
            .classifier(classifier).extension(extension).build();
    }

    @Override
    public ITasArtifact createArtifact() {
        return createArtifact(null);
    }
}
