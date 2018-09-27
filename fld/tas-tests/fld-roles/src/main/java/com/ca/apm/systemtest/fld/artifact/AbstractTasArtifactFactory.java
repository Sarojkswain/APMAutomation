/**
 * 
 */
package com.ca.apm.systemtest.fld.artifact;

import com.ca.tas.artifact.IBuiltArtifact;
import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.ITasArtifactFactory;
import com.ca.tas.artifact.TasArtifact;
import com.ca.tas.artifact.TasArtifact.Builder;
import com.ca.tas.resolver.ITasResolver;

/**
 * Convenience class to cut down on boilerplate code creating artifacts
 * @author keyja01
 *
 */
public abstract class AbstractTasArtifactFactory implements ITasArtifactFactory {
    protected String groupId;
    protected String artifactId;
    private String classifier;
    protected IBuiltArtifact.TasExtension extension;
    private ITasResolver resolver;
    
    

    public AbstractTasArtifactFactory(String groupId, String artifactId, TasExtension extension, 
            String classifier, ITasResolver resolver) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.extension = extension;
        this.resolver = resolver;
        this.classifier = classifier;
    }

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
        Builder builder = new TasArtifact.Builder(artifactId)
            .groupId(groupId)
            .version(version == null ? resolver.getDefaultVersion() : version)
            .extension(extension);
        if (classifier != null) {
            builder.classifier(classifier);
        }
        return builder.build();
    }

}
