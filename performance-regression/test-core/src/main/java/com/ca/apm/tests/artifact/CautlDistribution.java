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

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.tas.artifact.IThirdPartyArtifact;

/**
 * WurlitzerArtifact class
 * <p/>
 * Class description
 *
 * @author Jiri Jirinec (jirji01.ca.com)
 * @since 1.0
 */
public final class CautlDistribution
    implements IThirdPartyArtifact
{

    private DefaultArtifact artifact;

    public CautlDistribution()
    {
        this.artifact = new DefaultArtifact("com.ca.apm.binaries", "cautl", "zip", "1.0");
    }

    @Override
    public String getFilename()
    {
        return String.format("%s-%s.%s", artifact.getArtifactId(), artifact.getVersion(), artifact.getExtension());
    }

    @Override
    public Artifact getArtifact()
    {
        return new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getClassifier(),
                                   artifact.getExtension(), artifact.getVersion());
    }
}
