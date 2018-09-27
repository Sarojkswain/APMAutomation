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

package com.ca.apm.tests.role;

import com.ca.tas.artifact.IThirdPartyArtifact;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.type.Platform;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

/**
 * WebLogicVersion class
 *
 * Supported WebLogic version enumeration
 *
 * @author Jan Pojer (pojja01.ca.com)
 * @version $Id: $Id
 * @since 1.0
 */
public enum WebLogicPortalVersion implements IThirdPartyArtifact {
    v103x86w("10.3.0"),
    v1034generic("10.3.4", "generic", "jar"),
    
    v103x86win("10.3.0","windows-x86", "exe"),
    v1034win("10.3.4","windows-x86", "exe"),
    v1035x86linux("10.3.5", "linux-x86", "bin"); // Not on RedHat as requires the installation of 32-bit libc libraries

    /**
     * Constant <code>ARTIFACT_ID="weblogic"</code>
     */
    private static final String ARTIFACT_ID = "weblogic-portal";
    //mandatory fields
    private final DefaultArtifact artifact;
    private final String version;
    private final String classifier;
    //optional fields
    private final String type;

    /**
     * Default constructor for enum with specific version passed.
     *
     * @param version Artifact's version
     */
    WebLogicPortalVersion(String version) {
        this(version, Platform.WINDOWS, Bitness.b32);
    }

    /**
     * Default constructor for enum with specific version and platform passed.
     *
     * @param version  Artifact's version
     * @param platform Artifact's platform
     */
    WebLogicPortalVersion(String version, Platform platform) {
        this(version, platform, Bitness.b32);
    }

    /**
     * Default constructor for enum with specific version and bitness passed.
     *
     * @param version Artifact's version
     * @param bitness 32 vs 64 bitness
     */
    WebLogicPortalVersion(String version, Bitness bitness) {
        this(version, Platform.WINDOWS, bitness);
    }

    /**
     * @param version  Artifact's version
     * @param platform Artifact's platform
     * @param bitness  32 vs 64 bitnes
     */
    WebLogicPortalVersion(String version, Platform platform, Bitness bitness) {
        this.version = version;
        this.classifier = platform.toString().toLowerCase() + "-" + bitness.getArchitecture();
        this.type = "exe";
        this.artifact = new DefaultArtifact(GROUP_ID, ARTIFACT_ID, this.classifier, this.type, this.version);
    }

    WebLogicPortalVersion(final String version, final String classifier, final String type) {
        this.version = version;
        this.classifier = classifier;
        this.type = type;
        this.artifact = new DefaultArtifact(GROUP_ID, ARTIFACT_ID, this.classifier, this.type, this.version);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Artifact getArtifact() {
        return new DefaultArtifact(
            artifact.getGroupId(),
            artifact.getArtifactId(),
            artifact.getClassifier(),
            artifact.getExtension(),
            artifact.getVersion()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFilename() {
        return String.format("%s-%s-%s.%s", ARTIFACT_ID, this.version, this.classifier, this.type);
    }
}
