/*
 * Copyright (c) 2014 CA. All rights reserved.
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

package com.ca.apm.systemtest.fld.flow;

import org.apache.http.util.Args;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;

/**
 * Flow to setup AGC register
 *
 */
public class AGCRegisterFlowContext implements IFlowContext {
    
    private final String hostName;
    private final String emWvPort;
    private final String wvHostName;
    private final String wvPort;
    private final String agcHostName;
    private final String agcEmWvPort;
    private final String agcWvPort;

    public AGCRegisterFlowContext(Builder builder) {
        this.hostName = builder.hostName;
        this.emWvPort = builder.emWvPort;
        this.wvHostName = builder.wvHostName;
        this.wvPort = builder.wvPort;
        this.agcHostName = builder.agcHostName;
        this.agcEmWvPort = builder.agcEmWvPort;
        this.agcWvPort = builder.agcWvPort;
    }

    public String getHostName() {
        return hostName;
    }
    
    public String getEmWvPort() {
        return emWvPort;
    }
    
    public String getWvHostName() {
        return wvHostName;
    }
    
    public String getWvPort() {
        return wvPort;
    }
    
    public String getAGCHostName() {
        return agcHostName;
    }
    
    public String getAgcEmWvPort() {
        return agcEmWvPort;
    }
    
    public String getAgcWvPort() {
        return agcWvPort;
    }

    public static class Builder extends BuilderBase<Builder,AGCRegisterFlowContext> {

        private String hostName;
        private String emWvPort;
        private String wvHostName;
        private String wvPort;
        private String agcHostName;
        private String agcEmWvPort;
        private String agcWvPort;

        public Builder() {
            ;
        }

        public Builder hostName(String hostName) {
            this.hostName = hostName;
            return this;
        }
        
        public Builder emWvPort(String emWvPort) {
            this.emWvPort = emWvPort;
            return this;
        }
        
        public Builder wvHostName(String wvHostName) {
            this.wvHostName = wvHostName;
            return this;
        }
        
        public Builder wvPort(String wvPort) {
            this.wvPort = wvPort;
            return this;
        }
        
        public Builder agcHostName(String agcHostName) {
            this.agcHostName = agcHostName;
            return this;
        }
        
        public Builder agcEmWvPort(String agcEmWvPort) {
            this.agcEmWvPort = agcEmWvPort;
            return this;
        }
        
        public Builder agcWvPort(String agcWvPort) {
            this.agcWvPort = agcWvPort;
            return this;
        }

        @Override
        public AGCRegisterFlowContext build() {
            AGCRegisterFlowContext flowContext = getInstance();
            Args.notNull(flowContext.hostName, "Follower hostname");
            //Args.notNull(flowContext.wvHostName, "Follower WebView hostname");
            //Args.notNull(flowContext.agcHostName, "AGC hostname");

            return flowContext;
        }

        @Override
        protected AGCRegisterFlowContext getInstance() {
            return new AGCRegisterFlowContext(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }
}
