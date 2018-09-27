/**
 * 
 */
package com.ca.apm.systemtest.fld.artifact.thirdparty;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.artifact.IThirdPartyArtifact;

/**
 * Versions for the WeblogicWurlitzer domain template archives.
 * @author keyja01
 *
 */
public enum WeblogicWurlitzer1DomainTemplateVersion implements IThirdPartyArtifact {
    domain1v1_0("wurlitzer1-domain-template", "1.0"),
    domain1v1_0_1("wurlitzer1-domain-template", "1.0.1"),
    domain1v1_0_2("wurlitzer1-domain-template", "1.0.2"),
    domain2v1_0("wurlitzer2-domain-template", "1.0"),
    domain2v1_0_1("wurlitzer2-domain-template", "1.0.1"),
    domain2v1_0_2("wurlitzer2-domain-template", "1.0.2"),
    domain2v1_0_3("wurlitzer2-domain-template", "1.0.3"),
    domain2v1_0_4("wurlitzer2-domain-template", "1.0.4");

    private String version;
    private String artifactId;
    private static final String GROUP_ID = "com.ca.apm.systemtest.fld";
    private static final String CLASSIFIER = "wls103";
    private static final String type = TasExtension.JAR.getValue();
    
    private WeblogicWurlitzer1DomainTemplateVersion(String artifactId, String version) {
        this.artifactId = artifactId;
        this.version = version;
    }

    @Override
    public Artifact getArtifact() {
        return new DefaultArtifact(GROUP_ID, artifactId, CLASSIFIER, type, version);    
    }

    @Override
    public String getFilename() {
        return String.format("%s-%s-%s.%s", artifactId, version, CLASSIFIER, type);
    }
    
    
}
