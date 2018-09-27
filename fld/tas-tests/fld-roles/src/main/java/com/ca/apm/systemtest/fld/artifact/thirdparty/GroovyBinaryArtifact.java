/**
 * 
 */
package com.ca.apm.systemtest.fld.artifact.thirdparty;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.artifact.IThirdPartyArtifact;

/**
 * @author keyja01
 *
 */
public enum  GroovyBinaryArtifact implements IThirdPartyArtifact {
    v2_4_6("2.4.6");
    private String version;
    private static final String GROUP_ID = "org.codehaus.groovy";
    private static final String ARTIFACT_ID = "groovy-binary";
    private static final TasExtension type = TasExtension.ZIP;
    

    private GroovyBinaryArtifact(String version) {
        this.version = version;
    }
    
    @Override
    public Artifact getArtifact() {
        return new DefaultArtifact(GROUP_ID, ARTIFACT_ID, null, type.getValue(), version);
    }

    @Override
    public String getFilename() {
        return String.format("%s-%s.%s", ARTIFACT_ID, version, type);
    }
}
