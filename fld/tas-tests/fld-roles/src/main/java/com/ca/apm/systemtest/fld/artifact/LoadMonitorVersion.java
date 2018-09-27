/**
 * 
 */
package com.ca.apm.systemtest.fld.artifact;

import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.ITasArtifactFactory;
import com.ca.tas.artifact.TasArtifact;
import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.resolver.ITasResolver;

/**
 * @author keyja01
 *
 */
public class LoadMonitorVersion implements ITasArtifactFactory {
    private ITasResolver resolver;

    /**
     * 
     */
    public LoadMonitorVersion(ITasResolver resolver) {
        this.resolver = resolver;
    }

    /* (non-Javadoc)
     * @see com.ca.tas.artifact.ITasArtifactFactory#createArtifact(java.lang.String)
     */
    @Override
    public ITasArtifact createArtifact(String version) {
        if (version == null) {
            version = resolver.getDefaultVersion();
        }
        return new TasArtifact.Builder("load-monitor")
            .extension(TasExtension.WAR)
            .version(version)
            .groupId("com.ca.apm.systemtest")
            .build();
    }

    /* (non-Javadoc)
     * @see com.ca.tas.artifact.ITasArtifactFactory#createArtifact()
     */
    @Override
    public ITasArtifact createArtifact() {
        return createArtifact(null);
    }

}
