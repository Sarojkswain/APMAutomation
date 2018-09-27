package com.ca.apm.systemtest.fld.plugin.vo;

/**
 * Static per-thread storage of Activiti process instance id.  
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class ProcessInstanceIdStore {
    
    private static ThreadLocal<String> processInstanceIdThreadLocal = new ThreadLocal<String>();

    public static String getProcessInstanceId() {
        return processInstanceIdThreadLocal.get();
    }

    public static void setProcessInstanceId(String processInstanceId) {
        if (processInstanceId != null) {
            processInstanceIdThreadLocal.set(processInstanceId);    
        } else {
            clearProcessInstanceId();
        }
        
    }

    public static void clearProcessInstanceId() {
        processInstanceIdThreadLocal.remove();
    }

}
