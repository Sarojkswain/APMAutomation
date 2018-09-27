package com.ca.apm.systemtest.fld.artifact.thirdparty;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.tas.artifact.ITasArtifact;

/**
 * @author haiva01
 */
public enum WebSphereLibertyArtifact implements ITasArtifact {

    v8_5_5_9_javaee7("8.5.5.9", "javaee7", "zip"),
    v8_5_5_9_webProfile7("8.5.5.9", "webProfile7", "zip")
    ;

    public static final String GROUP_ID = "com.ca.apm.binaries.websphere.liberty";
    public static final String ARTIFACT_ID = "wlp";

    private String version;
    private String type;
    private String classifier;

    WebSphereLibertyArtifact(String version, String classifier, String type) {
        this.version = version;
        this.classifier = classifier;
        this.type = type;
    }

    @Override
    public Artifact getArtifact() {
        return new DefaultArtifact(GROUP_ID, ARTIFACT_ID, classifier, type, version);
    }

}
