/**
 * 
 */
package com.ca.apm.tests.artifact;

import com.ca.tas.artifact.IBuiltArtifact;
import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.ITasArtifactFactory;
import com.ca.tas.artifact.TasArtifact;
import com.ca.tas.resolver.ITasResolver;

/**
 * @author jirji01
 *
 */
public class MemoryMonitorWebappArtifact implements ITasArtifactFactory {
    /**
     * @param resolver
     */
    public MemoryMonitorWebappArtifact(ITasResolver resolver) {
        this.resolver = resolver;
    }

    protected String groupId = "com.ca.apm.systemtest";
    protected String artifactId = "memory-monitor-webapp";
    protected IBuiltArtifact.TasExtension extension = TasExtension.WAR;
    private ITasResolver resolver;

    /* (non-Javadoc)
     * @see com.ca.tas.artifact.ITasArtifactFactory#createArtifact()
     */
    @Override
    public ITasArtifact createArtifact() {
        return createArtifact(null);
    }

    /* (non-Javadoc)
     * @see com.ca.tas.artifact.ITasArtifactFactory#createArtifact(java.lang.String)
     */
    @Override
    public ITasArtifact createArtifact(String version) {
        TasArtifact.Builder builder = new TasArtifact.Builder(artifactId)
                .groupId(groupId)
                .version(version == null ? resolver.getDefaultVersion() : version)
                .extension(extension);
        return builder.build();
    }

}
