package com.ca.apm.systemtest.fld.flow;

import java.util.HashMap;
import java.util.Map;

import com.ca.tas.property.AbstractEnvPropertySerializer;
import com.google.gson.Gson;

public class RunMemoryMonitorFlowContextSerializer extends AbstractEnvPropertySerializer<RunMemoryMonitorFlowContext> {
    private RunMemoryMonitorFlowContext ctx;

    public RunMemoryMonitorFlowContextSerializer(RunMemoryMonitorFlowContext ctx) {
        super(RunMemoryMonitorFlowContextSerializer.class);
        this.ctx = ctx;
    }
    
    public RunMemoryMonitorFlowContextSerializer() {
        super(RunMemoryMonitorFlowContextSerializer.class);
    }
    
    
    @Override
    public Map<String, String> serialize(String key) {
        Map<String, String> data = super.serialize(key);
        
        Map<String, String> customData = new HashMap<>(1);
        if (ctx != null) {
            Gson gson = new Gson();
            String json = gson.toJson(ctx);
            customData.put("DATA", json);
        }
        
        data.putAll(serializeMapWithKey(key, customData));
        
        return data;
    }

    @Override
    public RunMemoryMonitorFlowContext deserialize(String key, Map<String, String> serializedData) {
        Map<String, String> map = deserializeMapWithKey(key, serializedData);
        String json = map.get("DATA");
        Gson gson = new Gson();
        if (json != null) {
            RunMemoryMonitorFlowContext ctx = gson.fromJson(json, RunMemoryMonitorFlowContext.class);
            return ctx;
        }
        
        return new RunMemoryMonitorFlowContext();
    }
    
}