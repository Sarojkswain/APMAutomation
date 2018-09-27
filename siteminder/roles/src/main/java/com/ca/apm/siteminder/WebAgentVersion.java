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
public enum WebAgentVersion implements IThirdPartyArtifact {

    v1252sp1x86w("12.52-sp01");

    private static final String SITEMINDER_GROUP_ID = GROUP_ID + ".siteminder";
    private static final String WA_ARTIFACT_ID = "wa";
    private static final String WA_OPTION_PACK_ID = "wa-opack";

    private String version;
    private String classifier;
    private String type = "zip";
    private DefaultArtifact artifact;

    /**
     * 
     */
    WebAgentVersion(String version) {
        this(version, Platform.WINDOWS);
    }

    /**
     * @param version
     * @param platform
     */
    WebAgentVersion(String version, Platform platform) {
        this(version, platform, Bitness.b32);

    }

    /**
     * @param version
     * @param platform
     * @param bitness
     */
    WebAgentVersion(String version, Platform platform, Bitness bitness) {
        this.version = version;
        this.classifier = platform.toString().toLowerCase().substring(0, 3) + bitness.getBits();
        this.artifact =
                new DefaultArtifact(SITEMINDER_GROUP_ID, WA_ARTIFACT_ID, this.classifier, this.type,
                        this.version);

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

    /**
     * @return
     */
    public Artifact getOptionPack() {
        return new DefaultArtifact(
            artifact.getGroupId(),
            WebAgentVersion.WA_OPTION_PACK_ID,
            artifact.getClassifier(),
            artifact.getExtension(),
            artifact.getVersion()
        );
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ca.tas.artifact.IThirdPartyArtifact#getFilename()
     */
    @Override
    public String getFilename() {
        // wa-12.52-sp01-win32.zip
        return String.format("%s-%s-%s.%s", WA_ARTIFACT_ID, this.version, this.classifier, this.type);
    }

    public String getOptionPackFilename() {
        // wa-12.52-sp01-win32.zip
        return String.format("%s-%s-%s.%s", WA_OPTION_PACK_ID, this.version, this.classifier, this.type);
    }

    /**
     * At the moment supports only Windows environment, not sure other platforms will be needed
     * 
     * @return the name of the installer executable
     */
    public String getInstallerFilename() {
        // ca-wa-12.52-sp01-win32.exe
        return String.format("%s-%s-%s-%s.%s", "ca", WA_ARTIFACT_ID, this.version, this.classifier,
                "exe");
    }

    /**
     * At the moment supports only Windows environment, not sure other platforms will be needed
     * 
     * @return the name of the option pack installer executable
     */
    public String getOptionPackInstallerFilename() {
        // ca-wa-12.52-sp01-win32.exe
        return String.format("%s-%s-%s-%s.%s", "ca", WA_OPTION_PACK_ID, this.version, this.classifier,
                "exe");
    }

}
