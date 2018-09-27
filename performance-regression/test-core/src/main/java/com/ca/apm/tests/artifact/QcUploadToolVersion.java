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
import org.eclipse.aether.artifact.Artifact;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public class QcUploadToolVersion implements ITasArtifactFactory {
    private static final String GROUP_ID = "com.ca.apm.coda-projects.test-tools";
    private static final String ARTIFACT_ID = "qcuploadtool";
    private static final IBuiltArtifact.TasExtension EXTENSION;
    private final ITasResolver resolver;

    static {
        EXTENSION = IBuiltArtifact.TasExtension.ZIP;
    }

    public QcUploadToolVersion(ITasResolver resolver) {
        this.resolver = resolver;
    }

    public ITasArtifact createArtifact(String version) {
        return (new TasArtifact.Builder(ARTIFACT_ID)).groupId(GROUP_ID).classifier("dist")
                .version(version == null ? this.resolver.getDefaultVersion() : version)
                .extension(EXTENSION).build();
    }

    public String getFilename() {
        Artifact artifact = createArtifact().getArtifact();
        return String.format("%s.%s", artifact.getArtifactId(), artifact.getExtension());
    }

    public ITasArtifact createArtifact() {
        return this.createArtifact((String) null);
    }
}