/**
 * 
 */
package com.ca.apm.systemtest.fld.role;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.systemtest.fld.artifact.EPAgentArtifact;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.EmRole;

/**
 * Installs the EPAgent package and sets up a batch file to start it.  Currently only available for Windows.
 * @author keyja01
 *
 */
/*
 * FIXME - create linux builder and add stop command
 */
public class EPAgentRole extends AbstractRole {
    public static final String START_EPAGENT_CTX = "START_EPAGENT";
    private FileModifierFlowContext createBatchFileCtx;
    private GenericFlowContext downloadCtx;
    
    protected EPAgentRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.createBatchFileCtx = builder.createBatchFileCtx;
        this.downloadCtx = builder.downloadCtx;
    }
    
    public static class Builder extends BuilderBase<Builder, EPAgentRole> {
        private static final String EP_AGENT_JAR = "EPAgent.jar";
        private static final String EPAGENT_PROPERTIES = "/epagent/IntroscopeEPAgent.properties";
        private static final String PROP_FILE_NAME = "IntroscopeEPAgent.properties";
        private static final String RUN_EP_AGENT_BAT = "runEpAgent.bat";
        
        private String roleId;
        private String version;
        private EmRole emRole;
        private String emHost;
        private String destination = "epagent";
        private ITasResolver resolver;
        private FileModifierFlowContext createBatchFileCtx;
        private GenericFlowContext downloadCtx;
        private ITasArtifact artifact;
        private String fileName;
        private String agentPropertiesFile;
        private Integer networkPort = 8000;
        private Integer httpPort = 8080;
        private String agentName;
        private int maxHeapsizeMB;
        private String agentHostName;
        private int emPort;

        @Override
        public EPAgentRole build() {
            createDownloadFlow();
            try {
                createBatchFile();
            } catch (IOException e) {
                throw new RuntimeException("Unable to read IntroscopeEPAgent.properties", e);
            }
            createStartStopContexts();
            return getInstance();
        }
        
        public Builder(String roleId, ITasResolver resolver) {
            this.roleId = roleId;
            this.resolver = resolver;
        }

        @Override
        protected Builder builder() {
            return this;
        }
        
        @Override
        protected EPAgentRole getInstance() {
            EPAgentRole role = new EPAgentRole(this);
            
            return role;
        }

        private void createStartStopContexts() {
            RunCommandFlowContext startEPAgentCtx = new RunCommandFlowContext.Builder(fileName)
                .workDir(deployDir()).doNotPrependWorkingDirectory()
                .build();
            getEnvProperties().add(START_EPAGENT_CTX, startEPAgentCtx);
        }
        
        private void createBatchFile() throws IOException {
            InputStream in = getClass().getResourceAsStream(EPAGENT_PROPERTIES);
            LineNumberReader reader = new LineNumberReader(new InputStreamReader(in));
            ArrayList<String> agentProperties = new ArrayList<>();
            String line;
            if (emRole != null) {
                emHost = resolver.getHostnameById(emRole.getRoleId());
                emPort = emRole.getEmPort();
            }
            while ((line = reader.readLine()) != null) {
                if (emHost != null) {
                    if (line.startsWith("introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT")) {
                        line = "introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT=" + emHost;
                    } else if (line.startsWith("introscope.agent.enterprisemanager.transport.tcp.port.DEFAULT")) {
                        line = "introscope.agent.enterprisemanager.transport.tcp.port.DEFAULT=" + emPort;
                    }
                }
                if (agentName != null) {
                    if (line.startsWith("introscope.agent.agentName")) {
                        //introscope.agent.agentName=MonkeyAgent2
                        line = "introscope.agent.agentName=" + agentName;
                    }
                }
                agentProperties.add(line);
            }
            if (networkPort != null) {
                agentProperties.add("introscope.epagent.config.networkDataPort=" + networkPort);
            }
            if (httpPort != null) {
                agentProperties.add("introscope.epagent.config.httpServerPort=" + httpPort);
            }
            if (agentHostName != null) {
                agentProperties.add("introscope.agent.hostName=" + agentHostName);
            }
            
            agentPropertiesFile = deployDir() + getPathSeparator() + PROP_FILE_NAME;
            
            // -Dcom.wily.introscope.epagent.properties="../IntroscopeEPAgent.properties"
            if (maxHeapsizeMB == 0) {
                maxHeapsizeMB = 48;
            }
            List<String> batchFileContent = Arrays.asList("java -jar " + agentJar() 
                + " -Xmx=" + maxHeapsizeMB + "m -Dcom.wily.introscope.epagent.properties=\"" + agentPropertiesFile + "\" > epagent.log");
            fileName = deployDir() + getPathSeparator() + RUN_EP_AGENT_BAT;
            
            createBatchFileCtx = new FileModifierFlowContext.Builder()
                .create(fileName, batchFileContent)
                .create(agentPropertiesFile, agentProperties)
                .build();
        }
        
        private void createDownloadFlow() {
            artifact = new EPAgentArtifact(resolver).createArtifact(version);
            URL url = resolver.getArtifactUrl(artifact);
            
            String downloadDir = deployDir();
            downloadCtx = new GenericFlowContext.Builder()
                .artifactUrl(url).notArchive()
                .destination(downloadDir + getPathSeparator() + EP_AGENT_JAR)
                .build();
        }
        
        private String agentJar() {
            return deployDir() + getPathSeparator() + EP_AGENT_JAR;
        }
        
        private String deployDir() {
            return getDeployBase() + destination;
        }
        
        /**
         * Set the port the EPA will listen on for HTTP connections
         * @param httpServerPort
         * @return
         */
        public Builder httpServerPort(Integer httpServerPort) {
            this.httpPort = httpServerPort;
            return this;
        }
        
        /**
         * Set the port the EPA will listen on for XML data
         * @param networkPort
         * @return
         */
        public Builder networkPort(Integer networkPort) {
            this.networkPort = networkPort;
            return this;
        }
        
        public Builder agentName(String agentName) {
            this.agentName = agentName;
            return this;
        }
        
        
        public Builder agentHostName(String agentHostName) {
            this.agentHostName = agentHostName;
            return this;
        }
        
        public Builder maxHeapMB(int maxHeapSizeMB) {
            this.maxHeapsizeMB = maxHeapSizeMB;
            return this;
        }
        
        /**
         * Set the version of EPA to download
         * @param version
         * @return
         */
        public Builder version(String version) {
            this.version = version;
            return this;
        }
        
        /**
         * Configures the EPA to use the specified collector
         * @param emRole
         * @return
         */
        public Builder em(EmRole emRole) {
            this.emRole = emRole;
            return this;
        }
        
        
        public Builder emHost(String emHost, int port) {
            this.emHost = emHost;
            this.emPort = port;
            return this;
        }
        
        
        /**
         * Set the destination directory, relative to $DEPLOY_BASE
         * @param destination
         * @return
         */
        public Builder destination(String destination) {
            this.destination = destination;
            return this;
        }
    }
    

    /* (non-Javadoc)
     * @see com.ca.tas.role.Deployable#deploy(com.ca.tas.client.IAutomationAgentClient)
     */
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        runFlow(aaClient, GenericFlow.class, downloadCtx);
        runFlow(aaClient, createBatchFileCtx);
    }

}
