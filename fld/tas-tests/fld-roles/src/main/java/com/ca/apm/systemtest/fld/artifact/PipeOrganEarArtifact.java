/**
 * 
 */
package com.ca.apm.systemtest.fld.artifact;

import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.resolver.ITasResolver;

/**
 * @author keyja01
 *
 */
public class PipeOrganEarArtifact extends AbstractTasArtifactFactory {

    /**
     * @param resolver
     */
    public PipeOrganEarArtifact(ITasResolver resolver) {
        super("com.ca.apm.coda-projects.test-tools.pipeorgan", "pipeorgan_web", TasExtension.WAR, null, resolver);
    }

}
