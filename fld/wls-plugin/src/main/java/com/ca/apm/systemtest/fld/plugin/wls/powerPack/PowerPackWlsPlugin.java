package com.ca.apm.systemtest.fld.plugin.wls.powerPack;

import com.ca.apm.systemtest.fld.plugin.wls.WlsPlugin;

public interface PowerPackWlsPlugin extends WlsPlugin {

    String POWERPACK_WLS_PLUGIN = "powerPackWlsPlugin";


    String INSERTPOINTKEY_JAVAOPTS = "javaopts";
    String INSERTPOINTKEY_MEMOPTS = "memopts";
    String INSERTPOINTKEY_JMX = "jmx";
    String INSERTPOINTKEY_GC = "gc";
    String INSERTPOINTKEY_AGENT = "agent";

    /**
     * Sets custom insert points to configure Weblogic startup scripts.
     * 
     * @param  wlsVersion         wls version taken from WLS plugin configuration 
     * @param  osVersion          OS version
     * @param  key                specific key
     * @param  searchAfterTexts   search text points
     * @param  replace            whether new insert points should be appended 
     *                            to existing ones or should replace the existing ones
     */
    public void setInsertAfterPoints(String wlsVersion, String osVersion, String key,
        String[] searchAfterTexts, boolean replace);


}
