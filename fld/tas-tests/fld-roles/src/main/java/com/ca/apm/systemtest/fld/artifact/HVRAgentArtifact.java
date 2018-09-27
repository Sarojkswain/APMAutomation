/**
 * 
 */
package com.ca.apm.systemtest.fld.artifact;

import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.artifact.TasArtifact;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.ITasArtifactFactory;
import com.ca.tas.resolver.ITasResolver;

/**
 * @author keyja01
 *
 */
public class HVRAgentArtifact implements ITasArtifactFactory {
    private final ITasResolver resolver;


    public HVRAgentArtifact(ITasResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public ITasArtifact createArtifact(String version) {
        if (version == null) {
            version = resolver.getDefaultVersion();
        }
        return new TasArtifact.Builder("hvragent").groupId("com.ca.apm.coda-projects.test-tools")
            .version(version).classifier("dist").extension(TasExtension.ZIP).build();
    }

    @Override
    public ITasArtifact createArtifact() {
        return createArtifact(null);
    }

}
