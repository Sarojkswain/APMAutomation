package com.ca.apm.tests.flow;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;

/**
 * @author kurma05
 */
public class AccAgentDownloadFlowContext implements IFlowContext {

	private String installDir;
	private String packageName;
	private String osName;
	private String accServerUrl;
	private boolean httpsEnabled;
	private String host;
	private int port;
	private String agentPackageUrl; //agent package url

	protected AccAgentDownloadFlowContext(Builder builder) {

		this.installDir = builder.installDir;
		this.agentPackageUrl = builder.agentPackageUrl;
		this.accServerUrl = builder.accServerUrl;
		this.packageName = builder.packageName;
		this.osName = builder.osName;
	}

	public String getInstallDir() {
		return installDir;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getOsName() {
		return osName;
	}

	public String getAgentPackageUrl() {
		return agentPackageUrl;
	}
	
	public String getAccServerUrl() {
		return accServerUrl;
	}

	public static class LinuxBuilder extends Builder {

		@Override
		protected Builder builder() {
			return this;
		}

		@Override
		protected String getPathSeparator() {
			return LINUX_SEPARATOR;
		}

		@Override
		protected String getDeployBase() {
			return getLinuxDeployBase();
		}
	}

	public static class Builder extends BuilderBase<Builder, AccAgentDownloadFlowContext> {

		private String installDir;
		private String agentPackageUrl;
		private String accServerUrl;
		private String packageName;
		private String osName;

		@Override
		public AccAgentDownloadFlowContext build() {

			return getInstance();
		}

		@Override
		protected AccAgentDownloadFlowContext getInstance() {
			return new AccAgentDownloadFlowContext(this);
		}

		@Override
		protected Builder builder() {
			return this;
		}

		public Builder installDir(String installDir) {
			this.installDir = installDir;
			return builder();
		}

		public Builder packageName(String packageName) {
			this.packageName = packageName;
			return builder();
		}

		public Builder osName(String osName) {
			this.osName = osName;
			return builder();
		}

		public Builder agentPackageUrl(String url) {
			this.agentPackageUrl = url;
			return builder();
		}
		
		public Builder accServerUrl(String url) {
			this.accServerUrl = url;
			return builder();
		}
	}
}