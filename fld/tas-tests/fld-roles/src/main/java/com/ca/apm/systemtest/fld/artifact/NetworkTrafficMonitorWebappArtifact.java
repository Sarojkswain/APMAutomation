package com.ca.apm.systemtest.fld.artifact;

import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.resolver.ITasResolver;

/**
 * @author bocto01
 *
 */
public class NetworkTrafficMonitorWebappArtifact extends AbstractTasArtifactFactory {

    private static final String GROUP_ID = "com.ca.apm.systemtest";
    private static final String ARTIFACT_ID = "network-traffic-monitor-webapp";
    private static final TasExtension EXT = TasExtension.WAR;

    /**
     * @param groupId
     * @param artifactId
     * @param extension
     * @param classifier
     * @param resolver
     */
    public NetworkTrafficMonitorWebappArtifact(ITasResolver resolver) {
        super(GROUP_ID, ARTIFACT_ID, EXT, null, resolver);
    }

}
