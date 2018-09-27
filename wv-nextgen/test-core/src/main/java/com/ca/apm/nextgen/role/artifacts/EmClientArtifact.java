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

package com.ca.apm.nextgen.role.artifacts;

import com.ca.tas.artifact.IBuiltArtifact;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.ITasArtifactFactory;
import com.ca.tas.artifact.TasArtifact;
import com.ca.tas.resolver.ITasResolver;

public class EmClientArtifact implements ITasArtifactFactory {
    // <dependency>
    // <groupId>com.ca.apm.common</groupId>
    // <artifactId>com.wily.introscope.em.client</artifactId>
    // <version>${project.version}</version>
    // </dependency>

    private static final String GROUP_ID = "com.ca.apm.common";
    private static final String ARTIFACT_ID = "com.wily.introscope.em.client";
    private static final IBuiltArtifact.TasExtension EXTENSION = IBuiltArtifact.TasExtension.JAR;

    public static final String FILE_NAME = "IntroscopeJDBC.jar";

    private final ITasResolver resolver;


    public EmClientArtifact(ITasResolver resolver) {
        this.resolver = resolver;
    }


    @Override
    public ITasArtifact createArtifact(String version) {
        return new TasArtifact.Builder(ARTIFACT_ID).groupId(GROUP_ID)
            .version((version == null) ? resolver.getDefaultVersion() : version)
            .extension(EXTENSION).build();
    }

    @Override
    public ITasArtifact createArtifact() {
        return createArtifact(null);
    }


}
