package com.ca.apm.systemtest.fld.plugin.logmonitor;

import java.io.File;



public interface LogFileTailer {

    /**
     * Return the file.
     *
     * @return the file
     */
    public abstract File getFile();

    /**
     * Return the delay in milliseconds.
     *
     * @return the delay in milliseconds.
     */
    public abstract long getDelay();

    /**
     * Allows the tailer to complete its current loop and return.
     */
    public abstract void stop();

}
