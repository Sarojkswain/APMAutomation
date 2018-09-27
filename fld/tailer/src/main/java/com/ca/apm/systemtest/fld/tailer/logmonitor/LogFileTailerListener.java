/**
 *
 */

package com.ca.apm.systemtest.fld.tailer.logmonitor;


/**
 * @author keyja01
 */
public interface LogFileTailerListener {

    /**
     * The tailer will call this method during construction,
     * giving the listener a method of stopping the tailer.
     *
     * @param tailer the tailer.
     */
    void init(LogFileTailer tailer);

    /**
     * This method is called if the tailed file is not found.

     * <b>Note:</b> this is called from the tailer thread.
     */
    void fileNotFound();

    /**
     * Called if a file rotation is detected.

     * This method is called before the file is reopened, and fileNotFound may
     * be called if the new file has not yet been created.

     * <b>Note:</b> this is called from the tailer thread.
     */
    void fileRotated();

    /**
     * Handles a line from a Tailer.

     * <b>Note:</b> this is called from the tailer thread.
     *
     * @param line the line.
     */
    void handle(String line);

    /**
     * Handles an Exception.
     *
     * <b>Note:</b> this is called from the tailer thread.
     *
     * @param ex the exception.
     */
    void handle(Exception ex);


}
