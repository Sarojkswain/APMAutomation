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
public class RunCommandFlow implements IAutomationFlow {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(RunCommandFlow.class);
	private int bedInTime;
	protected Process subprocess;
	@FlowContext
	private RunCommandFlowContext flowContext;

	@Override
	public void run() throws Exception {

		LOGGER.info("Beginning flow");
		List<String> commands = runCommandArguments();
		subprocess = ApmbaseUtil.runCommand(commands, flowContext.getDir());
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
	}

	private List<String> runCommandArguments() {
		List<String> args = new ArrayList<String>();
		args.add(flowContext.getCommand());
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

}
