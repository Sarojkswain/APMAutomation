package com.ca.apm.systemtest.fld.artifact.thirdparty;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.jetbrains.annotations.NotNull;

import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.artifact.IThirdPartyArtifact;

public enum TasTestsCoreVersion implements IThirdPartyArtifact {

    AQUARIUS_99_99_SNAPSHOT("99.99.aquarius-SNAPSHOT", TasExtension.JAR, "jar-with-dependencies");

    private static final String GROUP_ID = "com.ca.apm.systemtest.fld";
    private static final String ARTIFACT_ID = "tas-tests-core";

    private final DefaultArtifact artifact;
    private final String version;
    private final TasExtension type;
    private final String classifier;

    TasTestsCoreVersion(String version, TasExtension type) {
        this(version, type, null);
    }

    TasTestsCoreVersion(String version, TasExtension type, String classifier) {
        this.version = version;
        this.type = type;
        this.classifier = classifier;
        this.artifact =
            new DefaultArtifact(GROUP_ID, ARTIFACT_ID, this.classifier, this.type.getValue(),
                this.version);
    }

    @Override
    public Artifact getArtifact() {
        return new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(),
            artifact.getClassifier(), artifact.getExtension(), artifact.getVersion());
    }

    @Override
    public String getFilename() {
        return (classifier == null) ? String.format("%s-%s.%s", ARTIFACT_ID, version,
            type.getValue()) : String.format("%s-%s-%s.%s", ARTIFACT_ID, version, classifier,
            type.getValue());
    }

    @NotNull
    public String getVersion() {
        return version;
    }

}
