package com.ca.apm.tests.artifact;

import com.ca.tas.artifact.IThirdPartyArtifact;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.type.Platform;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

/**
 * Created by meler02 on 8/22/2016.
 */
public enum WeblogicVersion implements IThirdPartyArtifact {
    v103x86w("10.3"),
    v1035generic("10.3.5", "generic", "jar"),
    v1035x86linux("10.3.5", "linux-x86", "bin"),
    v1213generic("12.1.3", "generic", "jar");

    private static final String ARTIFACT_ID = "weblogic";
    private final DefaultArtifact artifact;
    private final String version;
    private final String classifier;
    private final String type;

    private WeblogicVersion(String version) {
        this(version, Platform.WINDOWS, Bitness.b32);
    }

    private WeblogicVersion(String version, Platform platform) {
        this(version, platform, Bitness.b32);
    }

    private WeblogicVersion(String version, Bitness bitness) {
        this(version, Platform.WINDOWS, bitness);
    }

    private WeblogicVersion(String version, Platform platform, Bitness bitness) {
        this.version = version;
        this.classifier = platform.toString().toLowerCase() + "-" + bitness.getArchitecture();
        this.type = "exe";
        this.artifact = new DefaultArtifact("com.ca.apm.binaries", "weblogic", this.classifier, this.type, this.version);
    }

    private WeblogicVersion(String version, String classifier, String type) {
        this.version = version;
        this.classifier = classifier;
        this.type = type;
        this.artifact = new DefaultArtifact("com.ca.apm.binaries", "weblogic", this.classifier, this.type, this.version);
    }

    public Artifact getArtifact() {
        return new DefaultArtifact(this.artifact.getGroupId(), this.artifact.getArtifactId(), this.artifact.getClassifier(), this.artifact.getExtension(), this.artifact.getVersion());
    }

    public String getFilename() {
        return String.format("%s-%s-%s.%s", new Object[]{"weblogic", this.version, this.classifier, this.type});
    }
}
