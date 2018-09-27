/**
 * 
 */
package com.ca.apm.tests.flow;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.property.EnvPropSerializable;

import java.util.Map;

/**
 * @author keyja01
 *
 */
public class RunMemoryMonitorFlowContext implements IFlowContext, EnvPropSerializable<RunMemoryMonitorFlowContext> {
    public static final String DEFAULT_SHUTDOWN_FILE = "mmShutdown.txt";
    protected String gcLogFile;
    protected String group;
    protected String roleName;
    protected String memoryMonitorWebappHost;
    protected Integer memoryMonitorWebappPort;
    protected String memoryMonitorWebappContextRoot;
    protected Integer chartWidth;
    protected Integer chartHeight;
    protected Long waitInterval;
    protected String shutdownFile = DEFAULT_SHUTDOWN_FILE;
    protected boolean shutdown = false;
    protected String deployDir;

    public static Builder getBuilder() {
        BuilderFactory<RunMemoryMonitorFlowContext, Builder> fact = new BuilderFactory<>();
        return fact.newBuilder(RunMemoryMonitorFlowContext.class, Builder.class);
    }

    public interface Builder extends IGenericBuilder<RunMemoryMonitorFlowContext> {
        public Builder gcLogFile(String gcLogFile);
        public Builder group(String group);
        public Builder roleName(String roleName);
        public Builder memoryMonitorWebappHost(String memoryMonitorWebappHost);
        public Builder memoryMonitorWebappPort(Integer memoryMonitorWebappPort);
        public Builder memoryMonitorWebappContextRoot(String memoryMonitorWebappContextRoot);
        public Builder chartWidth(Integer chartWidth);
        public Builder chartHeight(Integer chartHeight);
        public Builder waitInterval(Long waitInterval);
        public Builder shutdownFile(String shutdownFile);
        public Builder shutdown(boolean shutdown);
        public Builder deployDir(String deployDir);
    }

    @Override
    public RunMemoryMonitorFlowContext deserialize(String key, Map<String, String> serializedData) {
        RunMemoryMonitorFlowContextSerializer serializer = new RunMemoryMonitorFlowContextSerializer();
        
        return serializer.deserialize(key, serializedData);
    }

    @Override
    public Map<String, String> serialize(String key) {
        RunMemoryMonitorFlowContextSerializer serializer = new RunMemoryMonitorFlowContextSerializer(this);
        
        return serializer.serialize(key);
    }
}
