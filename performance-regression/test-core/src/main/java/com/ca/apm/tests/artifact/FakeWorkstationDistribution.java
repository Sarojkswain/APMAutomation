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

import org.jetbrains.annotations.Nullable;

import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.ITasArtifactFactory;
import com.ca.tas.artifact.TasArtifact;
import com.ca.tas.resolver.ITasResolver;

/**
 * WurlitzerArtifact class
 * <p/>
 * Class description
 *
 * @author Jiří Jiřinec (pojja01.ca.com)
 * @since 1.0
 */
public final class FakeWorkstationDistribution
    implements ITasArtifactFactory
{
    private ITasResolver resolver;

    public FakeWorkstationDistribution(ITasResolver resolver) {
        this.resolver = resolver;
    }
    
    @Override
    public ITasArtifact createArtifact(@Nullable String version)
    {
        return new TasArtifact.Builder("fakeworkstation")
                .groupId("com.ca.apm.coda-projects.test-tools")
                .version((version == null) ? resolver.getDefaultVersion() : version)
                .classifier("dist")
                .extension(TasExtension.ZIP)
                .build();
    }

    @Override
    public ITasArtifact createArtifact()
    {
        return createArtifact(null);
    }
}
