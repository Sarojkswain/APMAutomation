/*
 * Copyright (c) 2015 CA. All rights reserved.
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

package com.ca.apm.tests.utils.emutils;

import com.ca.apm.tests.utils.clw.ClwUtils;
import com.ca.apm.tests.utils.configutils.PropertiesUtility;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.Os;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

/**
 * Contains utility methods for use on the same server which runs test (
 * multi-server scenarios are not supported here )
 */
public class EmBatLocalUtils {

    private static final Logger log = LoggerFactory.getLogger(EmBatLocalUtils.class);

    private static Long SHUTDOWN_TIMEOUT = 120 * 1000L;

    private static Long STARTUP_TIMEOUT = 120 * 1000L;

    /**
     * Stops EM on local PC, initiates shutdown then waits for
     * "Orderly shutdown complete." message in log.
     * 
     * @author sobar03
     * 
     */
    public static void stopLocalEm(EmConfiguration config) throws Exception {
        log.info("About to shutdown EM from " + config.getInstallPath());
        final long started = System.currentTimeMillis();

        final ClwUtils cu = new ClwUtils();
        cu.setClWorkstationJarFileLocation(config.getClwPath());
        cu.setPort(config.getEmPort());
        cu.runClw("shutdown");

        waitForKeywordInLog(config.getLogPath(), "[Manager] Orderly shutdown complete.",
            SHUTDOWN_TIMEOUT);
        waitTillPortIsAvailable(config.getEmPort(), 10000L);

        final long msecs = System.currentTimeMillis() - started;
        log.info("EM took " + msecs + " msecs to shutdown");
    }


