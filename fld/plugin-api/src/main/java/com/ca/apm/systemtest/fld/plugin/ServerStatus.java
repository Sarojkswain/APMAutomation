package com.ca.apm.systemtest.fld.plugin;

/**
 * Stopped: not running<br>
 * Started: running<br>
 * Starting: starting up<br>
 * Error: did not start
 * @author keyja01
 *
 */
public enum ServerStatus {
    Stopped, Starting, Started, Error, Timeout, Stopping
}
