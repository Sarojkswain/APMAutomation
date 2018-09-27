package com.ca.apm.tests.utils;

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

import com.ca.tas.artifact.built.*;

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
 * @version $Id: $Id
 */
public class ApmSqlSeverArtifact implements ITasArtifactFactory {

    
    private static final String GROUP_ID = "com.ca.apm.em";
    private static final String ARTIFACT_ID = "com.ca.apm.server.teiid";

    private final ITasResolver resolver;

    /**
     * <p>Constructor for BuildArtifactTemplate.</p>
     *
     * @param resolver a {@link com.ca.tas.resolver.ITasResolver} object.
     */
    public ApmSqlSeverArtifact(ITasResolver resolver) {
        this.resolver = resolver;
    }

    /** {@inheritDoc} */
    @Override
    public ITasArtifact createArtifact(String version) {
        return new TasArtifact.Builder(getArtifactId())
            .groupId(GROUP_ID)
            .extension(IBuiltArtifact.TasExtension.ZIP)
            .version((version == null) ? resolver.getDefaultVersion(): version)          
            .build();
    }

    /** {@inheritDoc} */
    @Override
    public ITasArtifact createArtifact() {
        return createArtifact(null);
    }

    private String getArtifactId() {
        return ARTIFACT_ID;
        
    }
}
