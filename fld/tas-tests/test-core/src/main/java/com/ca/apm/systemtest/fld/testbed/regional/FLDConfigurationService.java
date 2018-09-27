/**
 * 
 */
package com.ca.apm.systemtest.fld.testbed.regional;

/**
 * @author keyja01
 *
 */
public class FLDConfigurationService {
    private static FLDConfiguration config;
    
    public static FLDConfiguration getConfig() {
        try {
            if (config == null) {
                config = new FLDConfigurationImpl(FLDConfigurationService.class.getResourceAsStream("/fld.properties"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize FLD Configuration", e);
        }
        return config;
    }
    
    /**
     * Intended for use in testing when we just want to override the default configuration
     * @param config
     */
    public static void setConfig(FLDConfiguration config) {
        FLDConfigurationService.config = config;
    }
}
