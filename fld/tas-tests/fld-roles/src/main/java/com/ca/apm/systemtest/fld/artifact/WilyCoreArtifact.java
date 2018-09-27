/**
 * 
 */
package com.ca.apm.systemtest.fld.artifact;

import com.ca.tas.artifact.IBuiltArtifact;
import com.ca.tas.artifact.ITasArtifactFactory;
import com.ca.tas.resolver.ITasResolver;

/**
 * @author keyja01
 *
 */
public class WilyCoreArtifact extends AbstractTasArtifactFactory implements ITasArtifactFactory {
    
    public WilyCoreArtifact(ITasResolver resolver) {
        super("com.ca.apm.common", "com.wily.core", IBuiltArtifact.TasExtension.JAR, null, resolver);
    }
}
