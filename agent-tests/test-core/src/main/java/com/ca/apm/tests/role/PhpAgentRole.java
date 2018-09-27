/*
 * Copyright (c) 2016 CA. All rights reserved.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.FlowConfig.FlowConfigBuilder;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.tests.artifact.PhpAgentArtifact;
import com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * PhpAgentRole class.
 *
 * Deploys the Collector Agent and the PHP probe.
 *
 * @author Aleem Ahmad (ahmal01@ca.com)
 */
public class PhpAgentRole extends AbstractRole {

    private static final Logger LOGGER = LoggerFactory.getLogger(PhpAgentRole.class);
    private ITasResolver tasResolver;
    private final GenericFlowContext deployAgentFlowContext;
    private final FileModifierFlowContext modifyFileFlowContext;
    private final RunCommandFlowContext startAgentCollectorFlowContext;
    private final RunCommandFlowContext startAgentProbeFlowContext;
    private final FileModifierFlowContext copyAgentProbeFlowContext;
    private final RunCommandFlowContext chmodAgentProbeModuleFlowContext;
    
    private String phpExtConfDirPath;
    private String deployBase;

    /**
     * <p>
     * Constructor for PhpAgentRole.
     * </p>
     *
     * @param builder Builder object containing all necessary data
     */
    protected PhpAgentRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());

        deployAgentFlowContext = builder.deployAgentFlowContext;
        modifyFileFlowContext = builder.modifyFileFlowContext;
        startAgentCollectorFlowContext = builder.startAgentCollectorFlowContext;
        startAgentProbeFlowContext = builder.startAgentProbeFlowContext;
        copyAgentProbeFlowContext = builder.copyAgentProbeFlowContext;
        chmodAgentProbeModuleFlowContext = builder.chmodAgentProbeModuleFlowContext;
        
        this.tasResolver = builder.tasResolver;
        this.phpExtConfDirPath = builder.phpExtConfDirPath;
        this.deployBase = builder.deployBase;
        
    }

    /** {@inheritDoc} */
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        deployAgent(aaClient);
        modifyINIFile(aaClient);
        modifyPbdFile(aaClient);
        activateProbe(aaClient);
        startCollector(aaClient);
        startProbe(aaClient);
    }

    /**
     * <p>
     * deployAgent.
     * </p>
     *
     * @param aaClient a {@link com.ca.tas.client.IAutomationAgentClient} object.
     */
    protected void deployAgent(IAutomationAgentClient aaClient) {
        aaClient.runJavaFlow(new FlowConfigBuilder(GenericFlow.class, deployAgentFlowContext,
            getHostingMachine().getHostnameWithPort()));
    }

    /**
     * <p>
     * modify probe ini File.
     * </p>
     *
     * @param aaClient a {@link com.ca.tas.client.IAutomationAgentClient} object.
     */
    protected void modifyINIFile(IAutomationAgentClient aaClient) {
        
        FileModifierFlowContext context = null;
        HashMap<String,String> replacePairs = null;
       // String file = phpExtConfDirPath + "/" + "wily_php_agent.ini";
        String file = deployBase + "phpAgent/probe/wily_php_agent.ini";
        
        LOGGER.info("Updating file " + file);
        
        replacePairs = new HashMap<String,String>();
        replacePairs.put("/opt/CA/APM/apm-phpagent/logs", deployBase +  "probe/logs");
        replacePairs.put("wily_php_agent.traceInternalFunctions=0", "wily_php_agent.traceInternalFunctions=1");
        replacePairs.put("wily_php_agent.logLevel=3", "wily_php_agent.logLevel=1");
        
    // replacing values
        context = new FileModifierFlowContext.Builder()
            .replace(file, replacePairs)
            .build();
        runFlow(aaClient, FileModifierFlow.class, context);

    }        
    
    /**
     * <p>
     * modifyPbdFile.
     * </p>
     *
     * @param aaClient a {@link com.ca.tas.client.IAutomationAgentClient} object.
     */
    protected void modifyPbdFile(IAutomationAgentClient aaClient) {
        if (modifyFileFlowContext != null) {
            aaClient.runJavaFlow(new FlowConfigBuilder(FileModifierFlow.class,
                modifyFileFlowContext, getHostingMachine().getHostnameWithPort()));
        }
    }
    
    
    /**
     * <p>
     * startCollector.
     * </p>
     *
     * @param aaClient a {@link com.ca.tas.client.IAutomationAgentClient} object.
     */
    protected void startCollector(IAutomationAgentClient aaClient) {
        if (startAgentCollectorFlowContext != null) {
            aaClient.runJavaFlow(new FlowConfigBuilder(RunCommandFlow.class,
                startAgentCollectorFlowContext, getHostingMachine().getHostnameWithPort()));
        }
    }

    
    /**
     * <p>
     * startProbe.
     * </p>
     *
     * @param aaClient a {@link com.ca.tas.client.IAutomationAgentClient} object.
     */
    protected void startProbe(IAutomationAgentClient aaClient) {
        if (startAgentCollectorFlowContext != null) {
            aaClient.runJavaFlow(new FlowConfigBuilder(RunCommandFlow.class,
                startAgentProbeFlowContext, getHostingMachine().getHostnameWithPort()));
        }
    }
    /**
     * <p>
     * activateProbe.
     * </p>
     *
     * @param aaClient a {@link com.ca.tas.client.IAutomationAgentClient} object.
     */
    protected void activateProbe(IAutomationAgentClient aaClient) {
        aaClient.runJavaFlow(new FlowConfigBuilder(FileModifierFlow.class,
            copyAgentProbeFlowContext, getHostingMachine().getHostnameWithPort()));


        if (chmodAgentProbeModuleFlowContext != null) {
            aaClient.runJavaFlow(new FlowConfigBuilder(RunCommandFlow.class,
                chmodAgentProbeModuleFlowContext, getHostingMachine().getHostnameWithPort()));
        }
    }

    /**
     * Enum representing PHP versions supported by the PHP agent.
     * 
     * @author JanZak (zakja01@ca.com)
     */
    public static enum PhpVersion {
        PHP53, PHP54
    }

    /**
     * Linux Builder responsible for holding all necessary properties to instantiate
     * {@link com.ca.apm.transactiontrace.appmap.role.PhpAgentRole}
     */
    public static class LinuxBuilder extends Builder {

        public LinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
        }

        @Override
        protected ArtifactPlatform getArtifactsPlatform() {
            return ArtifactPlatform.LINUX_AMD_64;
        }

        @Override
        protected String getCollectorAgentControlCommand() {
            return "CollectorAgent.sh";
        }

        @Override
        protected void initCopyAgentProbeFlow() {
            copyAgentProbeFlowContext =
                copyAgentProbeFlowContextBuilder
                    .copy(concatPaths(getDeployBase(), "phpAgent", "probe", "lib", phpVersion
                            .toString().toLowerCase(), "wily_php_agent.so"), phpExtDirPath)
                    .copy(concatPaths(getDeployBase(), "phpAgent", "probe", "wily_php_agent.ini"),
                          concatPaths(phpExtConfDirPath, "wily_php_agent.ini"))
                    .copy(concatPaths(getDeployBase(), "phpAgent", "probe", "utils", "blacklist_size.php"), 
                          "/var/www/html/blacklist_size.php")
                    .copy(concatPaths(getDeployBase(), "phpAgent", "probe", "utils","phpinfo.php"),
                          "/var/www/html/phpinfo.php")
                    .copy(concatPaths(getDeployBase(), "phpAgent", "probe", "utils","isblacklisted.php"),
                          "/var/www/html/isblacklisted.php")
                    .copy(concatPaths(getDeployBase(), "phpAgent", "probe", "utils","dump_blacklist.php"),
                          "/var/www/html/dump_blacklist.php")        
                    .copy(concatPaths(getDeployBase(), "phpAgent", "probe", "utils", "blacklist_memory_limit.php"), 
                          "/var/www/html/blacklist_memory_limit.php")
                    .copy(concatPaths(getDeployBase(), "client", "resources", "php","closuretest.php"),
                          "/var/www/html/closuretest.php")
                    .copy(concatPaths(getDeployBase(), "client", "resources", "php","dietest.php"),
                          "/var/www/html/dietest.php")
                    .copy(concatPaths(getDeployBase(), "client", "resources", "php","errortest.php"),
                          "/var/www/html/errortest.php")
                    .copy(concatPaths(getDeployBase(), "client", "resources", "php","exceptiontest.php"),
                          "/var/www/html/exceptiontest.php")
                    .copy(concatPaths(getDeployBase(), "client", "resources", "php","namespacetest.php"),
                          "/var/www/html/namespacetest.php")
                    .copy(concatPaths(getDeployBase(), "client", "resources", "php","\u6D4B\u8BD5_multibytetest.php"),
                          "/var/www/html/\u6D4B\u8BD5_multibytetest.php")
                    .copy(concatPaths(getDeployBase(), "client", "resources", "php","namespacetest.php"),
                          "/var/www/html/index.html")
                    .build();
            
        }

        @Override
        protected void initChmodAgentProbeFlow() {
            List<String> args = new ArrayList<String>();
            args.add("a+x");
            args.add(concatPaths(phpExtDirPath, "wily_php_agent.so"));

            chmodAgentProbeModuleFlowContext =
                new RunCommandFlowContext.Builder("chmod").args(args).build();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected String getDeployBase() {
            return getLinuxDeployBase();
        }

        @Override
        protected String getPathSeparator() {
            return LINUX_SEPARATOR;
        }
    }

    /**
     * Builder responsible for holding all necessary properties to instantiate
     * {@link com.ca.apm.transactiontrace.appmap.role.PhpAgentRole}
     */
    public static class Builder extends BuilderBase<Builder, PhpAgentRole> {

        private final String roleId;
        protected final ITasResolver tasResolver;

        protected GenericFlowContext deployAgentFlowContext;
        @Nullable
        protected RunCommandFlowContext startAgentCollectorFlowContext;
        @Nullable
        protected RunCommandFlowContext startAgentProbeFlowContext;
        protected FileModifierFlowContext copyAgentProbeFlowContext;
        @Nullable
        protected FileModifierFlowContext modifyFileFlowContext;
        @Nullable
        protected RunCommandFlowContext chmodAgentProbeModuleFlowContext;

        protected GenericFlowContext.Builder deployAgentFlowContextBuilder;
        protected FileModifierFlowContext.Builder copyAgentProbeFlowContextBuilder;
        protected FileModifierFlowContext.Builder modifyFileFlowContextBuilder;

        @Nullable
        protected String phpAgentVersion;
        protected boolean phpAgentCollectorAutoStart = false;
        protected PhpVersion phpVersion;
        protected String phpExtDirPath;
        protected String phpExtConfDirPath;
        protected String deployBase;
        @Nullable
        protected Collection<String> appendToPbdFile;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;

            deployAgentFlowContextBuilder = new GenericFlowContext.Builder();
            copyAgentProbeFlowContextBuilder = new FileModifierFlowContext.Builder();
            modifyFileFlowContextBuilder = new FileModifierFlowContext.Builder();
        }

        protected ArtifactPlatform getArtifactsPlatform() {
            return ArtifactPlatform.LINUX_AMD_64;
        }

        protected String getCollectorAgentControlCommand() {
            return "CollectorAgent.cmd";
        }

        @Override
        public PhpAgentRole build() {
            initDeployAgentFlow();
            initModifyPbdFileFlow();
            initCopyAgentProbeFlow();
            initChmodAgentProbeFlow();
            initStartAgentCollectorFlow();
            initStartAgentProbeFlow();

            return getInstance();
        }

        public Builder phpAgentVersion(String phpAgentVersion) {
            this.phpAgentVersion = phpAgentVersion;
            return builder();
        }

        public Builder collectorAgentAutoStart() {
            this.phpAgentCollectorAutoStart = true;
            return builder();
        }

        public Builder phpVersion(PhpVersion phpVersion) {
            this.phpVersion = phpVersion;
            return builder();
        }

        public Builder phpExtDirPath(String phpExtDirPath) {
            this.phpExtDirPath = phpExtDirPath;
            return builder();
        }

        public Builder phpExtConfDirPath(String phpExtConfDirPath) {
            this.phpExtConfDirPath = phpExtConfDirPath;
            return builder();
        }
        
        public Builder deployBase(String deployBase) {
            this.deployBase = deployBase;
            return builder();
        }

        public Builder appendToPbdFile(Collection<String> appendToPbdFile) {
            this.appendToPbdFile = appendToPbdFile;
            return builder();
        }

        protected void initDeployAgentFlow() {
            deployAgentFlowContext =
                deployAgentFlowContextBuilder
                    .artifactUrl(
                        tasResolver.getArtifactUrl(new PhpAgentArtifact(getArtifactsPlatform(),
                            tasResolver).createArtifact(phpAgentVersion)))
                    .destination(concatPaths(getDeployBase(), "phpAgent")).build();
        }

        protected void initModifyPbdFileFlow() {
            if (appendToPbdFile != null) {
                modifyFileFlowContext =
                    modifyFileFlowContextBuilder.append(
                        concatPaths(getDeployBase(), "phpAgent", "collector", "core", "config",
                            "php-sample.pbd"), appendToPbdFile).build();
            }
        }

        protected void initStartAgentCollectorFlow() {
            if (phpAgentCollectorAutoStart) {
                startAgentCollectorFlowContext =
                    new RunCommandFlowContext.Builder(getCollectorAgentControlCommand())
                        .args(Collections.singleton("start"))
                        .workDir(concatPaths(getDeployBase(), "phpAgent", "collector", "bin"))
                        .build();
            }
        }

        protected void initStartAgentProbeFlow() {
            String command = "service";
            List<String> arguments = new ArrayList<>();
             
            arguments.add("httpd");
            arguments.add("start");
            if (phpAgentCollectorAutoStart) {
                startAgentProbeFlowContext =
                    new RunCommandFlowContext.Builder(command)
                        .args(arguments)
                        .build();
            }
            
        }
        
        protected void initCopyAgentProbeFlow() {
            // todo
        }

        protected void initChmodAgentProbeFlow() {
            // noop
        }

        @Override
        protected PhpAgentRole getInstance() {
            return new PhpAgentRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }
}
