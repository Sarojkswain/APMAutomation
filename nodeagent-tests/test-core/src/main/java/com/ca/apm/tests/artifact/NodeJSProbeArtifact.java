package com.ca.apm.tests.artifact;

import static com.ca.tas.artifact.IBuiltArtifact.TasExtension.TGZ;

import com.ca.tas.artifact.IArtifactExtension;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.ITasArtifactFactory;
import com.ca.tas.artifact.TasArtifact;
import com.ca.tas.resolver.ITasResolver;

/**
 * NodeJSProbeArtifact class represents artifact for nodeJS probe
 * 
 * @author sinka08
 *
 */
public class NodeJSProbeArtifact implements ITasArtifactFactory {
	private static final String GROUP_ID = "com.ca.apm.nodejs";
	private static final String ARTIFACT_ID = "nodejs-probe";
	private final ITasResolver resolver;

	public NodeJSProbeArtifact(ITasResolver resolver) {
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
		        .version((version == null) ? resolver.getDefaultVersion() : version)
		        .extension(getExtension()).groupId(GROUP_ID).build();
	}

}
