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
public class TessTestArtifact extends AbstractTasArtifactFactory {

    /**
     * @param groupId
     * @param artifactId
     * @param extension
     * @param classifier
     * @param resolver
     */
    public TessTestArtifact(ITasResolver resolver) {
        super("com.ca.apm.coda-projects.test-tools", "tesstest", TasExtension.WAR, null, resolver);
    }

}
