/*
 * Copyright (c) 2017 CA.  All rights reserved.
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

package com.ca.apm.tests.role;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.tests.flow.AccAgentDownloadFlow;
import com.ca.apm.tests.flow.AccAgentDownloadFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * @author kurma05
 */
public class AccAgentSetupRole extends AbstractRole {

	private String installDir;
	private boolean shouldSetup;
	protected String agentPackageUrl;
	protected String accServerUrl;	
	private String packageName;
	private String osName;
	private static final Logger LOGGER = LoggerFactory.getLogger(AccAgentSetupRole.class);

	protected AccAgentSetupRole(Builder builder) {

		super(builder.roleId);
		this.installDir = builder.installDir;
		this.agentPackageUrl = builder.agentPackageUrl;
		this.accServerUrl = builder.accServerUrl;
		this.shouldSetup = builder.shouldSetup;
		this.packageName = builder.packageName;
		this.osName = builder.osName;
	}

	@Override
	public void deploy(IAutomationAgentClient aaClient) {

		downloadPackage(aaClient);
		if (shouldSetup) {
			setupAgent(aaClient);
		}
	}

	private void setupAgent(IAutomationAgentClient aaClient) {

		// copy WebAppSupport (until it's implemented as dynamic ext in 10.6)

		LOGGER.info("AccAgentSetupRole:setupAgent() -- No additional config steps");

	}

	private void downloadPackage(IAutomationAgentClient aaClient) {
        
        AccAgentDownloadFlowContext context = new AccAgentDownloadFlowContext.Builder()
           .agentPackageUrl(agentPackageUrl)
           .accServerUrl(accServerUrl)
           .packageName(packageName)
           .osName(osName)
           .installDir(installDir)
           .build();      
        runFlow(aaClient, AccAgentDownloadFlow.class, context);
    }

	public static class Builder extends BuilderBase<Builder, AccAgentSetupRole> {

		private final String roleId;
		protected String installDir;
		protected String agentPackageUrl;
		protected String accServerUrl;
		private boolean httpsEnabled;
		private String host;
		private int port;
		private final ITasResolver tasResolver;
		protected boolean shouldSetup;
		private String packageName;
		private String osName;

		public Builder(String roleId, ITasResolver tasResolver) {
			this.roleId = roleId;
			this.tasResolver = tasResolver;
		}

		@Override
		public AccAgentSetupRole build() {
			return getInstance();
		}

		@Override
		protected AccAgentSetupRole getInstance() {
			return new AccAgentSetupRole(this);
		}

		@Override
		protected Builder builder() {
			return this;
		}

		public Builder installDir(String installDir) {
			this.installDir = installDir;
			return builder();
		}

		public Builder accPackageUrl(String url) {
			this.agentPackageUrl = url;
			return builder();
		}
		
		public Builder accServerUrl(String url) {
			this.accServerUrl = url;
			return builder();
		}

		public Builder shouldSetup(boolean shouldSetup) {
			this.shouldSetup = shouldSetup;
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
	}
}