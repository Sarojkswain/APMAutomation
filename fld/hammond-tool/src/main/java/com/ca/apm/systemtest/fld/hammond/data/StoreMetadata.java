package com.ca.apm.systemtest.fld.hammond.data;

import java.io.Serializable;
import java.util.HashMap;

public class StoreMetadata implements Serializable {
    private static final long serialVersionUID = 6764867261200481598L;

    public static final String START_TIME = "start_time";
    public static final String END_TIME = "end_time";
    public static final String VERSION = "version";

    private HashMap<String, Object> properties = new HashMap<>();

    public void put(String key, Object value) {
        properties.put(key, value);
    }

    public Object get(String key) {
        return properties.get(key);
    }
}
