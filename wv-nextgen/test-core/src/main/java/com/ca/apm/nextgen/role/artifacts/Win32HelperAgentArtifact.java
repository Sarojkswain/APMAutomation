package com.ca.apm.nextgen.role.artifacts;

import com.ca.tas.artifact.IBuiltArtifact;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.ITasArtifactFactory;
import com.ca.tas.artifact.TasArtifact;
import com.ca.tas.resolver.ITasResolver;

/**
 * Created by haiva01 on 26.2.2016.
 */
public class Win32HelperAgentArtifact implements ITasArtifactFactory {
    private static final String GROUP_ID = "com.ca.apm.systemtest.fld";
    private static final String ARTIFACT_ID = "win32-helper-java-agent";
    private static final IBuiltArtifact.TasExtension EXTENSION = IBuiltArtifact.TasExtension.JAR;
    private static final String CLASSIFIER = "jar-with-dependencies";

    private final ITasResolver resolver;

    public Win32HelperAgentArtifact(ITasResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public ITasArtifact createArtifact(String version) {
        return new TasArtifact.Builder(ARTIFACT_ID)
            .groupId(GROUP_ID)
            .extension(EXTENSION)
            .classifier(CLASSIFIER)
            .version(version == null ? resolver.getDefaultVersion() : version)
            .build();
    }

    @Override
    public ITasArtifact createArtifact() {
        return null;
    }
}
