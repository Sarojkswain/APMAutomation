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
public class MemoryMonitorWebappArtifact extends AbstractTasArtifactFactory {
    private static final String GROUP_ID = "com.ca.apm.systemtest";
    private static final String ARTIFACT_ID = "memory-monitor-webapp";
    private static final TasExtension EXT = TasExtension.WAR;

    /**
     * @param groupId
     * @param artifactId
     * @param extension
     * @param classifier
     * @param resolver
     */
    public MemoryMonitorWebappArtifact(ITasResolver resolver) {
        super(GROUP_ID, ARTIFACT_ID, EXT, null, resolver);
    }

}
