package com.ca.apm.systemtest.fld.artifact;

import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.resolver.ITasResolver;

/**
 * 
 * Artifact coordinates describing a test web application for generating transaction trace storm load.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 * 
 */
public class TTStormerWebAppArtifact extends AbstractTasArtifactFactory {

    /**
     * Constructor.
     * 
     * @param resolver
     */
    public TTStormerWebAppArtifact(ITasResolver resolver) {
        super("com.ca.apm.systemtest.ttstormload", "tt-stormer", TasExtension.WAR, null, resolver);
    }

}
