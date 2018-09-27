package com.ca.apm.systemtest.fld.artifact.thirdparty;

import com.ca.tas.artifact.IThirdPartyArtifact;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import static com.ca.tas.artifact.IBuiltArtifact.TasExtension;

/**
 * ActiveMQ artifact.
 * @author Jirinec, Jiri (jirji01)
 */
public enum ActiveMqVersion implements IThirdPartyArtifact {

    v510("5.10.0");

    private static final String GROUP_ID = "com.ca.apm.binaries";
    private static final String ARTIFACT_ID = "activeMQ";
    private static final String CLASSIFIER = "windows";
    private final String version;

    // optional fields
    private final TasExtension type = TasExtension.ZIP;

    ActiveMqVersion(String version) {
        this.version = version;
    }

    @Override
    public Artifact getArtifact() {
        return new DefaultArtifact(GROUP_ID, ARTIFACT_ID, CLASSIFIER, type.getValue(), version);
    }

    @Override
    public String getFilename() {
        return String.format("%s-%s-%s.%s", ARTIFACT_ID, version, CLASSIFIER,  type);
    }

}
