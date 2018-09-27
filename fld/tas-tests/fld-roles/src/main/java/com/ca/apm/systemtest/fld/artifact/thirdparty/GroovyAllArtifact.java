package com.ca.apm.systemtest.fld.artifact.thirdparty;

import org.apache.commons.lang3.StringUtils;

import com.ca.tas.artifact.IBuiltArtifact;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.ITasArtifactFactory;
import com.ca.tas.artifact.TasArtifact;

/**
 * jMeter extension for FLEX testing artifact.
 *
 * @author Haisman, Vaclav (haiva01)
 */
public class GroovyAllArtifact implements ITasArtifactFactory {
    public  static final String GROUP_ID = "org.codehaus.groovy";
    public  static final String ARTIFACT_ID = "groovy-all";
    public static final IBuiltArtifact.TasExtension EXTENSION = IBuiltArtifact.TasExtension.JAR;
    public static final String DEFAULT_VERSION = "2.4.5";

    private boolean indyClassifier = true;

    public GroovyAllArtifact indy(boolean useIndyClassifier) {
        indyClassifier = useIndyClassifier;
        return this;
    }

    @Override
    public ITasArtifact createArtifact(String version) {
        TasArtifact.Builder builder = new TasArtifact.Builder(ARTIFACT_ID)
            .groupId(GROUP_ID)
            .version(StringUtils.isBlank(version) ? DEFAULT_VERSION : version)
            .extension(EXTENSION);

        if (indyClassifier) {
            builder.classifier("indy");
        }

        return builder.build();
    }

    @Override
    public ITasArtifact createArtifact() {
        return createArtifact(null);
    }
}
