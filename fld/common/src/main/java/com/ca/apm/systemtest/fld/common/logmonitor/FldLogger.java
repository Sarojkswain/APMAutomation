package com.ca.apm.systemtest.fld.common.logmonitor;


public interface FldLogger {
	
    public void log(FldLevel level, String category, String tag, String message, Throwable cause);
    
    public void trace(String category, String tag, String message);
    public void trace(String category, String tag, String message, Throwable cause);
    
    public void debug(String category, String tag, String message);
    public void debug(String category, String tag, String message, Throwable cause);

    public void info(String category, String tag, String message);
    public void info(String category, String tag, String message, Throwable cause);
    
    public void warn(String category, String tag, String message);
    public void warn(String category, String tag, String message, Throwable cause);
    
    public void error(String category, String tag, String message);
    public void error(String category, String tag, String message, Throwable cause);
    
}