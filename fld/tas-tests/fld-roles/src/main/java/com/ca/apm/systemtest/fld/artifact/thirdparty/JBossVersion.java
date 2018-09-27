package com.ca.apm.systemtest.fld.artifact.thirdparty;

import com.ca.tas.artifact.IThirdPartyArtifact;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import static com.ca.tas.artifact.IBuiltArtifact.TasExtension;

/**
 * JBoss artifact.
 * @author Filip, Jan (filja01)
 */
public enum JBossVersion implements IThirdPartyArtifact {

    v711("7.1.1.Final");

    private static final String GROUP_ID = "com.ca.apm.binaries.redhat";
    private static final String ARTIFACT_ID = "jboss-as";
    private static final String CLASSIFIER = null;
    private final String version;

    // optional fields
    private final TasExtension type = TasExtension.ZIP;

    JBossVersion(String version) {
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
