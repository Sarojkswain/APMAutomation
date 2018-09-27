package com.ca.apm.systemtest.fld.testbed.loads;

import java.util.Arrays;
import java.util.Collection;

import com.ca.apm.systemtest.fld.role.RecordingSessionRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;

/**
 * Agent recording sessions provider.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class FLDAgentRecordingSessionProvider implements FLDConstants, FLDLoadConstants, FldTestbedProvider {

	private String tessHost;
    private String tessUser = "cemadmin";
    private String tessPassword = "quality";
    //private String tessUser = "Admin6";
    //private String tessPassword = "Admin6";
    private String clientIP;
    private int tessPort = 8081;
    private boolean autostart = false;
    private int recordingDurationMillis = DEFAULT_AGENT_RECORDING_SESSION_DURATION_IN_MILLIS;
    private ITestbedMachine machine = null;


    public FLDAgentRecordingSessionProvider() {
    }
    
    public FLDAgentRecordingSessionProvider(String tessHost, int tessPort, String tessUser,
    		String tessPassword, String clientIP, boolean autostart, int recordingDurationMillis) {
		super();
		this.tessHost = tessHost;
		this.tessUser = tessUser;
		this.tessPassword = tessPassword;
		this.clientIP = clientIP;
		this.tessPort = tessPort;
		this.autostart = autostart;
		this.recordingDurationMillis = recordingDurationMillis;
	}

	public String getTessHost() {
		return tessHost;
	}

	public void setTessHost(String tessHost) {
		this.tessHost = tessHost;
	}

	public String getTessUser() {
		return tessUser;
	}

	public void setTessUser(String tessUser) {
		this.tessUser = tessUser;
	}

	public String getTessPassword() {
		return tessPassword;
	}

	public void setTessPassword(String tessPassword) {
		this.tessPassword = tessPassword;
	}

	public String getClientIP() {
		return clientIP;
	}

	public void setClientIP(String clientIP) {
		this.clientIP = clientIP;
	}

	public int getTessPort() {
		return tessPort;
	}

	public void setTessPort(int tessPort) {
		this.tessPort = tessPort;
	}

	public boolean isAutostart() {
		return autostart;
	}

	public void setAutostart(boolean autostart) {
		this.autostart = autostart;
	}

	public long getRecordingDurationMillis() {
		return recordingDurationMillis;
	}

	public void setRecordingDurationMillis(int recordingDurationMillis) {
		this.recordingDurationMillis = recordingDurationMillis;
	}
    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        machine = new TestbedMachine.Builder(AGENT_SESSION_RECORDING_MACHINE_ID)
            .templateId(ITestbedMachine.TEMPLATE_W64)
            .bitness(Bitness.b64)
            .build();
        return Arrays.asList(machine);
    }

	@Override
	public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
	    String tessHostName = tessHost;
	    if (tessHostName == null) {
	    	tessHostName = tasResolver.getHostnameById(EM_MOM_ROLE_ID);	
	    }
	    
	    RecordingSessionRole.Builder roleBuilder = new RecordingSessionRole.Builder(AGENT_SESSION_RECORDING_ROLE_ID, 
	    		tasResolver)
	    	.setTessHost(tessHostName)
	    	.setTessPort(tessPort)
	    	.setTessUser(tessUser)
	    	.setTessPassword(tessPassword)
	    	.setRecordingDurationMillis(recordingDurationMillis)
	    	.setClientIP(clientIP);
	     
	    if (autostart) {
	    	roleBuilder.setAutostart();
	    }
	    
	    RecordingSessionRole agentRecordingRole = roleBuilder.build();
		machine.addRole(agentRecordingRole);
	}

}
