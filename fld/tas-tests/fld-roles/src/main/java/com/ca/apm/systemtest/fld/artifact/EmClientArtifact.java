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
public class EmClientArtifact extends AbstractTasArtifactFactory {

    /**
     * @param resolver
     */
    public EmClientArtifact(ITasResolver resolver) {
        super("com.ca.apm.common", "com.wily.introscope.em.client", IBuiltArtifact.TasExtension.JAR, null, resolver);
    }

}
