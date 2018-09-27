/**
 * 
 */
package com.ca.apm.testing.metricsynth;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.wily.introscope.spec.server.transactiontrace.TransactionComponentData;

/**
 * @author keyja01
 *
 */
public class TTUtil {
    private static final Random rand = new SecureRandom();
    
    public static TransactionComponentData frontEndTT(String appName, long startTime, long duration, 
              String corrId, String txnTraceId, String callerTxnTraceId, String serverName, 
              String backendHost, Integer backendPort, String backendAppName) {
        TransactionComponentData[] calledComponents = null;
        String resource = null;
        String tz = null;
        
        if (backendHost != null) {
            calledComponents = new TransactionComponentData[1];
            if (backendPort == null) {
                backendPort = 8080;
            }
            resource = "Backends|WebService at http_//" + backendHost + "_" + backendPort + "|Paths|Default";
            calledComponents[0] = new TransactionComponentData(resource, startTime + 1, tz, duration - 2, backEndParams(backendHost, backendPort, backendAppName), new TransactionComponentData[0]);
        }
        resource = "Frontends|Apps|" + appName + "|URLs|Default";
        
        System.out.println("TT: " + resource + ", corrId = " + corrId + ", txnId = " + txnTraceId + ", callerTxnId = " + callerTxnTraceId);
        return new TransactionComponentData(resource, startTime, tz, duration, frontEndParams(resource, corrId, appName, serverName, txnTraceId, callerTxnTraceId), calledComponents);
    }
    
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Map frontEndParams(String resource, String corrId, String appName, String serverName, String txnTraceId, String callerTxnTraceId) {
        Map map = new HashMap();
        map.put("TxnTraceId", txnTraceId);
        if (callerTxnTraceId != null) {
            map.put("CallerTxnTraceId", callerTxnTraceId);
        }
        map.put("CorCrossProcessData", corrId);
        map.put("Scheme", "http");
        map.put("Method", "service");
        map.put("Thread Name", "http--127.0.0.1-8080-2");
        map.put("Application Name", appName);
        map.put("Server Port", "8080");
        map.put("Instrumentation Level", "low");
        map.put("Context Path", "/" + appName);
        map.put("Thread Group name", "main");
        map.put("Resource Name", resource);
        map.put("Method Descriptor", "(Ljavax/servlet/ServletRequest;Ljavax/servlet/ServletResponse;)V");
        map.put("Trace Type", "Normal");
        map.put("HTTP Method", "GET");
        map.put("Is dynamic", "false");
        map.put("Language", "Java");
        map.put("Server Name", serverName);
        map.put("Session ID", sessionId());
        map.put("Class", "javax.faces.webapp.FacesServlet");
        map.put("Is temporary", "false");
        map.put("DataCreationType", "0");
        map.put("URL", "/" + appName + "/busTx.jsf");
        
        return map;
    }
    
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Map backEndParams(String backendHost, int backendPort, String appName) {
        Map map = new HashMap();
        map.put("Method Descriptor",  "()Ljava/io/InputStream;");
        map.put("Component ID",  "1");
        map.put("HTTP Method",  "GET");
        map.put("Is dynamic",  "false");
      //params: {,DataCreationType="0",="",}
        map.put("Called URL",  "http://" + backendHost + ":" + backendPort + "/" + appName + "/busTx.jsf");
        map.put("Method",  "getInputStream");
        map.put("Class",  "sun.net.www.protocol.http.HttpURLConnection");
        map.put("HTTP Status Code",  "200 - OK");
        map.put("DataCreationType",  "0");
        map.put("Resource Name",  "Backends|WebService at http_//" + backendHost + "_" + backendPort + "|Paths|Default");
        
        return map;
    }
    
    
    public static String correlationId() {
        StringBuffer corrId = new StringBuffer();
        for (int i = 0; i < 16; i++) {
            corrId.append(String.format("%02X", rand.nextInt(256)));
        }
        return corrId.toString();
    }
    
    public static String sessionId() {
        StringBuffer corrId = new StringBuffer();
        for (int i = 0; i < 32; i++) {
            corrId.append(String.format("%02x", rand.nextInt(256)));
        }
        return corrId.toString();
    }
}
