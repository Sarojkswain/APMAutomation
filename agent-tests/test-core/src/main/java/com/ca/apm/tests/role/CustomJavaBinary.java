package com.ca.apm.tests.role;

import org.apache.commons.lang.StringUtils;
import org.eclipse.aether.artifact.Artifact;

import com.ca.tas.artifact.IBuiltArtifact;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.IThirdPartyArtifact;
import com.ca.tas.artifact.TasArtifact;

public enum CustomJavaBinary implements IThirdPartyArtifact {

    LINUX_64BIT_JDK_17_0_80("jdk.1.7", "1.7.0.80_64", "linux-x64", IBuiltArtifact.TasExtension.TAR_GZ),
    WINDOWS_64BIT_JDK_17_0_80("jdk.1.7", "1.7.0.80_64", "windows-x64", IBuiltArtifact.TasExtension.ZIP),  
    LINUX_64BIT_JDK_18_0_112("jdk.1.8", "1.8.0.112_64", "linux-x64", IBuiltArtifact.TasExtension.TAR_GZ),
    WINDOWS_64BIT_JDK_18_0_112("jdk.1.8", "1.8.0.112_64", "windows-x64", IBuiltArtifact.TasExtension.ZIP),
    WINDOWS_64BIT_JDK_18_0_131("jdk.1.8", "1.8.0.131_64", "windows-x64", IBuiltArtifact.TasExtension.ZIP),
    WINDOWS_64BIT_JDK_16_45("jdk.1.6", "1.6.0.45", "win-x64", IBuiltArtifact.TasExtension.ZIP);

    public enum JavaRuntime {JRE, JDK}

    private static final String GROUP_ID = "com.ca.apm.devtools";

    private final ITasArtifact tasArtifact;

    CustomJavaBinary(String artifactId, String version, String classifier, IBuiltArtifact.TasExtension type) {
        tasArtifact = new TasArtifact.Builder(artifactId)
            .groupId(GROUP_ID)
            .version(version)
            .classifier(classifier)
            .extension(type)
            .build();
    }

    /** {@inheritDoc} */
    @Override
    public String getFilename() {
        Artifact artifact = getArtifact();
        if (StringUtils.isNotEmpty(artifact.getClassifier())) {
            return String.format("%s-%s-%s.%s", artifact.getArtifactId(), artifact.getVersion(), artifact.getClassifier(),
                                 artifact.getExtension());
        }

        return String.format("%s-%s.%s", artifact.getArtifactId(), artifact.getVersion(), artifact.getExtension());

    }

    /** {@inheritDoc} */
    @Override
    public Artifact getArtifact() {
        return tasArtifact.getArtifact();
    }
}
