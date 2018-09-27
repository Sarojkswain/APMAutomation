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
package com.ca.apm.siteminder;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.apm.automation.action.flow.IFlowContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author surma04
 */
public class ServletExecFlowContext implements IFlowContext {

    private String installer;
    private String responseFile;
    private String javaPath;
    private String installerDir;
    private String apacheDir;
    private String hostName;
    private String targetDir;
    private String webAgentDir;

    /**
     * @param builder
     */
    public ServletExecFlowContext(Builder builder) {
        this.installer = builder.installerName;
        this.responseFile = builder.responseFile;
        this.javaPath = builder.java;
        this.installerDir = builder.installerDir;
        this.apacheDir = builder.apacheDir;
        this.targetDir = builder.targetDir;
        this.webAgentDir = builder.webAgentDir;
    }

    public static class Builder implements IBuilder<ServletExecFlowContext> {
        private String webAgentDir = "C:\\CA\\install\\webagent";
        ;
        private String targetDir = "C:\\CA\\install\\ServletExec";
        private String apacheDir;
        // TODO need to parameterize this
        private String temp = "C:\\CA\\sourcesUnpacked\\install\\thirdparty-tools\\";
        private String responseFile;
        private String installerDir;
        private String installerName;
        private String java;

        /*
         * (non-Javadoc)
         * 
         * @see com.ca.apm.automation.action.flow.IBuilder#build()
         */
        @Override
        public ServletExecFlowContext build() {
            return new ServletExecFlowContext(this);
        }

        public Builder webAgentDir(@NotNull final String webAgentDir) {
            this.webAgentDir = webAgentDir;
            return this;
        }

        public Builder installer(@NotNull final String installerName) {
            this.installerName = installerName;
            return this;
        }

        public Builder responseFile(@NotNull final String responseFile) {
            this.responseFile = responseFile;
            return this;
        }

        public Builder apacheDir(@NotNull final String apacheLocation) {
            this.apacheDir = apacheLocation;
            return this;
        }

        public Builder installationDir(@NotNull final String installationDir) {
            this.targetDir = installationDir;
            return this;
        }

        /**
         * @param javaPath
         */
        public Builder javaPath(@NotNull final String javaPath) {
            this.java = javaPath;
            return this;
        }

        /**
         * @param folderName the full path to the installer unpacked in the temp folder
         */
        public Builder installerDir(@NotNull final String folderName) {
            this.installerDir = temp + folderName;
            return this;
        }
    }

    public String createResponseFile() throws IOException {
        File respFile = new File(getResponseFileLocation());
        FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("ServletExecResp.txt"), respFile);

        this.hostName = "localhost"; //InetAddress.getLocalHost().getCanonicalHostName();
        final String encoding = System.getProperty("file.encoding");
        FileUtils.write(respFile, FileUtils.readFileToString(respFile, encoding).
            replace("%hostname%", hostName).
            replace("%targetDir%", targetDir).
            replaceAll("%apacheDir%", getApacheDir()), encoding);

        return respFile.getPath();
    }

    /**
     * @return
     */
    public String getApacheDir() {
        return this.apacheDir;
    }
    
    public String getServletExecInstallDir() {
        return this.targetDir;
    }

    /**
     * @return
     */
    private String getResponseFileLocation() {
        return this.installerDir + "seResp.iss";
    }

    /**
     * @return
     */
    public String getInstallerName() {
        return this.installer;
    }

    /**
     * @return
     * @throws IOException if context file read/creation failed
     */
    public String getReponseFile() throws IOException {
        if (this.responseFile == null) {
            responseFile = createResponseFile();
        }
        return this.responseFile;
    }

    /**
     * @return path to the JRE location
     */
    public String getJavaPath() {
        return this.javaPath.replace("\\", "\\\\");
    }

    public String getHostName() {
        return this.hostName;
    }

    /**
     * @return location of the directory with the installer
     */
    public String getInstallerDir() {
        return this.installerDir;
    }

    /**
     * @return the name of the start script to be modified - currently only win platform supported
     */
    public String getStartScriptFile() {
        String servletExecInstanceDir = getServletExecInstanceDir();
        return servletExecInstanceDir + "\\StartServletExec.bat";
    }

    /**
     * @return the name of the start script to be modified - currently only win platform supported
     */
    public String getStopScriptFile() {
        String servletExecInstanceDir = getServletExecInstanceDir();
        return servletExecInstanceDir + "\\StopServletExec.bat";
    }

    /**
     * @return the path to the instance of the installed SE instance (= targetDir\se-{hostname})
     */
    private String getServletExecInstanceDir() {
        return this.targetDir + "\\se-" + this.hostName;
    }

    /**
     * @return path the affwebservices folder within the SE instance folder
     */
    public String getAffWSDir() {
        return getServletExecInstanceDir() + "\\webapps\\default\\affwebservices";
    }

    /**
     * @return
     */
    public String getWebAgentDir() {
        return this.webAgentDir;
    }

    /**
     * @return path to the WebAgent.conf file under conf folder of the Apache installation
     */
    public String getApacheWebAgentConf() {
        return this.apacheDir + "\\\\conf\\\\WebAgent.conf";
    }
}
