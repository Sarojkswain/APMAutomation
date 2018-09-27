/*
 * Copyright (c) 2014 CA.  All rights reserved.
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

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;

/**
 * @author kurma05
 */
public class JarArchiveFlowContext implements IFlowContext {

    private String archivePath;
    private String tempUnpackDir;
    private boolean unpack = false;
    private boolean pack = false;
    
    protected JarArchiveFlowContext(Builder builder) {
        
        this.archivePath = builder.archivePath;
        this.tempUnpackDir = builder.tempUnpackDir;
        this.pack = builder.pack;
        this.unpack = builder.unpack;
    }
    
    public String getArchivePath() {
        return archivePath;
    }
   
    public String getTempUnpackDir() {
        return tempUnpackDir;
    }
    
    public boolean shouldPack() {
        return pack;
    }
    
    public boolean shouldUnpack() {
        return unpack;
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

    public static class Builder extends BuilderBase<Builder, JarArchiveFlowContext> {

        private String archivePath;
        private String tempUnpackDir;
        private boolean unpack = false;
        private boolean pack = false;

        @Override
        public JarArchiveFlowContext build() {

            return getInstance();
        }

        @Override
        protected JarArchiveFlowContext getInstance() {
            return new JarArchiveFlowContext(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
        
        public Builder archivePath(String archivePath) {
            this.archivePath = archivePath;

            return builder();
        }
          
        public Builder tempUnpackDir(String tempUnpackDir) {
            this.tempUnpackDir = tempUnpackDir;

            return builder();
        }
        
        public Builder pack(boolean pack) {
            this.pack = pack;

            return builder();
        }
        
        public Builder unpack(boolean unpack) {
            this.unpack = unpack;

            return builder();
        }
    }
}