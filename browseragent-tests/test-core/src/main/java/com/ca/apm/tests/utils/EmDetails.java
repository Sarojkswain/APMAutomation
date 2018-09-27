package com.ca.apm.tests.utils;

import org.apache.http.util.Args;

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
 * EM details - Browser Agent
 *
 * @author - gupra04
 * 
 */

public class EmDetails {
    private String emHost;
    private int emPort;
    private String emUsername;
    private String emPassword;
    private String cemPort;
    private String clwJar;

    private EmDetails(Builder builder) {
        this.emHost = builder.emHost;
        this.emPort = builder.emPort;
        this.emUsername = builder.emUsername;
        this.emPassword = builder.emPassword;
        this.cemPort = builder.cemPort;
        this.clwJar = builder.clwJar;
    }

    public String getEmHost() {
        return emHost;
    }

    public void setEmHost(String emHost) {
        this.emHost = emHost;
    }

    public int getEmPort() {
        return emPort;
    }

    public void setEmPort(int emPort) {
        this.emPort = emPort;
    }

    public String getEmUsername() {
        return emUsername;
    }

    public void setEmUsername(String emUsername) {
        this.emUsername = emUsername;
    }

    public String getEmPassword() {
        return emPassword;
    }

    public void setEmPassword(String emPassword) {
        this.emPassword = emPassword;
    }

    public String getCemPort() {
        return cemPort;
    }

    public void setCemPort(String cemPort) {
        this.cemPort = cemPort;
    }

    public String getClwJar() {
        return clwJar;
    }

    public void setClwJar(String clwJar) {
        this.clwJar = clwJar;
    }

    public static class Builder {
        private String emHost;
        private int emPort;
        private String emUsername = "Admin";
        private String emPassword = "";
        private String cemPort;
        private String clwJar;

        public Builder() {}

        public EmDetails build() {
            Args.notNull(this.emHost, "EM HOST NAME IS REQUIRIED");
            Args.notNull(this.emPort, "EM PORT IS REQUIRIED");
            Args.notNull(this.emUsername, "EM USERNAME IS REQUIRIED");
            Args.notNull(this.cemPort, "CEM PORT IS REQUIRIED");
            Args.notNull(this.clwJar, "CLW JAR IS REQUIRIED");
            return new EmDetails(this);
        }

        public Builder emHost(String emHost) {
            this.emHost = emHost;
            return this;
        }

        public Builder emPort(int emPort) {
            this.emPort = emPort;
            return this;
        }

        public Builder emUsername(String emUsername) {
            this.emUsername = emUsername;
            return this;
        }

        public Builder emPassword(String emPassword) {
            this.emPassword = emPassword;
            return this;
        }

        public Builder cemPort(String cemPort) {
            this.cemPort = cemPort;
            return this;
        }

        public Builder clwJar(String clwJar) {
            this.clwJar = clwJar;
            return this;
        }
    }

}
