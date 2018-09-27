package com.ca.apm.systemtest.fld.artifact;

import com.ca.tas.artifact.IBuiltArtifact;
import com.ca.tas.resolver.ITasResolver;

/**
 * Created by haiva01 on 26.2.2016.
 */
public class Win32HelperAgentArtifact extends AbstractTasArtifactFactory {
    private static final String GROUP_ID = "com.ca.apm.systemtest.fld";
    private static final String ARTIFACT_ID = "win32-helper-java-agent";
    private static final IBuiltArtifact.TasExtension EXTENSION = IBuiltArtifact.TasExtension.JAR;
    private static final String CLASSIFIER = "jar-with-dependencies";

    public Win32HelperAgentArtifact(ITasResolver resolver) {
        super(GROUP_ID, ARTIFACT_ID, EXTENSION, CLASSIFIER, resolver);
    }
}
