/*
 * Copyright (c) 2017 CA. All rights reserved.
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
package com.ca.tas.role;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.UniversalFlow;
import com.ca.apm.automation.action.flow.utility.UniversalFlowContext;
import com.ca.apm.test.em.util.RoleUtility;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 *
 * @author korzd01@ca.com
 */

public class ElasticSearchRole extends AbstractRole {

    private static final String VERSION = "5.6.3";
    public static final String ELASTICSEARCH_START = "elasticSearchStart";

    private String installDir;
	private ITasResolver tasResolver;
	private String host;

    private final RunCommandFlowContext startElasticSearchContext;

	protected ElasticSearchRole(Builder builder) {

		super(builder.roleId, builder.getEnvProperties());
		this.tasResolver = builder.tasResolver;
		this.installDir = builder.installDir;
		host = tasResolver.getHostnameById(builder.roleId);
        this.startElasticSearchContext = builder.startElasticSearchContext;
	}

	@Override
	public void deploy(IAutomationAgentClient aaClient) {

        getArtifacts(aaClient);
        updateConfig(aaClient);
		correctEnvironment(aaClient);
	}

    public RunCommandFlowContext getStartElasticSearchContext() {
        return startElasticSearchContext;
    }

	private void updateConfig(IAutomationAgentClient aaClient) {

		Map<String, String> replacePairs = new HashMap<String, String>();

		replacePairs.put("#network.host: 192.168.0.1", "network.host: "
				+ RoleUtility.getIp(host));

		String fileName = installDir
				+ "/config/elasticsearch.yml";

		FileModifierFlowContext context = new FileModifierFlowContext.Builder()
		            .replace(fileName, replacePairs).build();
		runFlow(aaClient, FileModifierFlow.class, context);
	}

    private void correctEnvironment(IAutomationAgentClient aaClient) {
        RunCommandFlowContext addUserContext = new RunCommandFlowContext.Builder("adduser")
                    .args(Arrays.asList("-m", "elastic"))
                    .doNotPrependWorkingDirectory().build();
        runCommandFlow(aaClient, addUserContext);
        
        RunCommandFlowContext chownContext = new RunCommandFlowContext.Builder("chown")
                    .args(Arrays.asList("-R", "elastic:elastic", installDir))
                    .doNotPrependWorkingDirectory().build();
        runCommandFlow(aaClient, chownContext);
        
        RunCommandFlowContext sysctlContext = new RunCommandFlowContext.Builder("sysctl")
                    .args(Arrays.asList("-w", "vm.max_map_count=262144"))
                    .doNotPrependWorkingDirectory().build();
        runCommandFlow(aaClient, sysctlContext);
        
        Collection<String> data = Arrays
            .asList("elastic    -   nofile      65536",
                    "elastic    -   nproc       65536");
        String fileName = "/etc/security/limits.conf";

        FileModifierFlowContext appendFileFlow =
            new FileModifierFlowContext.Builder().append(fileName, data).build();
        runFlow(aaClient, FileModifierFlow.class, appendFileFlow);
    }

	private void getArtifacts(IAutomationAgentClient aaClient) {

		URL url = tasResolver.getArtifactUrl(new DefaultArtifact(
				"com.ca.apm.binaries", "elasticsearch", "tar.gz", VERSION));
		UniversalFlowContext getAgentContext = new UniversalFlowContext.Builder()
				.archive(url, installDir).build();
		runFlow(aaClient, UniversalFlow.class, getAgentContext);
	}

	public static class LinuxBuilder extends Builder {
		public LinuxBuilder(String roleId, ITasResolver tasResolver) {
			super(roleId, tasResolver);
		}
	}

	public static class Builder extends BuilderBase<Builder, ElasticSearchRole> {

		private final String roleId;
		private final ITasResolver tasResolver;
		private String installDir = "/home/elasticsearch";

        private RunCommandFlowContext startElasticSearchContext;
		
		public Builder(String roleId, ITasResolver tasResolver) {
			this.roleId = roleId;
			this.tasResolver = tasResolver;
		}

		@Override
		public ElasticSearchRole build() {
			startElasticSearch();
			return getInstance();
		}

		@Override
		protected ElasticSearchRole getInstance() {
			return new ElasticSearchRole(this);
		}

		@Override
		protected Builder builder() {
			return this;
		}

		public Builder installDir(String installDir) {
			this.installDir = installDir;
			return builder();
		}

		private void startElasticSearch() {
		    startElasticSearchContext = new RunCommandFlowContext.Builder("runuser")
		            .args(Arrays.asList("-l", "elastic", "-c", installDir + "/bin/elasticsearch"))
		            .doNotPrependWorkingDirectory()
					.terminateOnMatch("indices into cluster_state").build();
			getEnvProperties().add(ELASTICSEARCH_START, startElasticSearchContext);
		}
	}
}
