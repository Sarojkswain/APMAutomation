package com.ca.apm.siteminder;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.tas.artifact.IThirdPartyArtifact;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.type.Platform;

/**
 * AdminUIVersion class
 * <p/>
 * Supported AdminUI version enumeration
 */

public enum AdminUIVersion implements IThirdPartyArtifact
{

     v125x86w("12.5-cr04", Platform.WINDOWS, Bitness.b32, "/ca-adminui-12.5-cr04-win32.exe"),
     v1252sp01x86w("12.52-sp01", Platform.WINDOWS, Bitness.b32, "/ca-adminui-12.52-sp01-win32.exe");
    
    private static final String SITEMINDER_GROUP_ID = GROUP_ID + ".siteminder";
    private static final String ARTIFACT_ID = "adminui";

    private final DefaultArtifact artifact;
    private final String          version;
    private final String          classifier;
    private final String type       = "zip";
    private final String executable;

    /**
     * Default constructor for enum with specific version passed.
     *
     * @param version Artifact's version
     */
    AdminUIVersion(String version)
    {
        this(version, Platform.WINDOWS, Bitness.b32);
    }

    /**
     * Default constructor for enum with specific version and platform passed.
     *
     * @param version  Artifact's version
     * @param platform Artifact's platform
     */
    AdminUIVersion(String version, Platform platform)
    {
        this(version, platform, Bitness.b32);
    }

    /**
     * Default constructor for enum with specific version and bitness passed.
     *
     * @param version Artifact's version
     * @param bitness 32 vs 64 bitness
     */
    AdminUIVersion(String version, Bitness bitness)
    {
        this(version, Platform.WINDOWS, bitness);
    }
    
    AdminUIVersion(String version, Platform platform, Bitness bitness)
    {
        this(version, platform, bitness, "");
    }

    /**
     * @param version  Artifact's version
     * @param platform Artifact's platform
     * @param bitness  32 vs 64 bitnes
     */
    AdminUIVersion(String version, Platform platform, Bitness bitness, String executable)
    {
        this.version = version;
        this.classifier = platform.toString().toLowerCase().substring(0, 3) + bitness.getBits();
        this.artifact = new DefaultArtifact(SITEMINDER_GROUP_ID, ARTIFACT_ID, this.classifier, this.type, this.version);
        this.executable = executable;
    }

    @Override
    public Artifact getArtifact()
    {
        return new DefaultArtifact(
                artifact.getGroupId(),
                artifact.getArtifactId(),
                artifact.getClassifier(),
                artifact.getExtension(),
                artifact.getVersion()
        );
    }

    @Override
    public String getFilename() {
        return String.format("%s-%s-%s.%s", ARTIFACT_ID, this.classifier, this.version, this.type);
    }

    public String getExecutable() {
        return executable;
    }
}
