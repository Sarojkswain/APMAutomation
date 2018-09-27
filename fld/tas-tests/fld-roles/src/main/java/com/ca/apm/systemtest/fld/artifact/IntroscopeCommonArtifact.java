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
public class IntroscopeCommonArtifact extends AbstractTasArtifactFactory implements ITasArtifactFactory {
    public IntroscopeCommonArtifact(ITasResolver resolver) {
        super("com.ca.apm.common", "com.wily.introscope.common", IBuiltArtifact.TasExtension.JAR, null, resolver);
    }
}
