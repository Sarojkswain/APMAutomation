package com.ca.apm.tests.artifact;

import static com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform.WINDOWS;
import static com.ca.tas.artifact.IBuiltArtifact.TasExtension.TAR_GZ;
import static com.ca.tas.artifact.IBuiltArtifact.TasExtension.ZIP;

import java.util.EnumSet;

import com.ca.tas.artifact.IArtifactExtension;
import com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.ITasArtifactFactory;
import com.ca.tas.artifact.TasArtifact;
import com.ca.tas.resolver.ITasResolver;

/**
 * CollectorAgentArtifact class represents non-installer artifact for Collector Agent
 * 
 * @author sinka08
 *
 */
public class CollectorAgentArtifact implements ITasArtifactFactory {
	private static final String GROUP_ID = "com.ca.apm.agent.CollectorAgent";
	private static final String ARTIFACT_ID = "CollectorAgent-dist";
	private final ArtifactPlatform platform;
	private final ITasResolver resolver;
	@SuppressWarnings("unused")
    private final Runtime runtime;

	public enum Runtime {
		NODEJS("node"), PHP;
		private final String name;

		Runtime() {
			this.name = name().toLowerCase();
		}

		Runtime(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	};

	public CollectorAgentArtifact(ArtifactPlatform platform, ITasResolver resolver,
	        Runtime runtime) {
		this.resolver = resolver;
		this.platform = platform;
		this.runtime = runtime;
	}

	@Override
	public ITasArtifact createArtifact() {
		return createArtifact(null);

	}

	private IArtifactExtension getExtension() {
		return EnumSet.of(WINDOWS).contains(platform) ? ZIP : TAR_GZ;
	}

	private String getClassifier() {
		//return this.runtime.toString() + "-" + platform.toString();
		return platform.toString();
	}

	@Override
	public ITasArtifact createArtifact(String version) {
		return new TasArtifact.Builder(ARTIFACT_ID)
				.version((version == null) ? resolver.getDefaultVersion() : version)
				.extension(getExtension()).classifier(getClassifier()).groupId(GROUP_ID).build();
	}

}
