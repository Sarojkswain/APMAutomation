package com.ca.apm.commons.coda.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.ca.apm.tests.common.introscope.util.CLWBean;

public class RegressionBaseAgentTest
{

    int           agentPort = Integer.parseInt(System
                                    .getProperty("role_webapp.port"));

    public String agentHost = System.getProperty("testbed_webapp.hostname");

    /**
     * Test method to start agent.
     */

    @Test
    @Parameters(value = { "maxSleepStillAgentStarts" })
    public void startAgentWithPolling(long maxSleepStillAgentStarts)
    {
        //super.startAgent();
        try
        {
            long time = maxSleepStillAgentStarts; // 10 min.
            long timeElapsed = 0;
            while (!isPortAvailable(agentPort, agentHost))
            {
                System.out
                        .println("**********  Checking Agent is started ***********");
                Thread.sleep(60 * 1000);
                timeElapsed = timeElapsed + (60 * 1000);
                if (timeElapsed == maxSleepStillAgentStarts)
                {
                    Assert.assertTrue(false);
                    break;

                }
            }
        } catch (Exception e)
        {
            // e.printStackTrace();
        }
    }

    /**
     * Test method to start agent.
     */
    @Test
    public void stopAgentWithPolling()
    {
        try
        {
            if (isPortAvailable(agentPort, agentHost))
            {
                //super.stopAgent();
            } else
            {
                System.out.println("Agent is stop mode only");
            }
        } catch (Exception e)
        {

        }
    }

    /**
     * is port available
     * 
     * @param port
     * @param hostname
     */

    private boolean isPortAvailable(int port, String hostName) throws Exception
    {
        Socket soc = null;
        boolean isAvailable = false;
        try
        {

            soc = new Socket(hostName, port);
            isAvailable = soc.isBound();

        } catch (IOException e)
        {
            // e.printStackTrace();
            isAvailable = false;
        } finally
        {
            if (soc != null) soc.close();
        }
        return isAvailable;

    }

    public static final int SUCCESS = 1;

    public static final int FAILURE = 0;

    public void stopEM(CLWBean clw, String hostName, int port) throws Exception
    {
        String shutdown = "shutdown";
        System.out.println("Check for port " + port
                           + " available before shutdown "
                           + isPortAvailable(port, hostName));

        try
        {
            if (isPortAvailable(port, hostName))
            {
                clw.runCLW(shutdown).toString();

                System.out.println("EM Shutdown Successfull");
            } else
            {
                System.out.println("EM is stopped already");
            }
            while (isPortAvailable(port, hostName))
            {
                Util.sleep(1000 * 60);
            }
            Util.sleep(1000 * 60);

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private int lookForPortReady(String hostName, int port) throws Exception
    {
        int time = 5 * 60 * 1000; // 10 min.
        int timeElapsed = 0;
        while (!isPortAvailable(port, hostName))
        {
            Util.sleep(60 * 1000);
            if (time == timeElapsed)
            {
                System.out.println("EM Not Started after 10 minutes also");
                return FAILURE;
            }
            timeElapsed = timeElapsed + (60 * 1000);
            System.out.println("*** Waiting for EM to Start .............."
                               + timeElapsed);
            continue;
        }
        return SUCCESS;
    }

    public int invokeEMProcess(String command, String hostName, int port)
        throws Exception
    {
        Process process = null;

        try
        {
            System.out.println("Starting EM **** exec Run Started");
            process = Runtime.getRuntime().exec(command);

            return SUCCESS;

        } catch (Exception e)
        {
            e.printStackTrace();
            return FAILURE;

        } finally
        {
            if (process != null)
            {
                process.getErrorStream().close();
                process.getInputStream().close();
                process.getOutputStream().close();
                process.destroy();
            }
        }

    }

    /****
 * 
 */
    public static int startEM(String command, String hostName, int port)
        throws IOException
    {
        Process process = null;
        int status = 0;

        int index = command.lastIndexOf("/");
        String directoryLoc = command.substring(0, index);

        String[] cmd = { "cmd.exe", "/c", command };
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        pb.directory(new File(directoryLoc));
        process = pb.start();

        InputStreamReader inputstreamreader = new InputStreamReader(
                                                                    process.getInputStream());
        BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
        try
        {

            String line = bufferedreader.readLine();
            while ((line) != null)
            {

                if (line.toLowerCase()
                        .contains("introscope enterprise manager started"))
                {
                    System.out
                            .println("%%%%%%%%%%%%%% EM STARTED %%%%%%%%%%%%%%%%%");
                    status = SUCCESS;
                    break;
                }
                if ((line.toLowerCase()
                        .contains("a socket port was already in use"))
                    || (line.toLowerCase().contains("jvm_bind"))
                    || (line.toLowerCase()
                            .contains("introscope enterprise manager failed to start because")))
                {
                    line = bufferedreader.readLine();
                    System.out
                            .println("%%%%%%%%%%%%%% EM ALREADY FAILED %%%%%%%%%"
                                     + line);
                    status = FAILURE;
                    break;
                }
                line = bufferedreader.readLine();
            }
        } finally
        {
            process.getInputStream().close();
            process.getErrorStream().close();
            process.getOutputStream().close();
            process.destroy();
        }
        return status;
    }
}
