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

package com.ca.apm.tests.utils.clw;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains utility methods to run CLW as a separate OS process
 */
public class ClwRunner {

    private static final Logger log = LoggerFactory.getLogger(ClwRunner.class);

    private String emHost = "localhost";

    private String user = "Admin";

    private String password = "";

    private int port = 5001;

    private String clWorkstationJarFileLocation = "./CLWorkstation.jar";

    private String javaPath = "java";

    public void setEmHost(String emHost) {
        this.emHost = emHost;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setJavaPath(String javaPath) {
        this.javaPath = javaPath;
    }

    /**
     * @param clWorkstationJarFileLocation
     *        the clWorkstationJarFileLocation to set
     */
    public void setClWorkstationJarFileLocation(String clWorkstationJarFileLocation) {
        this.clWorkstationJarFileLocation = clWorkstationJarFileLocation;
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
    public String[] runCLW(String command) throws Exception {
        final List<String> args = new ArrayList<String>();
        args.add(javaPath);
        args.add("-Xmx256M");
        args.add("-Duser=" + escape(user));
        args.add("-Dpassword=" + escape(password));
        args.add("-Dhost=" + emHost);
        args.add("-Dport=" + port);

        args.add("-jar");
        args.add(clWorkstationJarFileLocation);
        args.add(command);
        final ProcessBuilder pb = new ProcessBuilder(args).redirectErrorStream(true);

        log.info("Running the CLW command : " + args.toString().replace(",", ""));

        Process p;

        try {
            p = pb.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        final List<String> listOutput = new ArrayList<String>();

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
                    log.info(line);
                }
                Thread.sleep(500);
            }
        }

        if (listOutput.size() > 0 && listOutput.get(0).startsWith("Usage: java ")) {
            final String msg =
                "Couldn't Connect EM \"" + emHost + "\" or the command \"" + command
                    + "\" is invalid CLW command";
            log.error(msg);
            throw new RuntimeException(msg);
        }

        if (listOutput
            .contains("Usage: java -Xmx128M <EM_logon> <Trace_props> -jar CLWorkstation.jar <Arguments>")) {
            // if usage is printed there was a problem with CLW connection with
            // the EM or the CLW command was incorrect
            final String msg =
                "Could not get a connection to the enterprise manager \"" + emHost
                    + "\" or the command \"" + command + "\" is invalid CLW command";
            log.error(msg);
            throw new RuntimeException(msg);
        }
        return listOutput.toArray(new String[0]);
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

}
