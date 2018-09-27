package com.ca.apm.systemtest.alertstateload.testbed.regional;


public class ConfigurationService {

    private static Configuration config;

    public static Configuration getConfig() {
        try {
            if (config == null) {
                config =
                    new ConfigurationImpl(
                        ConfigurationService.class
                            .getResourceAsStream("/alert-state-load.properties"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize configuration", e);
        }
        return config;
    }

}
