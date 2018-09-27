package com.ca.apm.tests.artifact;

import static com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform.WINDOWS;
import static com.ca.tas.artifact.IBuiltArtifact.TasExtension.TAR_GZ;
import static com.ca.tas.artifact.IBuiltArtifact.TasExtension.ZIP;

import java.util.EnumSet;

import com.ca.apm.tests.artifact.CollectorAgentArtifact.Runtime;
import com.ca.tas.artifact.IArtifactExtension;
import com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.ITasArtifactFactory;
import com.ca.tas.artifact.TasArtifact;
import com.ca.tas.resolver.ITasResolver;

/**
 * 
 * @author Dhruv Mevada (mevdh01)
 *
 */
public class UMAgentArtifact implements ITasArtifactFactory 
{
    private static final String GROUP_ID = "com.ca.apm.delivery";
    private static final String ARTIFACT_ID = "APM-Infrastructure-Agent";
    
    private final ArtifactPlatform platform;
    private final ITasResolver resolver;
    
    public UMAgentArtifact(ArtifactPlatform platform, ITasResolver resolver) 
    {
        this.resolver = resolver;
        this.platform = platform;
    }
    
    @Override
    public ITasArtifact createArtifact() {
        return createArtifact(null);
    }

    private IArtifactExtension getExtension() {
        return EnumSet.of(WINDOWS).contains(platform) ? ZIP : TAR_GZ;
    }

    private String getClassifier() {
        return platform.toString().toLowerCase();
    }

    @Override
    public ITasArtifact createArtifact(String version) {
        return new TasArtifact.Builder(ARTIFACT_ID)
        .version((version == null) ? resolver.getDefaultVersion() : version)
        .extension(getExtension())
        .classifier(getClassifier())
        .groupId(GROUP_ID).build();
    }
}
