/**
 * 
 */
package com.ca.apm.systemtest.fld.artifact;

import com.ca.tas.artifact.IBuiltArtifact;
import com.ca.tas.resolver.ITasResolver;

/**
 * @author keyja01
 *
 */
public class IsengardClientArtifact extends AbstractTasArtifactFactory {

    /**
     * @param resolver
     */
    public IsengardClientArtifact(ITasResolver resolver) {
        super("com.ca.apm.common", "com.wily.isengard.client", IBuiltArtifact.TasExtension.JAR, null, resolver);
    }

}
