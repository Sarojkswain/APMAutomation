/*
 * Copyright (c) 2014 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.tests.role;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.aether.artifact.Artifact;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.tas.artifact.IArtifactVersion;
import com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform;
import com.ca.tas.artifact.ITasArtifactFactory;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.property.RolePropertyContainer;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.EmRole;

/**
 * AbstractAgentRole class
 *
 * Calls appropriate Agent installer role with given parameters passed in its @
 * {@code DeployAgentNoinstFlowContext}. Uses {@link AbstractBuilder} to
 * configure the installation parameters
 *
 * Sample :
 *
 * <pre>
 * @{@code
 * AgentRole agentRole = new AgentRole.Builder("agent-role").webAppRole(webAppRole).platform(ArtifactPlatform.WINDOWS)
 * .version(Version.SNAPSHOT_PRAGUE_99_99).build();
 * }
 * </pre>
 *
 * @author Jan Pojer (pojja01@ca.com)
 * @since 1.0
 */
public abstract class AbstractAgentRole extends AbstractRole {

	protected AbstractAgentRole(AbstractBuilder<?, ?> b) {
		super(b.roleId);
	}
	
	protected AbstractAgentRole(AbstractBuilder<?, ?> b, RolePropertyContainer envPropertyContainer) {
		super(b.roleId, envPropertyContainer);
	}

	public abstract void deploy(IAutomationAgentClient aaClient);

	@Override
	public Map<String, String> getEnvProperties() {
		properties.put("agent_hostname", getHostWithPort());

		return properties;
	}

	protected abstract static class AbstractLinuxBuilder<T extends BuilderBase<T, U>, U> extends
			AbstractBuilder<T, U> {

		public AbstractLinuxBuilder(String roleId, ITasResolver tasResolver) {
			super(roleId, tasResolver);
			agentPlatform = ArtifactPlatform.UNIX;
		}
	}

	/**
	 * Builds an instance of {@link AbstractAgentRole} with given parameters
	 */
	protected static abstract class AbstractBuilder<T extends BuilderBase<T, U>, U> extends
			BuilderBase<T, U> {

		protected final String roleId;
		protected final ITasResolver tasResolver;
		protected ArtifactPlatform agentPlatform = ArtifactPlatform.WINDOWS;
		// remaining fields

		protected boolean overrideEM = false;
		protected String emHostOverride;
		protected int emPortOverride;
		protected EmRole emRole;

		protected String installDir;
		protected String installerTgDir;
		protected Map<String, String> additionalProps = new HashMap<>();
		protected AgentInstrumentationLevel instrumentationLevel;

		protected String agentVersion;
		protected Artifact agentArtifact;
		protected ITasArtifactFactory tasAgentArtifact;
		protected Boolean webAppStart;

		public AbstractBuilder(String roleId, ITasResolver tasResolver) {
			this.roleId = roleId;
			this.tasResolver = tasResolver;
		}

		/**
		 * Builds instance of {@link AbstractAgentRole}
		 */
		@Override
		public abstract U build();

		@Override
		protected abstract T builder();

		@Override
		protected abstract U getInstance();

		public T emRole(EmRole emRole) {
			this.emRole = emRole;
			return builder();
		}

		public T overrideEM(String hostname, int port) {
			overrideEM = true;
			emHostOverride = hostname;
			emPortOverride = port;
			return builder();
		}

		public T installDir(String installDir) {
			this.installDir = installDir;
			return builder();
		}

		public T installerTgDir(String installerTgDir) {
			this.installerTgDir = installerTgDir;
			return builder();
		}

		public T version(IArtifactVersion agentVersion) {
			this.agentVersion = agentVersion.toString();
			return builder();
		}

		public T version(String agentVersion) {
			this.agentVersion = agentVersion;
			return builder();
		}

		public T customName(String agentCustomName) {
			Map<String, String> additionalProperties = new HashMap<>();
			additionalProperties.put("introscope.agent.customProcessName", agentCustomName);
			additionalProperties.put("introscope.agent.agentName", agentCustomName);
			this.additionalProps.putAll(additionalProperties);
			return builder();
		}

		public T platform(ArtifactPlatform agentPlatform) {
			this.agentPlatform = agentPlatform;
			return builder();
		}

		public T additionalProperties(Map<String, String> additionalProperties) {
			this.additionalProps.putAll(additionalProperties);
			return builder();
		}

		public T intrumentationLevel(AgentInstrumentationLevel instrumentationLevel) {
			this.instrumentationLevel = instrumentationLevel;
			return builder();
		}

		/**
		 * Setter allows to configure custom Agent artifact to the role
		 *
		 * @param agentArtifact
		 *            Object identifying artifact
		 */
		public T version(Artifact agentArtifact) {
			this.agentArtifact = agentArtifact;
			return builder();
		}

		/**
		 * Setter allows to configure custom Agent artifact to the role
		 *
		 * @param tasAgentArtifact
		 *            Object identifying artifact
		 */
		public T version(ITasArtifactFactory tasAgentArtifact) {
			this.tasAgentArtifact = tasAgentArtifact;
			return builder();
		}
	}
}
