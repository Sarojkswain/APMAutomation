package com.ca.apm.tests.role;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.tests.flow.JarArchiveFlow;
import com.ca.apm.tests.flow.JarArchiveFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * @author kurma05
 */
public class UpdateMgmtModuleRole extends AbstractRole {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateMgmtModuleRole.class);
    public static final String IA_MM_JAR_RELATED_PATH = "/config/modules/APMInfrastructureMM.jar";
    public static final String DEFAULT_MM_JAR_RELATED_PATH = "/config/modules/DefaultMM.jar";
    private static final String MM_XML_FILE_NAME = "ManagementModule.xml";
    private String emHomeDir;
    private String unpackDir;
    private String mmJarFile;
    private HashMap<String,String> replacePairs;
  
    protected UpdateMgmtModuleRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.emHomeDir = builder.emHomeDir;
        this.unpackDir = builder.unpackDir;
        this.replacePairs = builder.replacePairs;
        this.mmJarFile = builder.mmJarFile;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        unpackJar(aaClient);
        modifyXml(aaClient);
        packJar(aaClient);
    }
    
    private void unpackJar(IAutomationAgentClient aaClient) {
        
        String jarFilePath = emHomeDir + mmJarFile;

        JarArchiveFlowContext context = new JarArchiveFlowContext.Builder()
            .archivePath(jarFilePath)
            .tempUnpackDir(unpackDir)
            .unpack(true)
            .build();
    
        runFlow(aaClient, JarArchiveFlow.class, context);    
    }
 
    private void modifyXml(IAutomationAgentClient aaClient) {
        
        String xmlPath = unpackDir + "/" + MM_XML_FILE_NAME;
        
        LOGGER.info("Updating file : {}", xmlPath);  
        FileModifierFlowContext context = new FileModifierFlowContext.Builder()
            .replace(xmlPath, replacePairs)
            .build();
        
        runFlow(aaClient, FileModifierFlow.class, context);
    }

    private void packJar(IAutomationAgentClient aaClient) {
        
        String jarFilePath = emHomeDir + mmJarFile;
    
        JarArchiveFlowContext context = new JarArchiveFlowContext.Builder()
            .archivePath(jarFilePath)
            .tempUnpackDir(unpackDir)
            .pack(true)
            .build();

        runFlow(aaClient, JarArchiveFlow.class, context);  
    }
  
    public static class LinuxBuilder extends Builder {

        public LinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
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

    public static class Builder extends BuilderBase<Builder, UpdateMgmtModuleRole> {

        private final String roleId;
        @SuppressWarnings("unused")
        private final ITasResolver tasResolver;
        protected String emHomeDir;
        protected String unpackDir; 
        protected HashMap<String,String> replacePairs;
        protected String mmJarFile = IA_MM_JAR_RELATED_PATH;
        
        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public UpdateMgmtModuleRole build() {
            return getInstance();
        }

        @Override
        protected UpdateMgmtModuleRole getInstance() {
            return new UpdateMgmtModuleRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
        
        public Builder emHomeDir(String emHomeDir) {
            this.emHomeDir = emHomeDir;
           
            return builder();
        }
        
        public Builder unpackDir(String unpackDir) {
            this.unpackDir = unpackDir;
           
            return builder();
        }
        
        public Builder mmJarFile(String mmJarFile) {
            this.mmJarFile = mmJarFile;
           
            return builder();
        }
        
        public Builder replacePairs(HashMap<String,String> replacePairs) {
            this.replacePairs = replacePairs;
           
            return builder();
        }               
    }
}