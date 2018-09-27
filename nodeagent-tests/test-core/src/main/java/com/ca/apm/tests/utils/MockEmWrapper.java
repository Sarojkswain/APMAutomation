package com.ca.apm.tests.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.common.mockem.MockEm;
import com.ca.apm.automation.common.mockem.RequestProcessor;

public class MockEmWrapper {
	private static final Logger LOGGER = LoggerFactory.getLogger(MockEmWrapper.class);
	private static final Map<Integer, MockEmWrapper> CONNECTION_MAP = new ConcurrentHashMap<>();
	private MockEm mockEm;
	private int port;

	private MockEmWrapper(Builder b) {
		mockEm = new MockEm();
		port = b.getPort();

		if (CONNECTION_MAP.containsKey(port)) {
			throw new IllegalArgumentException(
			        String.format(
			                "Could not create new instance of mockEM. An instance of mockEM is already listening on port %s",
			                port));
		}
	}

	public boolean start() {
		LOGGER.info("starting mockEM");

		HashMap<String, String> params = new HashMap<>();
		params.put("isengard.server.port.default", String.valueOf(port));
		try {
			mockEm.setupSimpleServer(params);
		} catch (Exception e) {
			LOGGER.error("Exception while starting mockEM", e);
			return false;
		}

		LOGGER.info("mockEM listening on port: {}", port);
		CONNECTION_MAP.put(port, this);
		return true;
	}

	public boolean stop() {
		LOGGER.info("stopping mockEM");

		try {
			mockEm.shutdownSimpleServer();
		} catch (Exception e) {
			LOGGER.error("Exception while shutting down mockEM", e);
			return false;
		} finally {
			CONNECTION_MAP.remove(port);
		}

		return true;
	}

	public boolean reset() {
		LOGGER.info("reseting mockEM");
		try {
			mockEm.resetSimpleServer();
		} catch (Exception e) {
			LOGGER.error("Exception while reseting mockEM", e);
			return false;
		}
		return true;
	}

	public void processMetrics(IMetricAssertionData metricData) {
		RequestProcessor reqProcessor = getReqProcessor(metricData);

		reqProcessor.processMetrics(metricData.getDuration(), metricData.getValidator(),
				metricData.isFailOnTimeOut());
	}
	
	public void processMetricNames(IMetricAssertionData metricData, boolean isExpected) {
        RequestProcessor reqProcessor = getReqProcessor(metricData);

        reqProcessor.processMetricNames(metricData.getDuration(), metricData.getValidator(), isExpected);
    }

	public boolean processTraces(ITraceValidationData traceData) {
		RequestProcessor reqProcessor = getReqProcessor(traceData);

		return reqProcessor.processTraces(traceData.getNumExpected(), traceData.getDuration(),
				traceData.getValidator(), traceData.getShouldCheckTimeStamp());
	}
	
	public RequestProcessor getReqProcessor(IAgentNameExpr agentNameExpr){
		return new RequestProcessor.Builder(mockEm)
        .setAgentName(agentNameExpr.getAgentName()).setHostName(agentNameExpr.getHostName())
        .setProcessName(agentNameExpr.getProcessName()).build();
	}

	public static class Builder {
		private int port = DeployEMFlowContext.EM_PORT;

		public int getPort() {
			return port;
		}

		public Builder setPort(int port) {
			this.port = port;
			return this;
		}

		public MockEmWrapper build() {
			return new MockEmWrapper(Builder.this);
		}
	}

}
