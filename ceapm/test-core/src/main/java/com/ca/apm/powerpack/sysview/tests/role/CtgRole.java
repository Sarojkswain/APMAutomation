/*
 * Copyright (c) 2015 CA. All rights reserved.
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

package com.ca.apm.powerpack.sysview.tests.role;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.apm.automation.action.flow.FlowConfig.FlowConfigBuilder;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * Deployment of IBM CTG on distributed platform.
 * <B>Usage examples:</B> <BR>
 * CtgRole.Builder(...).build() - installation with default configuration <BR>
 * CtgRole.Builder(...).ctgVersion(version).installLocation(path).build() - for other CTG versions
 */
public class CtgRole extends AbstractRole {

    private static final String BASE_DIR = "C:\\automation\\deployed\\IBM\\";
    private static final String UNPACKED_SRC_DIR = "install_ctg";

    private static final String CONFIG_TARGET_PATH =
        "C:\\ProgramData\\IBM\\CICS Transaction Gateway\\ctg.ini";

    private static final String START_COMMAND = "ctgadmin -a start";
    private static final String STOP_COMMAND = "ctgadmin -a shut";

    private URL ctgUrl;
    private String installLocation;

    private final Collection<CtgServerDefinition> serverDefinitions;
    private final CtgGatewayDefinition gatewayDefinition;

    protected CtgRole(Builder builder) {
        super(builder.roleId);
        serverDefinitions = builder.definitions;
        gatewayDefinition = new CtgGatewayDefinition(builder.port);
    }

    /**
     * Deploys CTG including configuration (if supplied).
     */
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        unpackCtg(aaClient); // downloads and unpacks CTG
        installCtg(aaClient); // runs silent installer
        createConfigFile(aaClient); // uploads ctg.ini
        startCtg(aaClient); // starts CTG server
    }

    /**
     * Downloads and unpacks CTG.
     */
    protected void unpackCtg(IAutomationAgentClient aaClient) {
        GenericFlowContext ctx =
            new GenericFlowContext.Builder(ctgUrl).destination(BASE_DIR + UNPACKED_SRC_DIR).build();
        runFlow(aaClient, GenericFlow.class, ctx);
    }

    /**
     * Installs CTG.
     */
    protected void installCtg(IAutomationAgentClient aaClient) {

        String[] args =
            {"-i", "silent", "-DLICENSE_ACCEPTED=true",
             "-DUSER_INSTALL_DIR=" + installLocation.replace("\\", "\\\\")};

        RunCommandFlowContext cmdCtx =
            new RunCommandFlowContext.Builder("installer").workDir(BASE_DIR + UNPACKED_SRC_DIR)
                .args(Arrays.asList(args)).name(getRoleId()).build();

        aaClient
            .runJavaFlow(new FlowConfigBuilder(RunCommandFlow.class, cmdCtx, getHostWithPort()));
    }

    /**
     * Starts CTG
     * 
     * @param aaClient Automation agent.
     */
    public void startCtg(IAutomationAgentClient aaClient) {
        RunCommandFlowContext cmdCtx =
            new RunCommandFlowContext.Builder(START_COMMAND).workDir(installLocation + "\\bin")
                .build();

        aaClient
            .runJavaFlow(new FlowConfigBuilder(RunCommandFlow.class, cmdCtx, getHostWithPort()));
    }

    /**
     * Stops CTG
     * 
     * @param aaClient Automation agent.
     */
    public void stopCtg(IAutomationAgentClient aaClient) {
        RunCommandFlowContext cmdCtx =
            new RunCommandFlowContext.Builder(STOP_COMMAND).workDir(installLocation + "\\bin")
                .build();

        aaClient
            .runJavaFlow(new FlowConfigBuilder(RunCommandFlow.class, cmdCtx, getHostWithPort()));
    }

    /**
     * @param aaClient Automation agent.
     */
    protected void createConfigFile(IAutomationAgentClient aaClient) {
        /* 
         * TODO: Right now there is a bug in the TAS FileModifierFlowContext that prevents this from
         * being down in one flow execution. Once the bug is resolved this version should be used.
         * The bug is at FileModifierFlowContext:200 (trying to put into an immutable map)
        FileModifierFlowContext.Builder ctxBuilder = new FileModifierFlowContext.Builder();

        ctxBuilder.create(CONFIG_TARGET_PATH, gatewayDefinition.getDefinition());
        for (CtgServerDefinition serverDefinition : serverDefinitions) {
            ctxBuilder.append(CONFIG_TARGET_PATH, serverDefinition.getDefinition());
        }

        runFlow(aaClient, FileModifierFlow.class, ctxBuilder.build());
        */
        
        FileModifierFlowContext.Builder ctxBuilder = new FileModifierFlowContext.Builder();
        ctxBuilder.create(CONFIG_TARGET_PATH, gatewayDefinition.getDefinition());
        runFlow(aaClient, FileModifierFlow.class, ctxBuilder.build());

        ctxBuilder = new FileModifierFlowContext.Builder();
        for (CtgServerDefinition serverDefinition : serverDefinitions) {
            ctxBuilder.append(CONFIG_TARGET_PATH, serverDefinition.getDefinition());
        }
        runFlow(aaClient, FileModifierFlow.class, ctxBuilder.build());
    }

    public static class Builder extends BuilderBase<Builder, CtgRole> {
        private final String roleId;
        private final ITasResolver tasResolver;

        private String ctgVersion = "9.0.0.2";
        private String installLocation = BASE_DIR + "CTG";
        private int port = 2006;
        private Collection<CtgServerDefinition> definitions = new ArrayList<CtgServerDefinition>();

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public CtgRole build() {
            CtgRole role = getInstance();

            role.installLocation = installLocation;
            role.ctgUrl = tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.binaries.ibm",
                "ctg", "windows-x86", "zip", ctgVersion));

            return role;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected CtgRole getInstance() {
            return new CtgRole(this);
        }

        /**
         * Version CTG to deploy
         * 
         * @param version Version string.
         * @return Builder instance the method was called on.
         */
        public Builder ctgVersion(String version) {
            this.ctgVersion = version;
            return builder();
        }

        /**
         * CTG installation location
         * 
         * @param location Path to the install location.
         * @return Builder instance the method was called on.
         */
        public Builder installLocation(String location) {
            this.installLocation = location;
            return builder();
        }
        
        public Builder port(int port) {
            this.port = port;
            return builder();
        }
        
        public Builder addServerDefinition(CtgServerDefinition definition) {
            definitions.add(definition);
            return builder();
        }
    }
}
