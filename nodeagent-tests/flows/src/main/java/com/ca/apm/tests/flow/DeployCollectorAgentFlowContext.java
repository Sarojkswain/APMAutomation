/*
 * Copyright (c) 2014 CA.  All rights reserved.
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

import org.apache.http.util.Args;

import com.ca.apm.automation.action.flow.IFlowContext;

/**
 * DeployCollectorAgentFlowContext class captures information needed for deploying
 * Collector Agent(no installer)
 * 
 * @author pojja01
 * @author sinka08
 *
 */
public final class DeployCollectorAgentFlowContext extends AbstractDeployAgentFlowContext implements
        IFlowContext {
	private String collAgentExecutable;

	public DeployCollectorAgentFlowContext(Builder b) {
		super(b);
		collAgentExecutable = b.collAgentExecutable;
	}

	public String getCollAgentExecutable() {
		return collAgentExecutable;
	}

	public static class LinuxBuilder extends Builder {
		public static final String COLLECTOR_AGENT_EXECUTABLE = "CollectorAgent.sh";
		private static final String DEFAULT_INSTALL_TG_DIR = AbstractDeployAgentFlowContext.AbstractLinuxBuilder.DEFAULT_INSTALL_TG_DIR
		        + DEFAULT_COLLECTOR_AGENT_DIR_NAME + "/";
		private static final String DEFAULT_INSTALL_DIR = AbstractDeployAgentFlowContext.AbstractLinuxBuilder.DEFAULT_INSTALL_DIR
		        + DEFAULT_COLLECTOR_AGENT_DIR_NAME + "/";

		public LinuxBuilder() {
			installerTgDir(DEFAULT_INSTALL_TG_DIR);
			installDir(DEFAULT_INSTALL_DIR);
			collAgentExecutable = COLLECTOR_AGENT_EXECUTABLE;
		}

		@Override
		protected String getPathSeparator() {
			return LINUX_SEPARATOR;
		}
	}

	public static class Builder
	        extends
	        AbstractDeployAgentFlowContext.AbstractBuilder<Builder, DeployCollectorAgentFlowContext> {
		public static final String COLLECTOR_AGENT_EXECUTABLE = "CollectorAgent.cmd";
		protected static final String DEFAULT_COLLECTOR_AGENT_DIR_NAME = "collectoragent";
		public static final String DEFAULT_INSTALL_TG_DIR = AbstractDeployAgentFlowContext.AbstractBuilder.DEFAULT_INSTALL_TG_DIR
		        + DEFAULT_COLLECTOR_AGENT_DIR_NAME + "\\";
		public static final String DEFAULT_INSTALL_DIR = AbstractDeployAgentFlowContext.AbstractBuilder.DEFAULT_INSTALL_DIR
		        + DEFAULT_COLLECTOR_AGENT_DIR_NAME + "\\";

		protected String collAgentExecutable = COLLECTOR_AGENT_EXECUTABLE;
		@SuppressWarnings("unused")
        private final String PHP_PBL_FILE_NAME = "php-%s.pbl";
		@SuppressWarnings("unused")
        private final String NODEJS_PBL_FILE_NAME = "nodejs-%s.pbl";

		public enum CollectorAgentProperty {
			TCP_PORT("introscope.remoteagent.collector.tcp.port", "5005"), TCP_LOCAL_MODE(
			        "introscope.remoteagent.collector.tcp.local.only", "false");

			private String key;
			private String defValue;

			private CollectorAgentProperty(String key, String defValue) {
				this.key = key;
				this.defValue = defValue;
			}

			public String getKey() {
				return key;
			}

			public String getDefaultValue() {
				return defValue;
			}

			@Override
			public String toString() {
				return getKey() + ":" + getDefaultValue();
			}
		}

		public Builder() {
			installDir(DEFAULT_INSTALL_DIR);
			installerTgDir(DEFAULT_INSTALL_TG_DIR);
		}

		@Override
		public DeployCollectorAgentFlowContext build() {
			initDirectives();

			DeployCollectorAgentFlowContext flowContext = getInstance();
			Args.notNull(flowContext.installerUrl, "CollectorAgent installer URL");
			Args.notNull(flowContext.installerTgdir, "CollectorAgent target install directory");
			Args.notNull(flowContext.installDir, "CollectorAgent install directory");

			return flowContext;
		}

		protected void initDirectives() {
			Args.notNull(directiveFilenames, "directive files");

			if (directiveFilenames.isEmpty()) {
				// user did not configure directives, should we add default ones
				
				//directiveFilenames.add(getFormattedPblFile(PHP_PBL_FILE_NAME));
				//directiveFilenames.add(getFormattedPblFile(NODEJS_PBL_FILE_NAME));
				//directiveFilenames.add("hotdeploy");
			}

		}

		private String getFormattedPblFile(String fileName) {
			return String.format(fileName, instrumentationLevel.name().toLowerCase());
		}

		@Override
		protected Builder builder() {
			return this;
		}

		@Override
		protected DeployCollectorAgentFlowContext getInstance() {
			return new DeployCollectorAgentFlowContext(this);
		}

		@Override
		protected String getPathSeparator() {
			return WIN_SEPARATOR;
		}

	}

	@Override
	public String toString() {
		return "DeployCollectorAgentFlowContext{" + getFieldValuesRep() + "}";
	}
}
