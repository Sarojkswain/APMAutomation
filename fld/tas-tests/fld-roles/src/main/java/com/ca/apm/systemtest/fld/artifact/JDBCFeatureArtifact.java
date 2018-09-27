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
public class JDBCFeatureArtifact extends AbstractTasArtifactFactory {

    /**
     * @param resolver
     */
    public JDBCFeatureArtifact(ITasResolver resolver) {
        super("com.ca.apm.em", "com.wily.introscope.jdbc.feature", TasExtension.JAR, null, resolver);
    }

}
