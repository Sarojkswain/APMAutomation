package com.ca.apm.systemtest.fld.artifact.thirdparty;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.tas.artifact.ITasArtifact;

/**
 * @author haiva01
 */
public enum MavenDistributionArtifact implements ITasArtifact {
    v3_3_3_Zip("3.3.3", "bin", "zip"),
    v3_3_3_TarGz("3.3.3", "bin", "tar.gz"),

    v3_3_9_Zip("3.3.9", "bin", "zip"),
    v3_3_9_TarGz("3.3.9", "bin", "tar.gz"),
    ;

    public static final String GROUP_ID = "org.apache.maven";
    public static final String ARTIFACT_ID = "apache-maven";

    private String version;
    private String type;
    private String classifier;

    MavenDistributionArtifact(String version, String classifier, String type) {
        this.version = version;
        this.classifier = classifier;
        this.type = type;
    }

    @Override
    public Artifact getArtifact() {
        return new DefaultArtifact(GROUP_ID, ARTIFACT_ID, classifier, type, version);
    }
}
