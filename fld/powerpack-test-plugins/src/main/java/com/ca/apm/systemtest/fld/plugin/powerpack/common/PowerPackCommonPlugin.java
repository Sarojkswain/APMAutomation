package com.ca.apm.systemtest.fld.plugin.powerpack.common;

import com.ca.apm.systemtest.fld.plugin.Plugin;

/**
 * 
 * Plugin interface which is intended to provide common API for PowerPack performance testing workflows.     
 *      
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public interface PowerPackCommonPlugin extends Plugin {

    public static final String POWER_PACK_COMMON_PLUGIN_ID = "powerPackCommonPlugin";
    
    /**
     * Re-creates a Trade database for a test application (like StockTrader, Trade6, etc.).  
     * 
     * @param   sqlScriptUrl   URL of the script archive artifact with a Trade db dump
     * @return                 script execution exit code (sqlplus)
     */
    int reCreateTradeDb(String sqlScriptUrl);
    
    
}
