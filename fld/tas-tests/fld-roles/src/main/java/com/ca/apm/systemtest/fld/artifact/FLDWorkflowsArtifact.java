/**
 * 
 */
package com.ca.apm.systemtest.fld.artifact;

import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.resolver.ITasResolver;

/**
 * Provides access to the generated fld-workflows artifact
 * @author keyja01
 *
 */
public class FLDWorkflowsArtifact extends AbstractTasArtifactFactory {
    private static final String GROUP_ID = "com.ca.apm.systemtest.fld";
    private static final String ARTIFACT_ID = "fld-workflows";
    private static final TasExtension extension = TasExtension.JAR;
    

    /**
     * @param resolver
     */
    public FLDWorkflowsArtifact(ITasResolver resolver) {
        super(GROUP_ID, ARTIFACT_ID, extension, null, resolver);
    }

}
