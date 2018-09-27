/*
 * Copyright (c) 2015 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE 
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR 
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST 
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS 
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.  
 */

package com.ca.apm.tests.flow;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.tas.builder.BuilderBase;

/**
 * AbstractDeployAgentFlowContext is abstract base class which captures common
 * information needed for deployment of different types of agents for e.g.
 * JavaAgent, CollectorAgent etc.
 * 
 * @author macma13
 * @author pojja01
 */
public abstract class AbstractDeployAgentFlowContext implements IFlowContext {

	protected final String emHost;
	protected final int emPort;
	protected final String installerTgdir;
	protected final URL installerUrl;
	protected final String installDir;
	protected final Map<String, String> additionalProperties;
	protected final Collection<String> directiveFilenames;

	public AbstractDeployAgentFlowContext(AbstractBuilder<?, ?> b) {
		installerTgdir = b.installerTgDir;
		installDir = b.installDir;
		installerUrl = b.installerUrl;
		emPort = b.emPort;
		emHost = b.emHost;
		additionalProperties = b.additionalProperties;
		directiveFilenames = b.directiveFilenames;
	}

	public String getEmHost() {
		return emHost;
	}

	public int getEmPort() {
		return emPort;
	}

	public String getInstallerTgdir() {
		return installerTgdir;
	}

	public URL getInstallerUrl() {
		return installerUrl;
	}

	public String getInstallDir() {
		return installDir;
	}

	public Map<String, String> getAdditionalProperties() {
		return additionalProperties;
	}

	public Collection<String> getDirectiveFilenames() {
		return directiveFilenames;
	}

	protected static abstract class AbstractLinuxBuilder<T extends BuilderBase<T, U>, U> extends
	        AbstractBuilder<T, U> {
		public static final String DEFAULT_INSTALL_TG_DIR = LINUX_SOFTWARE_LOC
		        + "installers/agent/";
		public static final String DEFAULT_INSTALL_DIR = LINUX_SOFTWARE_LOC + "agent/";

		public AbstractLinuxBuilder() {
			installerTgDir(DEFAULT_INSTALL_TG_DIR);
			installDir(DEFAULT_INSTALL_DIR);
		}
	}

	protected static abstract class AbstractBuilder<T extends BuilderBase<T, U>, U> extends
	        BuilderBase<T, U> {

		public static final String DEFAULT_INSTALL_TG_DIR = WIN_SOFTWARE_LOC
		        + "installers\\agent\\";
		public static final String DEFAULT_INSTALL_DIR = WIN_SOFTWARE_LOC + "agent\\";
		protected String installerTgDir = DEFAULT_INSTALL_TG_DIR;
		protected String installDir = DEFAULT_INSTALL_DIR;
		protected String emHost;
		protected int emPort = DeployEMFlowContext.EM_PORT;
		protected URL installerUrl;
		protected AgentInstrumentationLevel instrumentationLevel = AgentInstrumentationLevel.TYPICAL;
		protected final Collection<String> directiveFilenames = new ArrayList<>();
		protected final Map<String, String> additionalProperties = new HashMap<>();

		@Override
		public abstract U build();

		@Override
		protected abstract T builder();

		@Override
		protected abstract U getInstance();

		public T installerUrl(URL installerUrl) {
			this.installerUrl = installerUrl;
			return builder();
		}

		public T installerTgDir(String installerTgDir) {
			this.installerTgDir = installerTgDir;
			return builder();
		}

		public T installDir(String installDir) {
			this.installDir = installDir;
			return builder();
		}

		public T setupEm(String hostname, int port) {
			emHost = hostname;
			emPort = port;
			return builder();
		}

		public T intrumentationLevel(AgentInstrumentationLevel instrumentationLevel) {
			this.instrumentationLevel = instrumentationLevel;
			return builder();
		}

		public T additionalProps(Map<String, String> additionalProperties) {
			this.additionalProperties.putAll(additionalProperties);
			return builder();
		}

		public T setupEm(String hostname) {
			return setupEm(hostname, emPort);
		}
	}

	protected String getFieldValuesRep() {
		return "emHost='" + emHost + '\'' + ", emPort=" + emPort + ", installerTgdir="
		        + installerTgdir + ", installerUrl=" + installerUrl + ", installDir=" + installDir
		        + ", directiveFilenames=" + directiveFilenames;
	}
}
