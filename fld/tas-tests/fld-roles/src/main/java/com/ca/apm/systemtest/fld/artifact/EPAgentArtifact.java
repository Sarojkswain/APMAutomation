/**
 * 
 */
package com.ca.apm.systemtest.fld.artifact;

import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.resolver.ITasResolver;

/**
 * Resolves an artifact for the EPAgent jar
 * @author keyja01
 *
 */
public class EPAgentArtifact extends AbstractTasArtifactFactory {

    public EPAgentArtifact(ITasResolver resolver) {
        super("com.ca.apm.agent.EPAgent", "EPAgent", TasExtension.JAR, null, resolver);
    }

}
