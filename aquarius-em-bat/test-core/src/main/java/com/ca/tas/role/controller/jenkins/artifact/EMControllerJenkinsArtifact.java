package com.ca.tas.role.controller.jenkins.artifact;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.thirdParty.ICodaControllerArtifact;

public enum EMControllerJenkinsArtifact implements ICodaControllerArtifact {

    v4_0_13_SNAPSHOT("4.0.13-SNAPSHOT");

    //mandatory fields
    private final DefaultArtifact artifact;
    private final String          version;
    //optional fields
    private static final String GROUP_ID    = "com.ca.coda.hudson.plugins";
    private static final String ARTIFACT_ID = "coda-jenkins-dist";
    private final        String type        = "war";
    private final        String classifier  = "dist";

    /**
     * @param version Artifact's version
     */
    EMControllerJenkinsArtifact(String version)
    {
        this.version = version;
        this.artifact = new DefaultArtifact(GROUP_ID, ARTIFACT_ID, this.classifier, this.type, this.version);
    }

    @Override
    public String getFilename()
    {
        return String.format("%s-%s-%s.%s", ARTIFACT_ID, this.version, this.classifier, this.type);
    }

	public Artifact getArtifact() {
        return getDefaultArtifact();
	}

	public DefaultArtifact getDefaultArtifact() {
        return new DefaultArtifact(
                this.artifact.getGroupId(),
                this.artifact.getArtifactId(),
                this.artifact.getClassifier(),
                this.artifact.getExtension(),
                this.artifact.getVersion()
        );
	}

	@Override
	public ITasArtifact getJenkinsStubArtifact() {
		// TODO Auto-generated method stub
		return null;
	}

}