    /**
     * Checks if the port is busy by creating socket and checking for success.
     * 
     * @author sobar03
     * @throws InterruptedException
     * 
     */
    public static void waitTillPortIsBusy(int port, Long timeout) throws InterruptedException {
        Long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < timeout) {
            try (Socket socket = new Socket("localhost", port);) {
                log.info("Port " + port + " took " + (System.currentTimeMillis() - start)
                    + " msecs to become busy");
                return;
            } catch (IOException e) {
                long elapsed = (System.currentTimeMillis() - start) / 1000;
                log.info("Port " + port + " is not busy yet. Time elapsed[s] : " + elapsed);
                Thread.sleep(5 * 1000);
            }
        }
        throw new RuntimeException("Port " + port + " is not busy after " + timeout + " msecs");
    }

    /**
     * Checks if the port is available by creating socket and checking for
     * success. Throws RuntimeException if port is not available within timeout
     * 
     * @author sobar03
     * 
     */
    public static void waitTillPortIsAvailable(int port, Long timeout) {
        Long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < timeout) {
            try (Socket socket = new Socket("localhost", port)) {
                long elapsed = (System.currentTimeMillis() - start) / 1000;
                log.info("Port " + port + " is not available yet. Time elapsed[s] : " + elapsed);

                Thread.sleep(5 * 1000);

            } catch (IOException e) {
                log.info("Port " + port + " took " + (System.currentTimeMillis() - start)
                    + " msecs to become available");
                return;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Port " + port + " is not available after wait of " + timeout
            + " msecs");
    }

    /**
     * Looks for some keyword in log file, check is repeated every second.
     * There is circa 20[ms] delay while it goes to end of file and starts waiting
     * for keyword.
     * 
     * @author sobar03
     * @throws InterruptedException
     * @throws IOException
     * @throws FileNotFoundException
     * 
     */
    public static void waitForKeywordInLog(String logfilePath, String keyword, Long timeout)
        throws InterruptedException, FileNotFoundException, IOException {

        Long start = System.currentTimeMillis();
        log.info("Waiting for '" + keyword + "' in " + logfilePath);
        try (FileReader fr = new FileReader(logfilePath);
            BufferedReader br = new BufferedReader(fr);) {
            String line = br.readLine();

            while (line != null) {
                line = br.readLine();
            }


            while (true) {
                line = br.readLine();
                if (line == null) {
                    Thread.sleep(1 * 1000);

                } else {
                    log.info("log -> " + line);
                    if (line.contains(keyword)) {
                        long elapsed = System.currentTimeMillis() - start;
                        log.info("Keyword found in " + elapsed + " miliseconds");
                        return;
                    }
                }

                if (System.currentTimeMillis() - start > timeout) {
                    throw new RuntimeException("Keyword '" + keyword
                        + "' not found in log within timeout. Timeout : " + timeout);
                }

            }
        }

    }

    /**
     * Looks through log and checks if some keyword appears in it.
     * 
     * 
     * @param keyword
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static boolean isKeywordInLog(String logfilePath, String keyword)
        throws FileNotFoundException, IOException {

        log.info("Looking for " + keyword + " in " + logfilePath);
        try (FileReader fr = new FileReader(logfilePath);
            BufferedReader br = new BufferedReader(fr);) {
            String line = br.readLine();
            while (line != null) {
                if (line.contains(keyword)) {
                    log.info("Keyword found");
                    return true;
                }
                line = br.readLine();
            }

        }
        return false;
    }

    /**
     * Starts EM on local machine. Asserts it's really started by actively
     * waiting for EM port to be busy.
     * 
     * @throws Exception if EM does not start within STARTUP_TIMEOUT
     * 
     * @author turlu01
     */
    public static void startLocalEm(EmConfiguration config) throws Exception {
        log.info("About to start EM from " + config.getInstallPath());
        final long started = System.currentTimeMillis();

        String emInstallDir = config.getInstallPath();
        String startCommand = getEmExecutableFile(new File(emInstallDir)).getAbsolutePath();

        ProcessBuilder pb;

        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            pb = new ProcessBuilder("cmd.exe", "/C", startCommand);
        } else {
            pb = new ProcessBuilder(startCommand);
        }

        pb.redirectErrorStream(true);
        Process subprocess = pb.start();

        InputStream inputStream = subprocess.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader br = new BufferedReader(inputStreamReader);

        String textToMatch = "Introscope Enterprise Manager started";
        String line;
        while ((line = br.readLine()) != null) {
            log.info(line);
            if (line.contains(textToMatch)) {
                log.info("Found what we have been looking for. EM should be started");
                break;
            }
        }

        // assert EM is started. isPortBusy actively waits with timeout
        EmBatLocalUtils.waitTillPortIsBusy(config.getEmPort(), STARTUP_TIMEOUT);

        final long msecs = System.currentTimeMillis() - started;
        log.info("EM took " + msecs + " msecs to start");
    }

    /**
     * Starts EM on local machine using WatchDog. Asserts it's really started by
     * actively waiting for EM port to be busy.
     * 
     * @throws ExecuteException if execution of EM binary failed
     * @throws Exception if EM does not start within STARTUP_TIMEOUT
     * @throws IOException in case of IO error
     * 
     * @author turlu01
     */
    public static void startEmByWatchdog(String emInstallDir, int emPort) throws ExecuteException,
        IOException, Exception {
        CommandLine cmdLine = new CommandLine("java");
        cmdLine.addArgument("-jar");
        cmdLine.addArgument("WatchDog.jar");
        cmdLine.addArgument("start");

        DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(new File(emInstallDir + File.separator + "bin"));
        executor.execute(cmdLine);

        EmBatLocalUtils.waitTillPortIsBusy(emPort, STARTUP_TIMEOUT);
    }

    /**
     * Returns the EM binary executable file
     *
     * If executable is not found, an <code>IllegalStateException</code> is thrown
     *
     * @return Introscope's binary executable file
     */
    public static File getEmExecutableFile(File emInstallDir) {
        log.info("Scanning " + emInstallDir + " for " + "Introscope_Enterprise_Manager");
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(emInstallDir);
        ds.setIncludes(new String[] {"**\\Introscope_Enterprise_Manager*"});
        ds.setExcludes(new String[] {"**\\*lax"});
        ds.scan();
        if (ds.getIncludedFiles().length == 0) {
            throw new IllegalStateException("Could not find EM executable");
        }

        return new File(emInstallDir, ds.getIncludedFiles()[0]);
    }

    /**
     * Translates hostname into fully-qualified domain name
     */
    public static String hostnameToFqdn(String hostname) throws UnknownHostException {
        final InetAddress addr = InetAddress.getByName(hostname);
        String ret = addr.getCanonicalHostName();
        return ret;
    }

    /**
     * This method enable secured communication of EM
     * 
     * @param path - EM config file
     * @throws Exception
     */
    public static void setUpHttpsProperties(String path) throws Exception {
        HashMap<String, String> propertiesToSet = new HashMap<String, String>();
        propertiesToSet.put("introscope.enterprisemanager.enabled.channels", "channel1,channel2");
        propertiesToSet.put("introscope.enterprisemanager.webserver.jetty.configurationFile",
            "em-jetty-config.xml");
        PropertiesUtility.saveProperties(path, propertiesToSet, true);

    }
}
