package com.ca.apm.systemtest.fld.testbed.util;

/**
 * Enum for specifying load status in email report. 
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public enum LoadStatus {
    STARTED, STOPPED, SHUTDOWN, START_FAILED, STOP_FAILED, SHUTDOWN_FAILED;
}