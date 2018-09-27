package com.ca.apm.systemtest.fld.artifact.thirdparty;

import org.apache.commons.lang3.StringUtils;

import com.ca.tas.artifact.IBuiltArtifact;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.ITasArtifactFactory;
import com.ca.tas.artifact.TasArtifact;
import com.ca.tas.resolver.ITasResolver;

/**
 * jMeter extension for FLEX testing artifact.
 *
 * @author Haisman, Vaclav (haiva01)
 */
public class LoremIpsumArtifact implements ITasArtifactFactory {
    public static final String GROUP_ID = "de.sven-jacobs";
    public static final String ARTIFACT_ID = "loremipsum";
    public static final IBuiltArtifact.TasExtension EXTENSION = IBuiltArtifact.TasExtension.JAR;
    public static final String DEFAULT_VERSION = "1.0";

    public LoremIpsumArtifact(ITasResolver resolver) {
    }

    @Override
    public ITasArtifact createArtifact(String version) {
        return new TasArtifact.Builder(ARTIFACT_ID)
            .groupId(GROUP_ID)
            .version(StringUtils.isBlank(version) ? DEFAULT_VERSION : version)
            .extension(EXTENSION).build();
    }

    @Override
    public ITasArtifact createArtifact() {
        return createArtifact(null);
    }
}
