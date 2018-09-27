package com.ca.apm.systemtest.fld.plugin.cm;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.plugin.PluginConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


@Component
public class ConfigurationManagerImpl implements ConfigurationManager {
    private static class ConfigurationCacheEntry {
        private long timestamp;
        private Class<? extends PluginConfiguration> klass;
        private List<ConfigurationChangeListener<? extends PluginConfiguration>> listeners;
        
        private ConfigurationCacheEntry(Class<? extends PluginConfiguration> klass) {
            super();
            listeners = new ArrayList<>();
            this.timestamp = System.currentTimeMillis();
            this.klass = klass;
        }
    }
    
    private Logger log = LoggerFactory.getLogger(ConfigurationManagerImpl.class);
    private static final ObjectMapper mapper;
    private String configFilePath;
    private HashMap<String, PluginConfiguration> map = new HashMap<>();
    private HashMap<String, ConfigurationCacheEntry> listenerMap = new HashMap<>();
    private Timer timer;
    
    
    public ConfigurationManagerImpl() {
    }
    
    
    @PostConstruct
    private void afterBeanInit() {
        // check that the configuration directory has been created
        File configPath = new File(configFilePath);
        if (!configPath.exists()) {
            configPath.mkdirs();
        }
        
        timer = new Timer(true);
        TimerTask checkConfigsTask = new TimerTask() {
            @Override
            public void run() {
                synchronized (listenerMap) {
                    for (Entry<String, ConfigurationCacheEntry> entry: listenerMap.entrySet()) {
                        String pluginId = entry.getKey();
                        ConfigurationCacheEntry cacheEntry = entry.getValue();
                        File dir = new File(configFilePath);
                        File f = new File(dir, pluginId + ".conf.json");
                        
                        if (f.exists() && f.canRead() && f.lastModified() > cacheEntry.timestamp) {
                            PluginConfiguration config = null;
                            try {
                                config = loadPluginConfiguration(pluginId, cacheEntry.klass);
                            } catch (Exception e) {
                                ErrorUtils.logExceptionFmt(log, e, "Exception: {0}");
                            }
                            if (config != null) {
                                notifyListeners(pluginId, config);
                            }
                        }
                    }
                }
            }
        };
        timer.scheduleAtFixedRate(checkConfigsTask, 60000L, 60000L);
        
    }
    
    
    private void notifyListeners(String pluginId, PluginConfiguration config) {
        ConfigurationCacheEntry cacheEntry = listenerMap.get(pluginId);
        // need to reread the file and notify listeners
        if (cacheEntry != null) {
            for (ConfigurationChangeListener<? extends PluginConfiguration> listener: cacheEntry.listeners) {
                try {
                    listener.onChange(config);
                } catch (Exception e) {
                    ErrorUtils.logExceptionFmt(log, e, "Exception: {0}");
                }
            }
        }
    }
    

    public String getConfigFilePath() {
        return configFilePath;
    }


    @Autowired
    @Value("${agent.config.dir:conf}")
    public void setConfigFilePath(String configFilePath) {
        this.configFilePath = configFilePath;
    }


    @Override
    public synchronized <T extends PluginConfiguration> void savePluginConfiguration(String pluginId, T cfg) {
        File dir = new File(configFilePath);
        File f = new File(dir, pluginId + ".conf.json").getAbsoluteFile();
        
        try {
            String json = mapper.writeValueAsString(cfg);
            try (FileWriter writer = new FileWriter(f)) {
                writer.append(json);
                writer.flush();
            }
        } catch (Exception e) {
            throw new ConfigurationException(
                ErrorUtils.logExceptionFmt(log, e, "Unable to save configuration to file {1}", f),
                e);
        }
        
        
        notifyListeners(pluginId, cfg);
        
        map.put(pluginId, cfg);
    }


    @Override
    public synchronized <T extends PluginConfiguration> T loadPluginConfiguration(String pluginId, Class<T> klass) {
        @SuppressWarnings("unchecked")
        T t = (T) map.get(pluginId);
        if (t != null) {
            return wrap(t, klass);
        }
        
        File dir = new File(configFilePath);
        File f = new File(dir, pluginId + ".conf.json").getAbsoluteFile();
        if (!f.exists()) {
            try {
                t = klass.newInstance();
                savePluginConfiguration(pluginId, t);
            } catch (ConfigurationException ex) {
                ErrorUtils.logExceptionFmt(log, ex, "Exception: {0}");
                throw ex;
            } catch (Exception ex) {
                throw new ConfigurationException(
                    ErrorUtils.logExceptionFmt(log, ex,
                        "Unable to create new configuration for class {1}"),
                    ex);
            }
        }
        
        try {
            t = mapper.readValue(f, klass);
        } catch (IOException ex) {
            throw new ConfigurationException(
                ErrorUtils.logExceptionFmt(log, ex,
                    "Unable to read configuration for class {1} from {2}. Exception: {0}",
                    klass.getName(), f.getAbsolutePath()),
                ex);
        }
        map.put(pluginId, t);
        
        
        return wrap(t, klass);
    }
    
    
    
    private <T extends PluginConfiguration> T wrap(T t, Class<T> klass) {
        T tgt = BeanUtils.instantiate(klass);
        BeanUtils.copyProperties(t, tgt, klass);
        return tgt;
    }
    
    
    
    @Override
    public <T extends PluginConfiguration> void registerConfigurationChangeListener(
            String pluginId, ConfigurationChangeListener<T> listener, Class<T> klass) {
        synchronized (listenerMap) {
            ConfigurationCacheEntry entry = listenerMap.get(pluginId);
            if (entry == null) {
                entry = new ConfigurationCacheEntry(klass);
                listenerMap.put(pluginId, entry);
            }
            if (!entry.listeners.contains(listener)) {
                entry.listeners.add(listener);
            }
        }
    }
    
    
    static {
        mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }
}
