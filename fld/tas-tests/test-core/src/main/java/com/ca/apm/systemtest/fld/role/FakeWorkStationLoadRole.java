package com.ca.apm.systemtest.fld.role;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import org.apache.http.util.Args;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * Fake Workstation Load Scripts for EM FLD. Wurlitzer fake agents load should
 * be up and running before running these live queries
 *
 * @author banra06@ca.com
 */

public class FakeWorkStationLoadRole extends AbstractRole {

	private String installDir;
	private ITasResolver tasResolver;
	private static final String EM_USER = "cemadmin";
	private static final String EM_PASSWORD = "quality";
	private int emPort;
	private String emHost;
	private String version = "10.7.0-SNAPSHOT";
	private static final String METRIC = "\"Servlets\\|Servlet_(.*):Average Response Time \\(ms\\)\"";
	public static final String FW_HISTORICALQUERY_START_LOAD = "fwhistoricalloadStart";
	public static final String FW_LIVEQUERIES_START_LOAD = "fwlivequeriesloadStart";
	public static final String FW_HISTORICALQUERY_STOP_LOAD = "fwhistoricalloadStop";
	public static final String FW_LIVEQUERIES_STOP_LOAD = "fwlivequeriesloadStop";
	private boolean performHistoricalQueries;
	private boolean performLiveQueries;

	protected FakeWorkStationLoadRole(Builder builder) {
		super(builder.roleId, builder.getEnvProperties());
		this.tasResolver = builder.tasResolver;
		this.installDir = builder.installDir;
		this.emHost = builder.emHost;
		this.emPort = builder.emPort;
		this.version = builder.version;
		this.performHistoricalQueries = builder.performHistoricalQueries;
		this.performLiveQueries = builder.performLiveQueries;
	}

	@Override
	public void deploy(IAutomationAgentClient aaClient) {
	    getArtifacts(aaClient);
		createBatchFiles(aaClient);
	}

	private void createBatchFiles(IAutomationAgentClient aaClient) {
	    FileModifierFlowContext.Builder builder = new FileModifierFlowContext.Builder();

	    if (performHistoricalQueries) {
	        Collection<String> historicalQuery = Arrays.asList("echo Started",
                "title Historical-FLD sleep",
                "java -Xmx512m -XX:+HeapDumpOnOutOfMemoryError -jar "
                        + "%cd%/FakeWorkstation.jar -host " + emHost
                        + " -port " + emPort + " -user " + EM_USER
                        + " -password " + EM_PASSWORD
                        + " -historical -resolution 15 -sleepBetween 15000"
                        + " -agent \"(.*)Agent_1\"" + " -metric " + METRIC
                        + " >> " + "FLD.historical_query.log 2>&1", "pause");
	        builder = builder.create(installDir + "/run.historical.query.mom.bat", historicalQuery);
	    }

	    if (performLiveQueries) {
	        Collection<String> empty = Arrays.asList("echo Started");
	        builder = builder.create(installDir + "/startAllLiveQueries.bat", empty);
	    }

	    FileModifierFlowContext emptyFile = builder.build();
		runFlow(aaClient, FileModifierFlow.class, emptyFile);

		Collection<String> kickoffhistory = Arrays.asList("start run.historical.query.mom.bat");
        Collection<String> kickofflauncher = Arrays.asList("start startAllLiveQueries.bat");

        for (int i = 1; i <= 10; i++) {
			Collection<String> command1 = Arrays
					.asList("java -Xmx512m -XX:+HeapDumpOnOutOfMemoryError -jar "
							+ "%cd%/FakeWorkstation.jar -host "
							+ emHost
							+ " -port "
							+ emPort
							+ " -user "
							+ EM_USER
							+ " -password "
							+ EM_PASSWORD
							+ " -resolution 15 -sleepBetween 15000"
							+ " -agent \"(.*)Agent_"
							+ i
							+ "\""
							+ " -metric "
							+ METRIC
							+ " >> "
							+ "FLD.live_query_"
							+ i
							+ ".log 2>&1");

			Collection<String> command2 = Arrays.asList(
					"start run.live.query.mom_" + i + ".bat",
					"ping -n 30 127.0.0.1", "");


			FileModifierFlowContext.Builder createBatchFlowBuilder = new FileModifierFlowContext.Builder();
			if (performLiveQueries) {
			    createBatchFlowBuilder = createBatchFlowBuilder.create(installDir + "/run.live.query.mom_" + i + ".bat", command1);
			    createBatchFlowBuilder = createBatchFlowBuilder.append(installDir + "/startAllLiveQueries.bat", command2);
			}
			FileModifierFlowContext createBatchFlow = createBatchFlowBuilder.build();
			runFlow(aaClient, FileModifierFlow.class, createBatchFlow);


			FileModifierFlowContext.Builder kickoffBatchBuilder = new FileModifierFlowContext.Builder();
			if (performHistoricalQueries) {
                kickoffBatchBuilder = kickoffBatchBuilder.create(installDir + "/kickoffhistory.bat", kickoffhistory);
            }
			if (performLiveQueries) {
			    kickoffBatchBuilder = kickoffBatchBuilder.create(installDir + "/kickofflauncher.bat", kickofflauncher);
			}
			FileModifierFlowContext kickoffBatch = kickoffBatchBuilder.build();
			runFlow(aaClient, FileModifierFlow.class, kickoffBatch);
		}
	}

