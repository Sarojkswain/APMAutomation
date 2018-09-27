package com.ca.apm.tests.flow;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;

/**
 * @author kurma05
 */
public class FileDownloadFlowContext implements IFlowContext {

    private String url;
    private String installDir;
    private boolean unpack = true;
    private String destFileName;
    
    protected FileDownloadFlowContext(Builder builder) {
        
        this.url = builder.url;
        this.installDir = builder.installDir;
        this.unpack = builder.unpack;
        this.destFileName = builder.destFileName;
    }
    
    public String getUrl() {
        return url;
    }
   
    public String getInstallDir() {
        return installDir;
    }
   
    public boolean shouldUnpack() {
        return unpack;
    }
    
    public String getDestFileName() {
        return destFileName;
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

    public static class Builder extends BuilderBase<Builder, FileDownloadFlowContext> {

        private String url;
        private String installDir;
        private boolean unpack = true;
        private String destFileName;

        @Override
        public FileDownloadFlowContext build() {

            return getInstance();
        }

        @Override
        protected FileDownloadFlowContext getInstance() {
            return new FileDownloadFlowContext(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
        
        public Builder installDir(String installDir) {
            this.installDir = installDir;
            return builder();
        }
          
        public Builder url(String url) {
            this.url = url;
            return builder();
        }        
      
        public Builder unpack(boolean unpack) {
            this.unpack = unpack;
            return builder();
        }
        
        public Builder destFileName(String destFileName) {
            this.destFileName = destFileName;
            return builder();
        }
    }
}