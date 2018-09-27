package com.ca.apm.systemtest.fld.artifact.thirdparty;

import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.artifact.IThirdPartyArtifact;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

/**
 * @Author rsssa02
 */
public enum MessageBrokerVersion implements IThirdPartyArtifact {
    VER_7("message-broker","7.0", "", TasExtension.ZIP, "MQParms.exe");

    public String artifactName;
    public String version;
    public String type;
    public String installerFileName;
    public String classifier;
    public DefaultArtifact artifact;

    public String getInstallerFileName() {
        return installerFileName;
    }


    MessageBrokerVersion(String artifactName, String version, String classifier, TasExtension type, String exeFileName){
        this.artifactName = artifactName;
        this.version = version;
        this.classifier = classifier;
        this.type = type.toString();
        this.installerFileName = exeFileName;
        this.artifact = new DefaultArtifact("com.ca.apm.binaries.ibm", this.artifactName , this.classifier, this.type, this.version);
    }

    @Override
    public String getFilename() {
        Artifact artifact = getArtifact();
        return String
                .format("%s-%s-%s.%s", artifact.getArtifactId(), artifact.getVersion(), artifact.getExtension());
    }

    @Override
    public Artifact getArtifact() {
        return new DefaultArtifact(this.artifact.getGroupId(), this.artifact.getArtifactId(), this.artifact.getClassifier(), this.artifact.getExtension(), this.artifact.getVersion());
    }
}
