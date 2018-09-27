package com.ca.apm.systemtest.fld.artifact.thirdparty;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.artifact.IThirdPartyArtifact;

/**
 * Chromedriver dist exe artifact.
 * @author Filip, Jan (filja01)
 */
public enum ChromeBrowserVersion implements IThirdPartyArtifact {

    v13("1.3.21.165");
    private static final String GROUP_ID = "com.ca.apm.binaries.google";
    private static final String ARTIFACT_ID = "chrome_installer";
    private static final String CLASSIFIER = null;
    private final String version;

    // optional fields
    private final TasExtension type = TasExtension.EXE;

    ChromeBrowserVersion(String version) {
        this.version = version;
    }

    @Override
    public Artifact getArtifact() {
        return new DefaultArtifact(GROUP_ID, ARTIFACT_ID, CLASSIFIER, type.getValue(), version);
    }

    @Override
    public String getFilename() {
        return String.format("%s-%s-%s.%s", ARTIFACT_ID, version, CLASSIFIER, type);
    }

}
