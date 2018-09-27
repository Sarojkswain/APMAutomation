package com.ca.apm.tests.utils;

import org.apache.http.util.Args;

import com.ca.apm.tests.utils.constants.AgentPropertyConstants.AgentDefaults;
import com.ca.tas.builder.TasBuilder;

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

/**
 * Agent / Application Server details - Browser Agent
 *
 * @author - gupra04
 * 
 */

public class AgentDetails {
    private String agentHost;
    private String agentHostUserName;
    private String agentHostPassword;
    private String agentHome;
    private String agentProfileFileFullPath;
    private String agentName;
    private String agentProcessName;
    private String agentLogFile;

    private String agentJsExtensionFileLocation;

    private String applicationServer;
    private String applicationServerPort;

    private String transactionTraceDirectory;
    private String transactionTraceFile;

    private AgentDetails(Builder builder) {
        this.agentHost = builder.agentHost;
        this.agentHostUserName = builder.agentHostUserName;
        this.agentHostPassword = builder.agentHostPassword;
        this.agentHome = builder.agentHome;
        this.agentProfileFileFullPath = builder.agentProfileFileFullPath;
        this.agentName = builder.agentName;
        this.agentProcessName = builder.agentProcessName;
        this.agentLogFile = builder.agentLogFile;

        this.agentJsExtensionFileLocation = builder.agentJsExtensionFileLocation;

        this.applicationServer = builder.applicationServer;
        this.applicationServerPort = builder.applicationServerPort;

        this.transactionTraceDirectory = builder.transactionTraceDirectory;
        this.transactionTraceFile = builder.transactionTraceFile;
    }

    public String getAgentHost() {
        return agentHost;
    }

    public void setAgentHost(String agentHost) {
        this.agentHost = agentHost;
    }

    public String getAgentHostUsername() {
        return agentHostUserName;
    }

    public void setAgentHostUsername(String agentHostUserName) {
        this.agentHostUserName = agentHostUserName;
    }

    public String getAgentHostPassword() {
        return agentHostPassword;
    }

    public void setAgentHostPassword(String agentHostPassword) {
        this.agentHostPassword = agentHostPassword;
    }

    public String getAgentHome() {
        return agentHome;
    }

    public void setAgentHome(String agentHome) {
        this.agentHome = agentHome;
    }

    public String getAgentProfileFileFullPath() {
        return agentProfileFileFullPath;
    }

