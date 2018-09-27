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
public class ManagementModulesArtifact extends AbstractTasArtifactFactory {

    /**
     * @param resolver
     */
    public ManagementModulesArtifact(ITasResolver resolver) {
        super("com.ca.apm.systemtest.fld", "management-modules", TasExtension.ZIP, "dist", resolver);
    }

}
