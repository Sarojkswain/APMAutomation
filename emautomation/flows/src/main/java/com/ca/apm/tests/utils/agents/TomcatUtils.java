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
package com.ca.apm.tests.utils.agents;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.plexus.util.Os;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.tests.utils.emutils.EmBatLocalUtils;

/**
 * Utility class to start/stop Tomcat from tests
 */
public class TomcatUtils {

    private final static int PORT_NUMBER = 8080;
    private final static long TIMEOUT = 120000;

    private static final Logger log = LoggerFactory.getLogger(TomcatUtils.class);

    /**
     * Starts Tomcat in the given folder
     */
    public static void startTomcat(String tomcatDir) throws Exception {
        String path = tomcatDir + File.separator + "bin" + File.separator;
        path += Os.isFamily(Os.FAMILY_WINDOWS) ? "startup.bat" : "startup.sh";
        runShellComand(tomcatDir, path);
        EmBatLocalUtils.waitTillPortIsBusy(PORT_NUMBER, TIMEOUT);
        Thread.sleep(5000);
    }


    /**
     * Shutdowns Tomcat in a given folder
     */
    public static void stopTomcat(String tomcatDir) throws Exception {
        String path = tomcatDir + File.separator + "bin" + File.separator;
        path += Os.isFamily(Os.FAMILY_WINDOWS) ? "shutdown.bat" : "shutdown.sh";
        runShellComand(tomcatDir, path);
        EmBatLocalUtils.waitTillPortIsAvailable(PORT_NUMBER, TIMEOUT);
        Thread.sleep(5000);
    }

    /**
     * Executes OS shell command
     * 
     * @param workdir - work dir
     * @param args - executable, flags and arguments passed as is to ProcessBuilder
     * @return command's output
     */
    private static List<String> runShellComand(String workdir, String... args) throws Exception {
        log.info("Running shell command : " + Arrays.asList(args).toString().replace(",", ""));

        final ProcessBuilder pb =
            new ProcessBuilder(args).directory(new File(workdir)).redirectErrorStream(true);
        Process p;
        p = pb.start();

        final List<String> ret = new ArrayList<String>();
        try (InputStream inputStream = p.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(inputStreamReader)) {

            /**
             * In case we are launching *.bat file that executes as a separate process, Windows
             * terminates the process, but input stream
             * does not receive EOF, so we need to implement non-blocking read and check whether the
             * process has terminated.
             * Linux behaves as expected
             */
            while (isAlive(p) || br.ready()) {
                while (br.ready()) {
                    final String line = br.readLine();
                    ret.add(line);
                    log.info(line);
                }
                Thread.sleep(500);
            }
        }
        return ret;
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
