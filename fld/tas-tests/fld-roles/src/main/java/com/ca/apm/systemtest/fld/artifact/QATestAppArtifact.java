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
public class QATestAppArtifact extends AbstractTasArtifactFactory {
    public QATestAppArtifact(ITasResolver resolver) {
        this(null, resolver);
    }
    
    public QATestAppArtifact(String classifier, ITasResolver resolver) {
        super("com.ca.apm.coda-projects.test-tools", "qatestapp", TasExtension.WAR, classifier, resolver);
    }
}
