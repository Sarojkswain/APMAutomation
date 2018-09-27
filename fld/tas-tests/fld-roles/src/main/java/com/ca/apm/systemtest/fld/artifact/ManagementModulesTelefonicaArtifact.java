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
public class ManagementModulesTelefonicaArtifact extends AbstractTasArtifactFactory {

    /**
     * @param resolver
     */
    public ManagementModulesTelefonicaArtifact(ITasResolver resolver) {
        super("com.ca.apm.systemtest.fld", "management-modules-telefonica", TasExtension.ZIP, "dist", resolver);
    }

}
