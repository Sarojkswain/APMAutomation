/**
 * 
 */
package com.ca.apm.systemtest.fld.artifact;

import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.resolver.ITasResolver;

/**
 * Artifact factory for CLWorkstation.jar
 * @author KEYJA01
 *
 */
public class CLWorkstationArtifact extends AbstractTasArtifactFactory {

    public CLWorkstationArtifact(ITasResolver resolver) {
        super("com.ca.apm.em", "com.wily.introscope.clw.feature", TasExtension.JAR, null, resolver);
    }

}
