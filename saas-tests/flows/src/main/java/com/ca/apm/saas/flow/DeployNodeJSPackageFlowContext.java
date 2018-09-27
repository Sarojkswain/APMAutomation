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

package com.ca.apm.saas.flow;

import java.net.URL;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;

/**
 * DeployNodeJSModuleFlowContext class stores data needed for a nodejs package
 * deployment
 */
public class DeployNodeJSPackageFlowContext implements IFlowContext {

	private String installerTgDir;
	private String installDir;
	private URL installerUrl;
	private String packageName;
	private String version;
	private String nodeJsExecutableLocation;
	private String nodeJsHomeDir;
	private String npmRegistryUrl;

	protected DeployNodeJSPackageFlowContext(Builder builder) {
		this.installerTgDir = builder.installerTgDir;
		this.installDir = builder.installDir;
		this.installerUrl = builder.installerUrl;
		this.packageName = builder.packageName;
		this.version = builder.version;
		this.nodeJsExecutableLocation = builder.nodeJsExecutableLocation;
		this.nodeJsHomeDir = builder.nodeJsHomeDir;
		this.npmRegistryUrl = builder.npmRegistryUrl;
	}

	public String getInstallerTgDir() {
		return installerTgDir;
	}

	public String getInstallDir() {
		return installDir;
	}

	public URL getInstallerUrl() {
		return installerUrl;
	}
	
	public String getPackageName() {
		return packageName;
	}

	public String version() {
		return version;
	}

	public String getNodeJsExecutableLocation() {
		return nodeJsExecutableLocation;
	}

	public String getNodeJsHomeDir() {
		return nodeJsHomeDir;
	}

	public String getNpmRegistryUrl() {
		return npmRegistryUrl;
	}
	
	public static class LinuxBuilder extends Builder {
	    
		public static final String DEFAULT_INSTALL_TG_DIR = LINUX_SOFTWARE_LOC
		        + "installers/npm-packages/";
		public static final String DEFAULT_INSTALL_DIR = LINUX_SOFTWARE_LOC + "npm-packages/";

		public LinuxBuilder() {
			installerTgDir = DEFAULT_INSTALL_TG_DIR;
			installDir = DEFAULT_INSTALL_DIR;
		}

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

	public static class Builder extends BuilderBase<Builder, DeployNodeJSPackageFlowContext> {

		public static final String DEFAULT_INSTALL_TG_DIR = WIN_SOFTWARE_LOC
		        + "installers\\npm-packages\\";
		public static final String DEFAULT_INSTALL_DIR = WIN_SOFTWARE_LOC + "npm-packages\\";
		public static final String DEFAULT_COLLAGENT_HOST = "localhost";
		protected String installerTgDir = DEFAULT_INSTALL_TG_DIR;
		protected String installDir = DEFAULT_INSTALL_DIR;
		protected String nodeJsExecutableLocation;
		protected String nodeJsHomeDir;
		protected String npmRegistryUrl;
		protected URL installerUrl;		
		protected String packageName;
		protected String version;

		@Override
		public DeployNodeJSPackageFlowContext build() {

			return getInstance();
		}

		@Override
		protected DeployNodeJSPackageFlowContext getInstance() {
			return new DeployNodeJSPackageFlowContext(this);
		}

		@Override
		protected Builder builder() {
			return this;
		}

		public Builder installerUrl(URL installerUrl) {
			this.installerUrl = installerUrl;
			return builder();
		}
		
		public Builder packageName(String packageName) {
			this.packageName = packageName;
			return builder();
		}

		public Builder version(String version) {
			this.version = version;
			return builder();
		}

		public Builder installerTgDir(String installerTgDir) {
			this.installerTgDir = installerTgDir;
			return builder();
		}

		public Builder installDir(String installDir) {
			this.installDir = installDir;
			return builder();
		}

		public Builder nodeJsExecutableLocation(String location) {
			this.nodeJsExecutableLocation = location;
			return builder();
		}

		public Builder nodeJsHomeDir(String nodeJsHomeDir) {
			this.nodeJsHomeDir = nodeJsHomeDir;
			return builder();
		}

		public Builder npmRegistryUrl(String url) {
			this.npmRegistryUrl = url;
			return builder();
		}		
	}

	@Override
	public String toString() {
		return "DeployNodeJSPackageFlowContext{installerTgdir=" + installerTgDir + ", installerUrl="
		        + installerUrl + ", installDir=" + installDir + "}";
	}
}