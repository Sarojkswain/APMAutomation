/**
 * 
 */
package com.ca.apm.systemtest.fld.artifact.thirdparty;

import com.ca.apm.systemtest.fld.artifact.AbstractTasArtifactFactory;
import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.resolver.ITasResolver;

/**
 * @author keyja01
 *
 */
public class BrtmTestAppArtifact extends AbstractTasArtifactFactory {

    /**
     * @param groupId
     * @param artifactId
     * @param extension
     * @param classifier
     * @param resolver
     */
    public BrtmTestAppArtifact(ITasResolver resolver) {
        super("com.ca.apm.coda-projects.test-tools", "brtmtestapp", TasExtension.WAR, null, resolver);
    }

}
