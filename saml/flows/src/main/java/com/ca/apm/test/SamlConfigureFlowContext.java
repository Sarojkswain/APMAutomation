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
package com.ca.apm.test;

import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.apm.automation.action.flow.IFlowContext;

public class SamlConfigureFlowContext implements IFlowContext {

    private String apmRootDir;
    private int wvPort;
    private int wsPort;

    public SamlConfigureFlowContext(Builder b) {
        apmRootDir = b.apmRootDir;
        wvPort = b.wvPort;
        wsPort = b.wsPort;
    }

    public String getApmRootDir() {
        return apmRootDir;
    }
    
    public int getWvPort() {
        return wvPort;
    }

    public int getWsPort() {
        return wsPort;
    }
    
    public static class Builder implements IBuilder<SamlConfigureFlowContext> {
        private String apmRootDir = "c:\\automation\\deployed\\em\\";
        private int wvPort;
        private int wsPort;

        public Builder apmRootDir(String apmRootDir) {
            this.apmRootDir = apmRootDir;
            return this;
        }
        
        public Builder wvPort(int port) {
            this.wvPort = port;
            return this;
        }

        public Builder wsPort(int port) {
            this.wsPort = port;
            return this;
        }
        
        @Override
        public SamlConfigureFlowContext build() {
            return new SamlConfigureFlowContext(this);
        }
    }
}
