package com.ca.apm.tests.artifact;

import static com.ca.tas.artifact.IBuiltArtifact.TasExtension.TGZ;

import com.ca.tas.artifact.IArtifactExtension;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.ITasArtifactFactory;
import com.ca.tas.artifact.TasArtifact;
import com.ca.tas.resolver.ITasResolver;

/**
 * HelloWorldAppArtifact class represents artifact for hello-world app
 * 
 * @author sinka08
 *
 */
public class HelloWorldAppArtifact implements ITasArtifactFactory {
	private static final String GROUP_ID = "com.ca.apm.nodejs.test-apps";
	private static final String ARTIFACT_ID = "hello-world";
	private static final String VERSION = "0.1.0";
	@SuppressWarnings("unused")
    private final ITasResolver resolver;

	public HelloWorldAppArtifact(ITasResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public ITasArtifact createArtifact() {
		return createArtifact(null);
	}

	private IArtifactExtension getExtension() {
		return TGZ;
	}

	@Override
	public ITasArtifact createArtifact(String version) {
		return new TasArtifact.Builder(ARTIFACT_ID)
		        .version((version == null) ? VERSION : version)
		        .extension(getExtension()).groupId(GROUP_ID).build();
	}

}
