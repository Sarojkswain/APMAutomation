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
 * Flow to setup TIM settings and interface
 *
 */
public class TIMSettingsFlowContext implements IFlowContext {
    
    private final String requestType;
    private final String timHostname;
    private final String settingName;
    private final String settingValue;
    private final String networkInterfaces;

    public TIMSettingsFlowContext(Builder builder) {
        this.requestType = builder.requestType;
        this.timHostname = builder.timHostname;
        this.settingName = builder.settingName;
        this.settingValue = builder.settingValue;
        this.networkInterfaces = builder.networkInterfaces;
    }

    public String getRequestType() {
        return requestType;
    }

    public String getTimHostname() {
        return timHostname;
    }

    public String getSettingName() {
        return settingName;
    }
    
    public String getSettingValue() {
        return settingValue;
    }
    
    public String getNetworkInterfaces() {
        return networkInterfaces;
    }
    
    public static class Builder extends BuilderBase<Builder,TIMSettingsFlowContext> {

        private String requestType;
        private String timHostname;
        private String settingName;
        private String settingValue;
        private String networkInterfaces;

        public Builder() {
            ;
        }

        public Builder requestType(String requestType) {
            this.requestType = requestType;
            return this;
        }

        public Builder timHostname(String timHostname) {
            this.timHostname = timHostname;
            return this;
        }

        public Builder settingName(String settingName) {
            this.settingName = settingName;
            return this;
        }

        public Builder settingValue(String settingValue) {
            this.settingValue = settingValue;
            return this;
        }
        
        public Builder networkInterfaces(String networkInterfaces) {
            this.networkInterfaces = networkInterfaces;
            return this;
        }

        @Override
        public TIMSettingsFlowContext build() {
            TIMSettingsFlowContext flowContext = getInstance();
            Args.notNull(flowContext.requestType, "Request Type Header of TIM settings GETter");
            Args.notNull(flowContext.timHostname, "TIM hostname");

            return flowContext;
        }

        @Override
        protected TIMSettingsFlowContext getInstance() {
            return new TIMSettingsFlowContext(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }
}
