package com.ca.apm.systemtest.fld.artifact.thirdparty;

import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.artifact.IThirdPartyArtifact;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

/**
 * @Author rsssa02
 */
public enum OSBVersion implements IThirdPartyArtifact {
    VER_11G("osb", "11.1.1.7","generic", TasExtension.ZIP, "setup.exe");

    private static final String GROUP_ID = IThirdPartyArtifact.GROUP_ID;
    private final String setupInstallerName;
    private final DefaultArtifact artifact;
    private final String version;
    private final String classifier;
    private final String type;
    private final String artifactName;


    public String getSetupInstallerName() {
        return setupInstallerName;
    }

    OSBVersion(String artifactName, String version, String classifier, TasExtension zip, String setupInstallerName) {
        this.setupInstallerName = setupInstallerName;
        this.classifier = classifier;
        this.type = zip.toString();
        this.version = version;
        this.artifactName = artifactName;
        this.artifact = new DefaultArtifact("com.ca.apm.binaries", this.artifactName , this.classifier, this.type, this.version);
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
