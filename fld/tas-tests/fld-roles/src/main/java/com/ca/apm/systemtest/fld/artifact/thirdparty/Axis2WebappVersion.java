package com.ca.apm.systemtest.fld.artifact.thirdparty;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.artifact.IThirdPartyArtifact;

/**
 * axis2-webapp artifact version.
 *
 * @author bocto01
 */
public enum Axis2WebappVersion implements IThirdPartyArtifact {

    v154("1.5.4");

    private static final String GROUP_ID = "org.apache.axis2";
    private static final String ARTIFACT_ID = "axis2-webapp";

    private final String version;

    private final TasExtension type = TasExtension.WAR;

    private Axis2WebappVersion(String version) {
        this.version = version;
    }

    @Override
    public Artifact getArtifact() {
        return new DefaultArtifact(GROUP_ID, ARTIFACT_ID, type.getValue(), version);
    }

    @Override
    public String getFilename() {
        return String.format("%s-%s.%s", ARTIFACT_ID, version, type);
    }

}
