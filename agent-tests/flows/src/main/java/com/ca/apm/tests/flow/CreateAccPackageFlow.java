package com.ca.apm.tests.flow;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.google.gson.Gson;

/**
 * @author sinka08
 */
@Flow
public class CreateAccPackageFlow extends FlowBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(CreateAccPackageFlow.class);
	private static final String PACKAGE_URL_PATH = "/apm/acc/package";
	private static final String SECURITY_TOKEN = "Bearer 5d39df5-d624-4b4c-9304-c7861a992675";

	@FlowContext
	private CreateAccPackageFlowContext context;

	@Override
	public void run() throws Exception {
	 
	    LOGGER.info("Sleeping for {} ms ...", context.getSleep());
	    Thread.sleep(context.getSleep());
	    
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost packageRequest = new HttpPost(context.getAccServerUrl() + PACKAGE_URL_PATH);
		packageRequest.addHeader("Authorization", SECURITY_TOKEN);
		packageRequest.setHeader("Content-type", "application/json");

		Package p = new Package(context.getPackageName(), context.getOsName(),
            context.getProcess(), context.getAgentVersion());
        String jsonInput = new Gson().toJson(p);
        StringEntity input = new StringEntity(jsonInput, ContentType.APPLICATION_FORM_URLENCODED);
        packageRequest.setEntity(input);
        
		LOGGER.info("Executing package creation request : {}", packageRequest);
		LOGGER.info("Json input: \n" + jsonInput);

		HttpResponse jsonResponse = client.execute(packageRequest);
		String jsonData = EntityUtils.toString(jsonResponse.getEntity());
		LOGGER.info("Json output: \n" + jsonData);
	}

	private static class Package {

		private String packageName;
		private String description;
		private Environment environment;

		Package(String packageName, String osName, String process, String agentVersion) {
			this.packageName = packageName;
			this.setEnvironment(new Environment(osName, process, agentVersion));
		}

		public String getPackageName() {
			return packageName;
		}

		public void setPackageName(String packageName) {
			this.packageName = packageName;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public Environment getEnvironment() {
			return environment;
		}

		public void setEnvironment(Environment environment) {
			this.environment = environment;
		}

	}

	private static class Environment {
		private String osName;
		private String process;
		private String agentVersion;

		Environment(String osName, String process, String agentVersion) {
			this.osName = osName;
			this.process = process;
			this.agentVersion = agentVersion;
		}

		public String getOsName() {
			return osName;
		}

		public void setOsName(String osName) {
			this.osName = osName;
		}

		public String getProcess() {
			return process;
		}

		public void setProcess(String process) {
			this.process = process;
		}

		public String getAgentVersion() {
			return agentVersion;
		}

		public void setAgentVersion(String agentVersion) {
			this.agentVersion = agentVersion;
		}
	}
}
