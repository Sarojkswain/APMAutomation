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
public class EmClient14Artifact extends AbstractTasArtifactFactory {

    /**
     * @param resolver
     */
    public EmClient14Artifact(ITasResolver resolver) {
        super("com.ca.apm.common", "com.wily.introscope.em.client14", IBuiltArtifact.TasExtension.JAR, null, resolver);
    }

}
