/**
 * 
 */
package com.ca.apm.systemtest.fld.artifact.thirdparty;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.artifact.IThirdPartyArtifact;

/**
 * @author KEYJA01
 *
 */
public enum HelloWorld10KVersion implements IThirdPartyArtifact {
    v1_0("1.0");
    
    private static final String ARTIFACT_ID = "HelloWorld10k";
    private String version;

    private HelloWorld10KVersion(String version) {
        this.version = version;
    }

    @Override
    public Artifact getArtifact() {
        return new DefaultArtifact("com.wily.test", ARTIFACT_ID, TasExtension.WAR.getValue(), version);
    }

    @Override
    public String getFilename() {
        return String.format("%s-%s.%s", ARTIFACT_ID, version, TasExtension.WAR);
    }
}
