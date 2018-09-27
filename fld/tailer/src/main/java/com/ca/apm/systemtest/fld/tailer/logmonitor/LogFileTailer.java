package com.ca.apm.systemtest.fld.tailer.logmonitor;

import java.io.File;


public interface LogFileTailer {

    /**
     * Return the file.
     *
     * @return the file
     */
    File getFile();

    /**
     * Return the delay in milliseconds.
     *
     * @return the delay in milliseconds.
     */
    long getDelay();

    /**
     * Allows the tailer to complete its current loop and return.
     */
    void stop();

}
