package com.ca.apm.systemtest.fld.artifact.thirdparty;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.jetbrains.annotations.NotNull;

import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.artifact.IThirdPartyArtifact;

public enum JMeterVersion implements IThirdPartyArtifact {

    v211("2.11", TasExtension.ZIP), v212("2.12", TasExtension.ZIP), v213("2.13", TasExtension.ZIP);

    private static final String ARTIFACT_ID = "apache-jmeter";

    private final DefaultArtifact artifact;
    private final String version;
    private final TasExtension type;

    JMeterVersion(String version, TasExtension type) {
        this.version = version;
        this.type = type;
        this.artifact =
            new DefaultArtifact(GROUP_ID, ARTIFACT_ID, this.type.getValue(), this.version);
    }

    @Override
    public Artifact getArtifact() {
        return new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(),
            artifact.getClassifier(), artifact.getExtension(), artifact.getVersion());
    }

    @Override
    public String getFilename() {
        return String.format("%s-%s.%s", ARTIFACT_ID, this.version, this.type.getValue());
    }

    @NotNull
    public String getVersion() {
        return version;
    }

}
