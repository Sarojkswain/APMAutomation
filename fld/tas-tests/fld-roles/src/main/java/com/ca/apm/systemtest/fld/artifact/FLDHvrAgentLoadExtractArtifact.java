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
public class FLDHvrAgentLoadExtractArtifact extends AbstractTasArtifactFactory {

    /**
     * @param resolver
     */
    public FLDHvrAgentLoadExtractArtifact(ITasResolver resolver) {
        super("com.ca.apm.systemtest.fld", "hvragent-extract", TasExtension.ZIP, null, resolver);
    }

}
