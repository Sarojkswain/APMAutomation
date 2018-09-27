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
public class WurlitzerWebAppArtifact extends AbstractTasArtifactFactory {

    /**
     * @param resolver
     */
    public WurlitzerWebAppArtifact(ITasResolver resolver) {
        super("com.ca.apm.coda-projects.test-projects", "Wurlitzer", TasExtension.WAR, "noejb", resolver);
    }

}
