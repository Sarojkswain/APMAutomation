/**
 * 
 */
package com.ca.apm.systemtest.fld.artifact;

import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.resolver.ITasResolver;

/**
 * @author KEYJA01
 *
 */
public class MetricSynthArtifact extends AbstractTasArtifactFactory {

    /**
     * @param resolver
     */
    public MetricSynthArtifact(ITasResolver resolver) {
        super("com.ca.apm.testing", "metric-synth", TasExtension.JAR, "exec", resolver);
    }

}
