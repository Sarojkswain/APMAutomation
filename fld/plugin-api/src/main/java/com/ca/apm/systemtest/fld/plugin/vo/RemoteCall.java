/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author keyja01
 *
 */
@JsonTypeInfo(use=Id.CLASS, property="@type")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RemoteCall {
	private String callReferenceId;
	private String plugin;
	private String target;
	private String operation;
	private String processInstanceId;
	private Long   dashboardId;
	@JsonTypeInfo(use=Id.CLASS, property="@type")
	private Object[] parameters;

	/**
	 * Default constructor.
	 */
	public RemoteCall() {
	}

	public String getPlugin() {
		return plugin;
	}

	public void setPlugin(String plugin) {
		this.plugin = plugin;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	
	
	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public Object[] getParameters() {
		return parameters;
	}

	public void setParameters(Object[] parameters) {
		this.parameters = parameters;
	}

	public String getCallReferenceId() {
		return callReferenceId;
	}

	public void setCallReferenceId(String callReferenceId) {
		this.callReferenceId = callReferenceId;
	}

	public Long getDashboardId() {
		return dashboardId;
	}

	public void setDashboardId(Long dashboardId) {
		this.dashboardId = dashboardId;
	}

	
	/**
     * @return the processInstanceId
     */
    public String getProcessInstanceId() {
        return processInstanceId;
    }

    /**
     * @param processInstanceId the processInstanceId to set
     */
    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public static void main(String[] args) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		RemoteCall rc = new RemoteCall();
		rc.setPlugin("introscopePlugin");
		rc.setTarget("target");
		rc.setOperation("setAttribute");
		Object[] params = new Object[2];
		params[0] = "fooAttribute";
		params[1] = "Foo me, Amadeus";
		rc.setParameters(params);
		rc.setCallReferenceId("124938820-a1xx");
		String json = mapper.writeValueAsString(rc);
		System.out.println(json);
		
		rc = mapper.readValue(json, RemoteCall.class);
		System.out.println(rc);
		for (int i = 0; i < 4; i++) {
//			Attribute attr = gson.fromJson(json, classOfT)
		}
	}
}
