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
public class JmeterAmfJmeterExtArtifact implements ITasArtifactFactory {
    private static final String GROUP_ID = "com.ca.apm.systemtest.fld.flex";
    private static final String ARTIFACT_ID = "jmeter-amf";
    private static final IBuiltArtifact.TasExtension EXTENSION = IBuiltArtifact.TasExtension.JAR;
    private final ITasResolver resolver;

    public JmeterAmfJmeterExtArtifact(ITasResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public ITasArtifact createArtifact(String version) {
        return new TasArtifact.Builder(ARTIFACT_ID)
            .groupId(GROUP_ID)
            .version(StringUtils.isBlank(version) ? resolver.getDefaultVersion() : version)
            .classifier("jar-with-dependencies")
            .extension(EXTENSION).build();
    }

    @Override
    public ITasArtifact createArtifact() {
        return createArtifact(null);
    }
}
