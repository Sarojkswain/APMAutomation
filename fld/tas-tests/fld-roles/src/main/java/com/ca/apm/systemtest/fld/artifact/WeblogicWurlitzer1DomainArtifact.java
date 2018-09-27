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
public class WeblogicWurlitzer1DomainArtifact extends AbstractTasArtifactFactory {

    /**
     * @param resolver
     */
    public WeblogicWurlitzer1DomainArtifact(ITasResolver resolver) {
        super("com.ca.apm.systemtest.fld", "wurlitzer1-domain-template", TasExtension.JAR, "wls103", resolver);
    }

}
