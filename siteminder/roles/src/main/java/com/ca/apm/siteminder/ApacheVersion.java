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
public enum ApacheVersion implements IThirdPartyArtifact {
    v2225x32w("2.2.25");

    private static final String APACHE_GROUP_ID = GROUP_ID + ".apache";
    private static final String ARTIFACT_ID = "httpd";
    private String version;
    private String classifier;
    private String type = "msi";
    private DefaultArtifact artifact;

    /**
     * 
     */
    private ApacheVersion(final String version) {

        this(version, Platform.WINDOWS, Bitness.b32);
    }

    /**
     * @param version
     * @param platform
     * @param bitness
     */
    ApacheVersion(final String version, final Platform platform, final Bitness bitness) {
        this.version = version;
        // win32-x86-openssl-0.9.8y
        this.classifier =
                platform.toString().toLowerCase().substring(0, 3) + bitness.getBits()
                        + "-x86-openssl-0.9.8y";
        this.artifact = new DefaultArtifact(APACHE_GROUP_ID, ARTIFACT_ID, this.classifier,
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
        // e.g. httpd-2.2.25-win32-x86-openssl-0.9.8y.msi
        return String.format("%s-%s-%s.%s", ARTIFACT_ID, this.version, this.classifier, this.type);
    }

}
