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
public class WeblogicWurlitzer2DomainArtifact extends AbstractTasArtifactFactory {

    /**
     * @param resolver
     */
    public WeblogicWurlitzer2DomainArtifact(ITasResolver resolver) {
        super("com.ca.apm.systemtest.fld", "wurlitzer2-domain-template", TasExtension.JAR, "wls103", resolver);
    }

}
