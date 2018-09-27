/**
 * 
 */
package com.ca.apm.systemtest.fld.artifact.metricsynth;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.tas.artifact.IThirdPartyArtifact;

/**
 * @author keyja01
 *
 */
public enum BasicScenario001Artifact implements IThirdPartyArtifact {
    Version1_0("1.0"), Version1_0_2cities("1.0", "2cities");
    
    private String version;
    private String classifier;
    

    /**
     * @param tasResolver
     */
    private BasicScenario001Artifact(String version) {
        this(version, null);
    }
    

    /**
     * @param tasResolver
     */
    private BasicScenario001Artifact(String version, String classifier) {
        this.version = version;
        this.classifier = classifier;
    }

    @Override
    public Artifact getArtifact() {
        if (classifier != null) {
            return new DefaultArtifact("com.ca.apm.testing.metricsynth.scenario", "basic-scenario-1", classifier, "zip", version);
        }
        return new DefaultArtifact("com.ca.apm.testing.metricsynth.scenario", "basic-scenario-1", "zip", version);
    }

    @Override
    public String getFilename() {
        String filename = String.format("basic-scenario-1-%s.zip", version);
        return filename;
    }
}
