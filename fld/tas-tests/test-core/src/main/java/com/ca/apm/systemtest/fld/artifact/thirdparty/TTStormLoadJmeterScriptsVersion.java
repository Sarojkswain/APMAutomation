package com.ca.apm.systemtest.fld.artifact.thirdparty;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.tas.artifact.IThirdPartyArtifact;
import com.ca.tas.artifact.IBuiltArtifact.TasExtension;

/**
 * Artifact version of JMeter scripts used for transaction trace storm load.
 *  
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public enum TTStormLoadJmeterScriptsVersion implements IThirdPartyArtifact {

    v10_5_1("10.5.1");

    private String version;
    private static final String GROUP_ID = "com.ca.apm.systemtest.fld";
    private static final String ARTIFACT_ID = "tt-storm-load-jmeter-scripts";
    private final static TasExtension type = TasExtension.ZIP;

    private TTStormLoadJmeterScriptsVersion(String version) {
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
