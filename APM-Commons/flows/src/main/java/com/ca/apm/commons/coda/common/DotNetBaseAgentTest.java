/**
 * 
 */
package com.ca.apm.commons.coda.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * @author Administrator
 *
 */
public class DotNetBaseAgentTest {
	
	
	private static final String CMD_NET_START = "cmd /c net start ";
	private static final String CMD_NET_STOP = "cmd /c net stop ";
	private static final String CMD_IISRESET = "cmd /c iisreset";
	private static final String IIS_ADMIN_SERVICE ="IIS Admin Service";

	String agentConfigLoc = System.getProperty ("role_dotnet-agent.install.dir")+ "\\" + System.getProperty ("role_dotnet-agent.install.dir.name");
	String agentProfilePath = agentConfigLoc +"\\"+ AutomationConstants.INTROSCOPE_PROFILE ;
	
	String agentLogPrefix = System.getProperty ("role_client.log.prefix");
	String agentLogPath = System.getProperty ("results.dir") + "/" + agentLogPrefix + ".log";
	String agentAutoprobeLogPath = System.getProperty ("results.dir") + "/" + agentLogPrefix + ".Autoprobe.log";

	
	
	
	@Test (enabled=true)
	@Parameters(value = {"autonaming", "agentName", "agentProcess", "agentLogPrefix"})
  	public void updateAgentProfileBase (String autonaming, String agentName,
  									String agentProcess, String agentLogPrefix) {

		try {

			agentLogPath = System.getProperty ("results.dir") + "/" + agentLogPrefix + ".log";
			agentAutoprobeLogPath = System.getProperty ("results.dir") + "/" + agentLogPrefix + ".Autoprobe.log";
			System.out.println ("AgentConfigLoc :" + agentConfigLoc);
			System.out.println ("AgentProfilePath :" + agentProfilePath);
			System.out.println ("AgentLogPrefix :" + agentLogPrefix);
			System.out.println ("AgentLogPath :" + agentLogPath);
			System.out.println ("AgentAutoprobeLogPath :" + agentAutoprobeLogPath);


			Properties properties = Util.loadPropertiesFile(agentProfilePath);
			properties.setProperty(AutomationConstants.AGENT_AUTONAMING_PROPERTY, autonaming);
			properties.setProperty(AutomationConstants.AGENT_NAME_PROPERTY, agentName);
			properties.setProperty(AutomationConstants.AGENT_CUSTOM_PROCESS_NAME_PROPERTY, agentProcess);
			properties.setProperty(AutomationConstants.AGENT_LOG_PATH_PROPERTY, agentLogPath);
			properties.setProperty(AutomationConstants.AGENT_AUTOPROBE_LOG_PATH_PROPERTY, agentAutoprobeLogPath);

			Util.writePropertiesToFile(agentProfilePath, properties);
		}
		catch (Exception e){
			e.printStackTrace();
			Assert.fail("Test failed because of the following reason: ",e);
		}
	}
	
	
	
	@Test
	public void startIISAgent() {
		try {
			
			long sleep = 2*60000;
			String startIISCommand = CMD_IISRESET +  " -start";
			System.out.println("EXECUTING IIS START COMMAND :"+startIISCommand);
			
			invokeProcess(startIISCommand);
			System.out.println ("Sleeping for " + sleep + " ms while agent is starting and connecting to em ...");
			Thread.sleep(sleep);
		} catch (Exception e) {
			 Assert.fail("Failed while starting IISAdmin Service.");
			e.printStackTrace();
		}
	}
	
	
	
	@Test
	public void restartIISAgent() {
		try {
			
			long sleep = 2*60000;
			System.out.println("EXECUTING IIS RESET COMMAND :"+CMD_IISRESET);
			
			invokeProcess(CMD_IISRESET);
			System.out.println ("Sleeping for " + sleep + " ms while agent is starting and connecting to em ...");
			Thread.sleep(sleep);
		} catch (Exception e) {
			 Assert.fail("Failed while Restarting IISAgent.");
			e.printStackTrace();
		}
	}

	@Test
	public void stopIISAgent() {

		try {
			long sleep = 2*60000;
			String stopIISCommand = CMD_IISRESET + " -stop";
			System.out.println("EXECUTING IIS STOP COMMAND :" + stopIISCommand);
			
			invokeProcess(stopIISCommand);
			System.out.println ("Sleeping for " + sleep + " ms while agent is stopping ...");
			Thread.sleep(sleep);
		} catch (Exception e) {
            Assert.fail("Failed while stopping IISAgent.");
			e.printStackTrace();
		}

	}
	
	public void invokeProcess(String command) throws IOException{
		Process process = null;
		try{
		 process = Runtime.getRuntime().exec(command);
		BufferedReader br = new BufferedReader(new InputStreamReader(
				process.getInputStream()));
		String line = br.readLine();
		while (line != null) {
			System.out.println(line);
			line = br.readLine();
		}
	
	}finally{
		if(process!=null){
		process.getInputStream().close();
		process.getOutputStream().close();
		process.getErrorStream().close();
	}
	}
}
}
