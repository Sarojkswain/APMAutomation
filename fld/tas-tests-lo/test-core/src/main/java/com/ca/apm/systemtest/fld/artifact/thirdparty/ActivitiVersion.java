package com.ca.apm.systemtest.fld.artifact.thirdparty;

import com.ca.tas.artifact.IThirdPartyArtifact;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import static com.ca.tas.artifact.IBuiltArtifact.TasExtension;

/**
 * Activiti artifact.
 * @author Jirinec, Jiri (jirji01)
 */
public enum ActivitiVersion implements IThirdPartyArtifact {

    snapshot("99.99.aquarius-SNAPSHOT");

    private static final String GROUP_ID = "com.ca.apm.systemtest";
    private static final String ARTIFACT_ID = "load-orchestrator-webapp";
    private final String version;

    // optional fields
    private final TasExtension type = TasExtension.WAR;

    ActivitiVersion(String version) {
        this.version = version;
    }

    @Override
    public Artifact getArtifact() {
        return new DefaultArtifact(GROUP_ID, ARTIFACT_ID, type.getValue(), version);
    }

    @Override
    public String getFilename() {
        return String.format("%s-%s.%s", ARTIFACT_ID, version,  type);
    }

}
