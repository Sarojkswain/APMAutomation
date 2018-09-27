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

package com.ca.apm.test.em.metadata.hammond;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.tas.artifact.IBuiltArtifact;
import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.ITasArtifactFactory;
import com.ca.tas.artifact.IThirdPartyArtifact;
import com.ca.tas.artifact.TasArtifact;
import com.ca.tas.resolver.ITasResolver;

public class HammondArtifact implements ITasArtifactFactory {
    
    public enum Data implements IThirdPartyArtifact {
        ATT_em1("ATT_em1"), ATT_em2("ATT_em2"), ATT_em3("ATT_em3"), METADATA_TEST_DATA(
            "metadata-test-data", "1.0");

        private static final String GROUP_ID = IThirdPartyArtifact.GROUP_ID + ".microsoft";
        private Artifact artifact; 
        
        Data(String archiveFileName) {
            artifact =
                new DefaultArtifact(GROUP_ID, archiveFileName, TasExtension.ZIP.getValue(), null);
        }

        Data(String archiveFileName, String version) {
            artifact =
                new DefaultArtifact(GROUP_ID, archiveFileName, TasExtension.ZIP.getValue(), version);
        }
        
        @Override
        public Artifact getArtifact() {
            return artifact;
        }

        @Override
        public String getFilename() {
            return String.format("%s.%s", artifact.getArtifactId(), artifact.getExtension());
        }
    }
    
    private static final String GROUP_ID = "com.ca.apm.systemtest.fld";
    private static final String ARTIFACT_ID = "hammond";
    private static final String CLASSIFIER = "jar-with-dependencies";
    private static final IBuiltArtifact.TasExtension EXTENSION = IBuiltArtifact.TasExtension.JAR;

    private final ITasResolver resolver;

    public HammondArtifact(ITasResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public ITasArtifact createArtifact(String version) {
        return new TasArtifact.Builder(ARTIFACT_ID).groupId(GROUP_ID)
            .version((version == null) ? resolver.getDefaultVersion() : version)
            .extension(EXTENSION).classifier(CLASSIFIER).build();
    }

    @Override
    public ITasArtifact createArtifact() {
        return createArtifact(null);
    }
}
