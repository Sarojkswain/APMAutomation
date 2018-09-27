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

public class KibanaRole extends AbstractRole {

    private static final String VERSION = "5.4.0";
    public static final String KIBANA_START = "kibanaStart";

    private String installDir;
    private ITasResolver tasResolver;
    private String host;
    private String esHost;

    private final RunCommandFlowContext startKibanaContext;

    protected KibanaRole(Builder builder) {

        super(builder.roleId, builder.getEnvProperties());
        this.tasResolver = builder.tasResolver;
        this.installDir = builder.installDir;
        this.esHost = builder.esHost;
        host = tasResolver.getHostnameById(builder.roleId);
        this.startKibanaContext = builder.startKibanaContext;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {

        getArtifacts(aaClient);
        updateConfig(aaClient);
    }

    public RunCommandFlowContext getStartKibanaContext() {
        return startKibanaContext;
    }

    private void updateConfig(IAutomationAgentClient aaClient) {

        Map<String, String> replacePairs = new HashMap<String, String>();

        replacePairs.put("#server.host: \"localhost\"",
            "server.host: \"" + RoleUtility.getIp(host) + "\"");
        replacePairs.put("#elasticsearch.url: \"http://localhost:9200\"",
            "elasticsearch.url: \"http://" + RoleUtility.getIp(esHost) + ":9200\"");
        replacePairs.put("#logging.dest: stdout",
            "logging.dest: /var/log/kibana.log");

        String fileName = installDir
                + "/config/kibana.yml";

        FileModifierFlowContext context = new FileModifierFlowContext.Builder()
                    .replace(fileName, replacePairs).build();
        runFlow(aaClient, FileModifierFlow.class, context);
    }

    private void getArtifacts(IAutomationAgentClient aaClient) {

        URL url = tasResolver.getArtifactUrl(new DefaultArtifact(
                "com.ca.apm.binaries", "kibana", "linux-x86_64", "tar.gz", VERSION));
        UniversalFlowContext getAgentContext = new UniversalFlowContext.Builder()
                .archive(url, installDir).build();
        runFlow(aaClient, UniversalFlow.class, getAgentContext);
    }

    public static class LinuxBuilder extends Builder {
        public LinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
        }
    }

    public static class Builder extends BuilderBase<Builder, KibanaRole> {

        private final String roleId;
        private final ITasResolver tasResolver;
        private String installDir = "/home/kibana";
        private String esHost;

        private RunCommandFlowContext startKibanaContext;
        
        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public KibanaRole build() {
            startKibana();
            return getInstance();
        }

        @Override
        protected KibanaRole getInstance() {
            return new KibanaRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        public Builder installDir(String installDir) {
            this.installDir = installDir;
            return builder();
        }

        public Builder elasticSearch(String esHost) {
            this.esHost = esHost;
            return builder();
        }

        private void startKibana() {
            startKibanaContext = new RunCommandFlowContext.Builder("sh")
                       .args(Arrays.asList("-c", "nohup " + installDir + "/bin/kibana >std.txt 2>&1 &"))
                       .workDir(installDir)
                       .doNotPrependWorkingDirectory().build();
            getEnvProperties().add(KIBANA_START, startKibanaContext);
        }
    }
}
