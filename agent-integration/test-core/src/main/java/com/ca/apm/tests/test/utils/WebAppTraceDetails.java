package com.ca.apm.tests.test.utils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author kurma05
 *
 */
public abstract class WebAppTraceDetails {
    
    public static TraceInfo[] TRACES_INFO = new TraceInfo[] {
        
        new TraceInfo("Apps|thieves|URLs|/thieves/purchase", "/thieves/purchase?event=stall&seconds=3", 20, "Standard", getThievesStandardTrace()),
        new TraceInfo("Apps|thieves|URLs|/thieves/escape", "/thieves/escape?event=stall&seconds=15", 15, "Stall", getThievesStallTrace()),
        new TraceInfo("Apps|thieves|URLs|/thieves/escape", "/thieves/escape?event=error", 20, "Error", getThievesErrorTrace()),
        new TraceInfo("Apps|thieves|URLs|/thieves/steal", "/thieves/steal?event=stall&seconds=3", 20, "Normal", getThievesNormalTrace()),
        new TraceInfo("Apps|NerdDinnerMVC5|URLs|/dinners", "/Dinners", 20, "Standard", getNerdDinnerMVC5StandardTrace()),
        new TraceInfo("Apps|NerdDinnerMVC5|URLs|/home/…", "/Home/ThrowAnExceptionNoGlobalCatch", 20, "Error", getNerdDinnerMVC5ErrorTrace()),
        new TraceInfo("Apps|NerdDinnerMVC5|URLs|/home/about", "/Home/About?sleepTime=15000", 15, "Stall", getNerdDinnerMVC5StallTrace()),
        new TraceInfo("Apps|NerdDinnerMVC5|URLs|/home/about", "/Home/About", "/Home/About?sleepTime=2000", 20, "Normal", getNerdDinnerMVC5NormalTrace()),
        new TraceInfo("Apps|server|URLs|/api/Items", "/api/Items?filter[where][productId]=8&filter[limit]=8", 120, "Normal", getTixchangeNodeJsNormalTrace()),
    };
    
    private static ArrayList<HashMap<String, String>> getThievesErrorTrace() {

        ArrayList<HashMap<String, String>> components = new ArrayList<HashMap<String, String>>();

        HashMap<String, String> frontends = new HashMap<String, String>();
        frontends.put("Resource Name", "Frontends|Apps|thieves|URLs|/thieves/escape");
        frontends.put("Context Path", "/thieves");
        frontends.put("URL Query", "event=error");
        frontends.put("Error Message", "thieves"); // partial string
        components.add(frontends);
    
        return components;
    }

    private static ArrayList<HashMap<String, String>> getTixchangeNodeJsNormalTrace() {
        
        ArrayList<HashMap<String, String>> components = new ArrayList<HashMap<String, String>>();

        HashMap<String, String> frontends = new HashMap<String, String>();
        frontends.put("Resource Name", "Frontends|Apps|server|URLs|/api/Items");
        frontends.put("URL", "/api/Items");
        frontends.put("Method", "GET");
        components.add(frontends);

        return components;
    }

    private static ArrayList<HashMap<String, String>> getThievesStandardTrace() {

        ArrayList<HashMap<String, String>> components = new ArrayList<HashMap<String, String>>();

        HashMap<String, String> frontends = new HashMap<String, String>();
        frontends.put("Resource Name", "Frontends|Apps|thieves|URLs|/thieves/purchase");
        frontends.put("Context Path", "/thieves");
        frontends.put("Scheme", "http");
        components.add(frontends);

        return components;
    }
    
    private static ArrayList<HashMap<String, String>> getThievesNormalTrace() {

        ArrayList<HashMap<String, String>> components = new ArrayList<HashMap<String, String>>();

        HashMap<String, String> frontends = new HashMap<String, String>();
        frontends.put("Resource Name", "Frontends|Apps|thieves|URLs|/thieves/steal");
        frontends.put("Context Path", "/thieves");
        frontends.put("Trace Type", "Normal");
        frontends.put("Scheme", "http");
        components.add(frontends);

        return components;
    }

    private static ArrayList<HashMap<String, String>> getThievesStallTrace() {

        ArrayList<HashMap<String, String>> components = new ArrayList<HashMap<String, String>>();

        HashMap<String, String> frontends = new HashMap<String, String>();
        frontends.put("Resource Name", "Frontends|Apps|thieves|URLs|/thieves/escape");
        frontends.put("Context Path", "/thieves");
        frontends.put("Trace Type", "ErrorSnapshot");
        components.add(frontends);

        return components;
    }
    
