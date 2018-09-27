/**
 * 
 */
package com.ca.apm.tests.artifact;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.tas.artifact.IThirdPartyArtifact;
import com.ca.tas.artifact.IBuiltArtifact.TasExtension;

/**
 * @author keyja01
 *
 */
public enum JmeterScriptsVersion implements IThirdPartyArtifact {
    v10_3("10.3");

    private String version;
    private static final String GROUP_ID = "com.ca.apm.systemtest.fld";
    private static final String ARTIFACT_ID = "jmeter-scripts";
    private final static TasExtension type = TasExtension.ZIP;

    private JmeterScriptsVersion(String version) {
        this.version = version;
    }
    
    @Override
    public Artifact getArtifact() {
        return new DefaultArtifact(GROUP_ID, ARTIFACT_ID, null, type.getValue(), version);
    }

    @Override
    public String getFilename() {
        return String.format("%s-%s.%s", ARTIFACT_ID, version,  type);
    }

}
