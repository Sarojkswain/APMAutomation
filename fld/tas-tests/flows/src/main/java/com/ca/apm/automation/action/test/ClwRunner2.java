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

package com.ca.apm.automation.action.test;

import com.ca.tas.builder.BuilderBase;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Contains utility methods to run CLW as a separate OS process.
 * @author sobar03
 */
public class ClwRunner2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClwRunner2.class);

    private final String emHost;

    private final String user;

    private final String password;

    private final int port;

    private final String clWorkstationJarFileLocation;

    private final String javaPath;

    private final String clwWorkStationDir;
    
    private final Map<String, String> transactionTraceProperties;
    
    private final int maxMemoryInMb;
    
    protected ClwRunner2(Builder builder) {
        emHost = builder.host;
        user = builder.user;
        password = builder.password;
        port = builder.port;
        clWorkstationJarFileLocation = builder.clWorkstationJarFileLocation;
        javaPath = builder.javaPath;
        clwWorkStationDir = builder.clwWorkStationDir;
        transactionTraceProperties = builder.transactionTraceProperties;
        maxMemoryInMb = builder.maxMemoryInMb;
    }

    /**
     * runs command line workstation commands against and EM
     * 
     * @param command
     *        the CLW command you want to run
     * @return an array of strings which is the output from CLW command
     *         execution.
     * @version 1.0
     */
    @NotNull
    public List<String> runClw(String command) {
        LOGGER.info("Running CLW command '{}' on {}:{}", command, emHost, port);
        final List<String> args = new ArrayList<>();
        args.add(javaPath);
        args.add("-Xmx" + maxMemoryInMb + "M");
        args.add("-Duser=" + escape(user));
        args.add("-Dpassword=" + escape(password));
        args.add("-Dhost=" + emHost);
        args.add("-Dport=" + port);
        if (transactionTraceProperties != null) {
            for (Entry<String, String> traceProp : transactionTraceProperties.entrySet()) {
                args.add("-D" + traceProp.getKey() + "=" + traceProp.getValue());
            }
        }
        args.add("-jar");
        args.add(clWorkstationJarFileLocation);
        args.add(command);
        final ProcessBuilder pb = new ProcessBuilder(args).redirectErrorStream(true);

        LOGGER.info("Running the CLW command : {}", args.toString().replace(",", ""));

        Process p;
        try {
            p = pb.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        final List<String> listOutput = new ArrayList<>();

        try (InputStream inputStream = p.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(inputStreamReader)) {
            // give it some cushion
            String line;

            /**
             * In case we are launching *.bat file that executes as a separate process, Windows
             * terminates the process, but input stream
             * does not receive EOF, so we need to implement non-blocking read and check whether the
             * process has terminated.
             * Linux behaves as expected
             */
            while (isAlive(p) || br.ready()) {
                while (br.ready()) {
                    line = br.readLine();
                    listOutput.add(line);
                    LOGGER.info(line);
                }
                Thread.sleep(500);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (!listOutput.isEmpty() && listOutput.get(0).startsWith("Usage: java ")) {
            final String msg =
                "Couldn't Connect EM \"" + emHost + "\" or the command \"" + command
                    + "\" is invalid CLW command";
            LOGGER.error(msg);
            throw new RuntimeException(msg);
        }

        if (listOutput
            .contains("Usage: java -Xmx128M <EM_logon> <Trace_props> -jar CLWorkstation.jar <Arguments>")) {
            // if usage is printed there was a problem with CLW connection with
            // the EM or the CLW command was incorrect
            final String msg =
                "Could not get a connection to the enterprise manager \"{}\" or the command \"{}\" is invalid CLW command";
            LOGGER.error(msg, emHost, command);
            throw new RuntimeException(msg);
        }
        return listOutput;
    }

    /**
     * Escapes spaces in cl
     * 
     * @param str
     *        String
     * @return String
     */
    private String escape(String str) {

        if (str == null) {
            return null;
        }

        if (str.trim().contains(" ")) {
            return "\"" + str + "\"";
        }

        return str;
    }

    private static boolean isAlive(Process p) {
        try {
            p.exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            return true;
        }
    }

    public String getClWorkstationJarFileLocation() {
        return clWorkstationJarFileLocation;
    }

    public String getEmHost() {
        return emHost;
    }

    public String getJavaPath() {
        return javaPath;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    public String getUser() {
        return user;
    }

    public String getClwWorkStationDir() {
        return clwWorkStationDir;
    }

    public static class Builder extends BuilderBase<Builder, ClwRunner2> {
        private static final String DEFAULT_HOST = "localhost";
        private static final String DEFAULT_USER = "Admin";
        private static final String DEFAULT_PASSWORD = "";
        private static final int DEFAULT_PORT = 5001;
        private static final String DEFAULT_CLW_FILENAME = "CLWorkstation.jar";
        private static final String DEFAULT_JAVA = "java";
        private static final int DEFAULT_MAX_MEMORY_IN_MB = 256;
        
        protected String host = DEFAULT_HOST;
        protected String user = DEFAULT_USER;
        protected String password = DEFAULT_PASSWORD;
        protected int port = DEFAULT_PORT;
        protected String clWorkstationJarFileLocation;
        protected String clwWorkStationDir;
        protected String javaPath = DEFAULT_JAVA;
        protected Map<String, String> transactionTraceProperties;
        protected int maxMemoryInMb = DEFAULT_MAX_MEMORY_IN_MB; 
        
        @Override
        public ClwRunner2 build() {
            clWorkstationJarFileLocation = clwWorkStationDir + DEFAULT_CLW_FILENAME;

            return getInstance();
        }

        public Builder clwWorkStationDir(String location) {
            clwWorkStationDir = location;

            return builder();
        }

        public Builder host(String host) {
            this.host = host;
            return builder();
        }

        public Builder javaPath(String javaPath) {
            this.javaPath = javaPath;
            return builder();
        }

        public Builder password(String password) {
            this.password = password;
            return builder();
        }

        public Builder port(int port) {
            this.port = port;
            return builder();
        }

        public Builder user(String user) {
            this.user = user;
            return builder();
        }

        /**
         * Sets <code>"Xmx"</code> option value in megabytes for CLWorkstation.
         * 
         * @param   xmxOptVal  megabytes value for max heap size
         * @return             this builder object
         */
        public Builder maxHeapSizeInMb(int xmxOptVal) {
            this.maxMemoryInMb = xmxOptVal;
            return builder();
        }
        
        /**
         * Adds transaction trace properties. Transaction trace properties can be (example): 
         * <ul>
         * <li>introscope.clw.tt.filename</li>
         * <li>introscope.clw.tt.dirname</li>
         * <li>introscope.clw.tt.console</li>
         * <li>..etc.</li>
         * </ul> 
         * 
         * @param   name   property name without prefix <code>"-D"</code>, that one is added automatically
         * @param   value  property value
         * @return  this builder object
         */
        public Builder addTransactionTraceProperty(String name, String value) {
            if (transactionTraceProperties == null) {
                transactionTraceProperties = new HashMap<>();
            }
            transactionTraceProperties.put(name, value);
            return this;
        }
        
        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected ClwRunner2 getInstance() {
            return new ClwRunner2(this);
        }
    }
}