    private static ArrayList<HashMap<String, String>> getNerdDinnerMVC5StandardTrace() {
        
        ArrayList<HashMap<String, String>> components = new ArrayList<HashMap<String, String>>();

        HashMap<String, String> frontends = new HashMap<String, String>();
        frontends.put("Path", "Frontends|Apps|NerdDinnerMVC5|URLs|/dinners");        
        frontends.put("Resource Name", "MVC|Controllers|{controllername}|Actions|{actionname}");
        frontends.put("URL", "/Dinners");
        frontends.put("Scheme", "http");
        frontends.put("Trace Type", "Sampled");
        components.add(frontends);

        HashMap<String, String> mvc = new HashMap<String, String>();
        mvc.put("Path", "MVC|Controllers|Dinners");        
        mvc.put("Resource Name", "MVC|Controllers|{controllername}|Actions|{actionname}");
        mvc.put("URL", "/Dinners");
        mvc.put("Scheme", "http");
        components.add(mvc);
        
        return components;
    }
    
    private static ArrayList<HashMap<String, String>> getNerdDinnerMVC5NormalTrace() {
        
        ArrayList<HashMap<String, String>> components = new ArrayList<HashMap<String, String>>();

        HashMap<String, String> frontends = new HashMap<String, String>();
        frontends.put("Path", "Frontends|Apps|NerdDinnerMVC5|URLs|/home/about");        
        frontends.put("Resource Name", "MVC|Controllers|{controllername}|Actions|{actionname}");
        frontends.put("URL", "/Home/About");
        frontends.put("Scheme", "http");
        frontends.put("Trace Type", "Normal");
        components.add(frontends);

        HashMap<String, String> mvc = new HashMap<String, String>();
        mvc.put("Path", "MVC|Controllers|Home");        
        mvc.put("Resource Name", "MVC|Controllers|{controllername}|Actions|{actionname}");
        mvc.put("URL", "/Home/About");
        mvc.put("Scheme", "http");
        components.add(mvc);
        
        return components;
    }
    
    private static ArrayList<HashMap<String, String>> getNerdDinnerMVC5StallTrace() {
        
        ArrayList<HashMap<String, String>> components = new ArrayList<HashMap<String, String>>();

        HashMap<String, String> frontends = new HashMap<String, String>();
        frontends.put("Path", "Frontends|Apps|NerdDinnerMVC5|URLs|/home/about");        
        frontends.put("Resource Name", "MVC|Controllers|{controllername}|Actions|{actionname}");
        frontends.put("URL", "/Home/About");
        frontends.put("Scheme", "http");
        frontends.put("Trace Type", "ErrorSnapshot");
        components.add(frontends);

        HashMap<String, String> mvc = new HashMap<String, String>();
        mvc.put("Path", "MVC|Controllers|Home");        
        mvc.put("Resource Name", "MVC|Controllers|{controllername}|Actions|{actionname}");
        mvc.put("URL", "/Home/About");
        mvc.put("Scheme", "http");
        mvc.put("Trace Type", "ErrorSnapshot");
        components.add(mvc);
        
        return components;
    }
    
    private static ArrayList<HashMap<String, String>> getNerdDinnerMVC5ErrorTrace() {
        
        ArrayList<HashMap<String, String>> components = new ArrayList<HashMap<String, String>>();

        HashMap<String, String> frontends = new HashMap<String, String>();
        frontends.put("Path", "Frontends|Apps|NerdDinnerMVC5|URLs|/home/throwanexceptionnoglobalcatch");        
        frontends.put("Resource Name", "MVC|Controllers|{controllername}|Actions|{actionname}");
        frontends.put("URL", "/Home/ThrowAnExceptionNoGlobalCatch");
        frontends.put("Scheme", "http");
        frontends.put("Error Message", "InvalidOperationException"); // partial string
        components.add(frontends);

        HashMap<String, String> mvc = new HashMap<String, String>();
        mvc.put("Path", "MVC|Controllers|Home");        
        mvc.put("Resource Name", "MVC|Controllers|{controllername}|Actions|{actionname}");
        mvc.put("URL", "/Home/ThrowAnExceptionNoGlobalCatch");
        mvc.put("Scheme", "http");
        mvc.put("Error Message", "InvalidOperationException"); // partial string
        components.add(mvc);
        
        return components;
    }
}
