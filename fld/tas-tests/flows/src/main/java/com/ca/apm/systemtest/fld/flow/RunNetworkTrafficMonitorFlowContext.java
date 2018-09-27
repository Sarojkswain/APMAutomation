package com.ca.apm.systemtest.fld.flow;

import java.util.Map;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.property.EnvPropSerializable;

/**
 * @author bocto01
 *
 */
public class RunNetworkTrafficMonitorFlowContext
    implements
        IFlowContext,
        EnvPropSerializable<RunNetworkTrafficMonitorFlowContext> {

    public static final String DEFAULT_SHUTDOWN_FILE = "ntmShutdown.txt";

    protected String networkTrafficMonitorWebappHost;
    protected Integer networkTrafficMonitorWebappPort;
    protected String networkTrafficMonitorWebappContextRoot;
    protected Integer chartWidth;
    protected Integer chartHeight;
    protected Long waitInterval;
    protected String shutdownFile = DEFAULT_SHUTDOWN_FILE;
    protected boolean shutdown = false;
    protected String workDir;

    public static Builder getBuilder() {
        BuilderFactory<RunNetworkTrafficMonitorFlowContext, Builder> fact = new BuilderFactory<>();
        return fact.newBuilder(RunNetworkTrafficMonitorFlowContext.class, Builder.class);
    }

    public interface Builder extends IGenericBuilder<RunNetworkTrafficMonitorFlowContext> {
        public Builder networkTrafficMonitorWebappHost(String networkTrafficMonitorWebappHost);

        public Builder networkTrafficMonitorWebappPort(Integer networkTrafficMonitorWebappPort);

        public Builder networkTrafficMonitorWebappContextRoot(
            String networkTrafficMonitorWebappContextRoot);

        public Builder chartWidth(Integer chartWidth);

        public Builder chartHeight(Integer chartHeight);

        public Builder waitInterval(Long waitInterval);

        public Builder shutdownFile(String shutdownFile);

        public Builder shutdown(boolean shutdown);

        public Builder workDir(String workDir);
    }

    @Override
    public RunNetworkTrafficMonitorFlowContext deserialize(String key,
        Map<String, String> serializedData) {
        RunNetworkTrafficMonitorFlowContextSerializer serializer =
            new RunNetworkTrafficMonitorFlowContextSerializer();
        return serializer.deserialize(key, serializedData);
    }

    @Override
    public Map<String, String> serialize(String key) {
        RunNetworkTrafficMonitorFlowContextSerializer serializer =
            new RunNetworkTrafficMonitorFlowContextSerializer(this);
        return serializer.serialize(key);
    }

}
