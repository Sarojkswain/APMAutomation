/*
 * Copyright (c) 2015 CA. All rights reserved.
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
package com.ca.tas.artifact.built;

import org.jetbrains.annotations.NotNull;

import com.ca.tas.artifact.IBuiltArtifact;
import com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform;
import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.ITasArtifactFactory;
import com.ca.tas.artifact.TasArtifact;
import com.ca.tas.resolver.ITasResolver;

/**
 * 
 * @author Pospichal, Pavel <pospa02@ca.com>
 * 
 */
public class TIMAsSoftwareDistribution implements ITasArtifactFactory {

    private final String GROUP_ID = "com.ca.apm.cem";

    private final String type;
    private final ArtifactPlatform platform;
    private final ITasResolver resolver;
    private final IBuiltArtifact.TasExtension extension;

    public TIMAsSoftwareDistribution(@NotNull ArtifactPlatform platform, ITasResolver resolver) {
        this.platform = platform;
        this.resolver = resolver;

        switch (this.platform) {
        // support for 32-bit is not currently produced as part of APM
            case LINUX_AMD_64:
                type = "rhel6";
                extension = TasExtension.ZIP;
                break;
            default:
                throw new IllegalArgumentException(String.format(
                        "Provided platform (%s) is not supported for TIM as software.", platform));
        }
    }

    @Override
    public ITasArtifact createArtifact(String version) {
        return new TasArtifact.Builder(getArtifactId()).groupId(GROUP_ID)
                .classifier("Linux-el6-x64").extension(extension)
                .version((version == null) ? resolver.getDefaultVersion() : version).build();
    }

    @Override
    public ITasArtifact createArtifact() {
        return createArtifact(null);
    }

    private String getArtifactId() {
        return String.format("tim-%s-dist", type);
    }

}
