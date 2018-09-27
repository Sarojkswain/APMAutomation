package com.ca.apm.commons.flow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.commons.coda.common.ApmbaseUtil;

/**
 * Created by nick on 8.10.14.
 */
@Flow
public class RunHvrAgentFlow implements IAutomationFlow {

  private static final Logger LOGGER = LoggerFactory.getLogger(RunHvrAgentFlow.class);
  private static final String JAVA_COMMAND_WINDOWS = "java.exe";
  protected Process subprocess;
  @FlowContext
  private RunHvrAgentFlowContext flowContext;
  private int bedInTime;

  @Override
  public void run() throws Exception {

    LOGGER.info("Beginning flow");

//    BackgroundExecution.Builder builder =
//        new BackgroundExecution.Builder(new File(flowContext.getInstallDir()),
//                                        hvrAgentCommand());

    if(flowContext.getAction().toLowerCase().contains("start"))
    {
//    builder.args(hvrAgentArguments());
    
    LOGGER.info("Executing HVR Agent start");
//    int exitValue = builder.build().go();
    
    List<String> commands = hvrAgentArguments();
    commands.add(0, hvrAgentCommand());
    
    subprocess = ApmbaseUtil.runCommand(commands, "C:\\");
   long processStartTime = System.currentTimeMillis();
   LOGGER.info("Process started at " + Long.toString(processStartTime));
   try (InputStream inputStream = subprocess.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader br = new BufferedReader(inputStreamReader)) {

     while ((isAlive(subprocess) && isBeddingIn(processStartTime)) || br.ready()) {
       while (br.ready()) {
         LOGGER.info(br.readLine());
       }
       Thread.sleep(500);
     }

     if (isAlive(subprocess)) {
       LOGGER.debug("Subprocess bedded in, returning success.");
       
     }
     else {
       LOGGER.debug("Subprocess no-longer running after %d seconds, returning failure.", (System.currentTimeMillis() - processStartTime) /1000);
     }

 } catch (IOException e) {
   LOGGER.error("IOException", e);
 }

    
//    if (exitValue != 0) {
//      throw new IllegalStateException("RunCommand has failed." + exitValue);
//    }

    LOGGER.info("Agent running");
    }
    else if(flowContext.getAction().toLowerCase().contains("stop"))
    {
        ApmbaseUtil.killProcess(subprocess);
    }
  }

  private String hvrAgentCommand() {
    return JAVA_COMMAND_WINDOWS;
  }

  private List<String> hvrAgentArguments() {

    List<String> args = new ArrayList<>();

    args.add("-cp");
    args.add(String.format("%1$sHVRAgent.jar;%1$sIntroscopeClient.jar;",
                           ensureEndsWithBackslash(flowContext.getInstallDir())));

    args.add("com.wily.introscope.tools.fakeagent.FakeAgent");

    args.add("replay");

    args.add("-host");
    args.add(flowContext.getEmHost());

    args.add("-port");
    args.add(Integer.toString(flowContext.getEmPort()));

    args.add("-username");

    args.add("admin");

    args.add("-loadfile");
    args.add(String.format("%s%s", ensureEndsWithBackslash(flowContext.getInstallDir()), flowContext.getFileToLoad()));

    args.add("-cloneconnections");
    args.add(flowContext.getCloneconnections());

    args.add("-cloneagents");
    args.add(flowContext.getCloneagents());

    args.add("-secondspertrace");
    args.add(flowContext.getSecondsPerTrace());

    return args;
  }

  private boolean isAlive(Process p) {
      try {
        p.exitValue();
        return false;
      } catch (IllegalThreadStateException e) {
        return true;
      }
    }

    private boolean isBeddingIn(long processStartTime) {
      return System.currentTimeMillis() < (processStartTime + (bedInTime * 1000));
    }

  
  private static String ensureEndsWithBackslash(final String input) {
    return input.endsWith("\\") ? input : input + "\\";
  }
}
