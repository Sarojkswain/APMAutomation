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
package com.ca.apm.siteminder;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.tas.artifact.IThirdPartyArtifact;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.type.Platform;

/**
 * @author surma04
 *
 */
public enum CADirectoryVersion implements IThirdPartyArtifact {

    v120x64w("12.0");

    private static final String ARTIFACT_ID = "cadirectory";
    private String type = "exe";

    private Artifact artifact;
    private String version;
    private String classifier;


    /**
     * Default constructor for specific version passed.
     *
     * @param version Artifact's version
     */
    CADirectoryVersion(String version)
    {
        this(version, Platform.WINDOWS, Bitness.b64);
    }

    /**
     * @param version
     * @param windows
     * @param bitness
     */
    CADirectoryVersion(String versionParam, Platform platform, Bitness bitness) {
        this.version = versionParam;
        this.classifier =
                platform.toString().toLowerCase() + "-x" +  bitness.getBits();
        this.artifact =
                new DefaultArtifact(GROUP_ID, ARTIFACT_ID, this.classifier,
                        this.type, this.version);


    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ca.tas.artifact.ITasArtifact#getArtifact()
     */
    @Override
    public Artifact getArtifact() {
        return new DefaultArtifact(
                artifact.getGroupId(),
                artifact.getArtifactId(),
                artifact.getClassifier(),
                artifact.getExtension(),
                artifact.getVersion());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ca.tas.artifact.IThirdPartyArtifact#getFilename()
     */
    @Override
    public String getFilename() {
        return String.format("%s-%s-%s.%s", ARTIFACT_ID, this.classifier, this.version, this.type);
    }
}
