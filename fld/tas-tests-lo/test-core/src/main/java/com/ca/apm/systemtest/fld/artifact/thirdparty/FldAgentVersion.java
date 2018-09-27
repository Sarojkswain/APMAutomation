package com.ca.apm.systemtest.fld.artifact.thirdparty;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.artifact.IThirdPartyArtifact;

/**
 * Agetn dist zip artifact.
 * @author Jirinec, Jiri (jirji01)
 */
public enum FldAgentVersion implements IThirdPartyArtifact {

    snapshot("99.99.aquarius-SNAPSHOT");

    private static final String GROUP_ID = "com.ca.apm.systemtest.fld";
    private static final String ARTIFACT_ID = "agent";
    private static final String CLASSIFIER = "dist";
    private final String version;

    // optional fields
    private final TasExtension type = TasExtension.ZIP;

    FldAgentVersion(String version) {
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
