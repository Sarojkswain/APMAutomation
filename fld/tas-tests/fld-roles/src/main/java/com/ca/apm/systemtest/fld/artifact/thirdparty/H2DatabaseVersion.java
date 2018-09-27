/**
 * 
 */
package com.ca.apm.systemtest.fld.artifact.thirdparty;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.tas.artifact.IThirdPartyArtifact;

/**
 * @author keyja01
 *
 */
public enum H2DatabaseVersion implements IThirdPartyArtifact {
    v1_4_196("1.4.196");
    
    private String version;
    
    /**
     * @param version
     */
    private H2DatabaseVersion(String version) {
        this.version = version;
    }

    @Override
    public Artifact getArtifact() {
        return new DefaultArtifact("com.h2database", "h2", "jar", version);
    }

    @Override
    public String getFilename() {
        return String.format("h2-%s.jar", version);
    }

}