    public void setAgentProfileFileFullPath(String agentProfileFileFullPath) {
        this.agentProfileFileFullPath = agentProfileFileFullPath;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getAgentProcessName() {
        return agentProcessName;
    }

    public void setAgentProcessName(String agentProcessName) {
        this.agentProcessName = agentProcessName;
    }

    public String getAgentLogFile() {
        return agentLogFile;
    }

    public void setAgentLogFile(String agentLogFile) {
        this.agentLogFile = agentLogFile;
    }

    public String getAgentJsExtensionFileLocation() {
        return agentJsExtensionFileLocation;
    }

    public void setAgentJsExtensionFileLocation(String agentJsExtensionFileLocation) {
        this.agentJsExtensionFileLocation = agentJsExtensionFileLocation;
    }

    public String getApplicationServer() {
        return applicationServer;
    }

    public void setApplicationServer(String applicationServer) {
        this.applicationServer = applicationServer;
    }

    public String getApplicationServerPort() {
        return applicationServerPort;
    }

    public void setApplicationServerPort(String applicationServerPort) {
        this.applicationServerPort = applicationServerPort;
    }

    public String getTransactionTraceDirectory() {
        return transactionTraceDirectory;
    }

    public void setTransactionTraceDirectory(String transactionTraceDirectory) {
        this.transactionTraceDirectory = transactionTraceDirectory;
    }

    public String getTransactionTraceFile() {
        return transactionTraceFile;
    }

    public void setTransactionTraceFile(String transactionTraceFile) {
        this.transactionTraceFile = transactionTraceFile;
    }

    public static class Builder {
        private String agentHost;
        private String agentHostUserName;
        private String agentHostPassword;
        private String agentHome;
        private String agentProfileFileFullPath;
        private String agentName;
        private String agentProcessName;
        private String agentLogFile;

        private String agentJsExtensionFileLocation;

        private String applicationServer;
        private String applicationServerPort;

        private String transactionTraceDirectory;
        private String transactionTraceFile;

        public Builder() {}

        public AgentDetails build() {
            Args.notNull(this.agentHost, "AGENT HOST NAME IS REQUIRIED");
            Args.notNull(this.agentHostUserName, "AGENT HOST USER NAME IS REQUIRIED");
            Args.notNull(this.agentHostPassword, "AGENT HOST PASSWORD IS REQUIRIED");
            Args.notNull(this.agentHome, "AGENT HOME IS REQUIRIED");
            Args.notNull(this.agentProfileFileFullPath, "AGENT PROFILE FULL PATH NAME IS REQUIRIED");
            Args.notNull(this.agentName, "AGENT NAME IS REQUIRIED");
            Args.notNull(this.agentProcessName, "AGENT PROCESS HOST NAME IS REQUIRIED");
            Args.notNull(this.applicationServer, "APPLICATION SERVER NAME IS REQUIRIED");
            Args.notNull(this.applicationServerPort, "APPLICATION SERVER PORT IS REQUIRIED");

            if (this.agentLogFile == null) {
                this.agentLogFile = this.agentHome + AgentDefaults.DEFAULT_LOG_FILE;
            }
            if (this.transactionTraceDirectory == null) {
                this.transactionTraceDirectory = TasBuilder.WIN_SOFTWARE_LOC;
            }
            if (this.transactionTraceFile == null) {
                this.transactionTraceFile = AgentDefaults.TRANSACTION_TRACE_FILE;
            }
            if (this.agentJsExtensionFileLocation == null) {
                this.agentJsExtensionFileLocation =
                    this.agentHome + AgentDefaults.DEFAULT_EXTERNAL_JS_EXTENSION_FILE;
            }

            return new AgentDetails(this);
        }

        public Builder agentHost(String agentHost) {
            this.agentHost = agentHost;
            return this;
        }

        public Builder agentHostUserName(String agentHostUserName) {
            this.agentHostUserName = agentHostUserName;
            return this;
        }

        public Builder agentHostPassword(String agentHostPassword) {
            this.agentHostPassword = agentHostPassword;
            return this;
        }

        public Builder agentHome(String agentHome) {
            this.agentHome = agentHome;
            return this;
        }

        public Builder agentProfileFileFullPath(String agentProfileFileFullPath) {
            this.agentProfileFileFullPath = agentProfileFileFullPath;
            return this;
        }

        public Builder agentName(String agentName) {
            this.agentName = agentName;
            return this;
        }

        public Builder agentProcessName(String agentProcessName) {
            this.agentProcessName = agentProcessName;
            return this;
        }

        public Builder agentLogFile(String agentLogFile) {
            this.agentLogFile = agentLogFile;
            return this;
        }

        public Builder agentJsExtensionFileLocation(String agentJsExtensionFileLocation) {
            this.agentJsExtensionFileLocation = agentJsExtensionFileLocation;
            return this;
        }

        public Builder applicationServer(String applicationServer) {
            this.applicationServer = applicationServer;
            return this;
        }

        public Builder applicationServerPort(String applicationServerPort) {
            this.applicationServerPort = applicationServerPort;
            return this;
        }

        public Builder transactionTraceDirectory(String transactionTraceDirectory) {
            this.transactionTraceDirectory = transactionTraceDirectory;
            return this;
        }

        public Builder transactionTraceFile(String transactionTraceFile) {
            this.transactionTraceFile = transactionTraceFile;
            return this;
        }

    }
}