	private void getArtifacts(IAutomationAgentClient aaClient) {
		URL url = tasResolver.getArtifactUrl(new DefaultArtifact(
				"com.ca.apm.coda-projects.test-tools", "fakeworkstation", "",
				"jar", version));

		GenericFlowContext getAgentContext = new GenericFlowContext.Builder()
				.artifactUrl(url)
				.destination(installDir + "/FakeWorkstation.jar").notArchive()
				.build();
		runFlow(aaClient, GenericFlow.class, getAgentContext);
	}

	public static class Builder extends
			BuilderBase<Builder, FakeWorkStationLoadRole> {
		private final String roleId;
		private final ITasResolver tasResolver;
		protected String installDir = "C:\\FakeWS";
		protected String emHost = "localhost";
		protected int emPort = 5001;
		protected String version;
		protected boolean performHistoricalQueries;
		protected boolean performLiveQueries;

		public Builder(String roleId, ITasResolver tasResolver) {
			this.roleId = roleId;
			this.tasResolver = tasResolver;
		}

		@Override
		public FakeWorkStationLoadRole build() {
		    Args.check(performHistoricalQueries || performLiveQueries, "At least one of historic or live queries must be configured to be run");
		    if (performHistoricalQueries) {
		        startHistoricalQueries();
		        stopHistoricalQueries();
		    }
		    if (performLiveQueries) {
		        startAllLiveQueries();
	            stopAllLiveQueries();
		    }
			return getInstance();
		}

		@Override
		protected FakeWorkStationLoadRole getInstance() {
			return new FakeWorkStationLoadRole(this);
		}

		@Override
		protected Builder builder() {
			return this;
		}

		public Builder installDir(String installDir) {
			this.installDir = installDir;
			return builder();
		}

		public Builder emHost(String emHost) {
			this.emHost = emHost;
			return builder();
		}

		public Builder emPort(int emPort) {
			this.emPort = emPort;
			return builder();
		}
		
        public Builder version(String version) {
            this.version = version;
            return builder();
        }

		private void stopAllLiveQueries() {
			String stopCommand = "wmic process where \"CommandLine like '%FakeWorkstation%' and not (CommandLine like '%wmic%')\" Call Terminate";
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					stopCommand).build();
			getEnvProperties().add(FW_LIVEQUERIES_STOP_LOAD, runCmdFlowContext);

		}

		private void stopHistoricalQueries() {
			String stopCommand = "wmic process where \"CommandLine like '%FakeWorkstation%' and not (CommandLine like '%wmic%')\" Call Terminate";
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					stopCommand).build();
			getEnvProperties().add(FW_HISTORICALQUERY_STOP_LOAD,
					runCmdFlowContext);

		}

		private void startHistoricalQueries() {
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					"kickoffhistory.bat").workDir(installDir).build();
			getEnvProperties().add(FW_HISTORICALQUERY_START_LOAD,
					runCmdFlowContext);

		}

		private void startAllLiveQueries() {
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					"kickofflauncher.bat").workDir(installDir).build();
			getEnvProperties()
					.add(FW_LIVEQUERIES_START_LOAD, runCmdFlowContext);

		}

		public Builder performHistoricalQueries() {
		    performHistoricalQueries = true;
		    return builder();
		}

		public Builder performLiveQueries() {
		    performLiveQueries = true;
		    return builder();
		}
	}

}
