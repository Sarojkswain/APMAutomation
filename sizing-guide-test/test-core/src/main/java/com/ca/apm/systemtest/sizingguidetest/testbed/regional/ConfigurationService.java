package com.ca.apm.systemtest.sizingguidetest.testbed.regional;


public class ConfigurationService {

    private static Configuration config;

    public static Configuration getConfig() {
        try {
            if (config == null) {
                config =
                    new ConfigurationImpl(
                        ConfigurationService.class
                            .getResourceAsStream("/sizing-guide-test.properties"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize configuration", e);
        }
        return config;
    }

}
