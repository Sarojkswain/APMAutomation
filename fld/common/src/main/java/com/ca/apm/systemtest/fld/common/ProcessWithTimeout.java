
package com.ca.apm.systemtest.fld.common;

public class ProcessWithTimeout extends Thread {
    private Process process;
    private int exitCode = Integer.MIN_VALUE;

    public ProcessWithTimeout(Process process) {
        this.process = process;
    }

    /**
     * Wait for process specified time. 
     * @param timeoutMilliseconds
     * @return process return code or Integer.MIN_VALUE on timeout
     */
    public int waitForProcess(long timeoutMilliseconds) {
        this.start();

        try {
            this.join(timeoutMilliseconds);
        } catch (InterruptedException e) {
            this.interrupt();
        }

        return exitCode;
    }

    @Override
    public void run() {
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException ignore) {
            // Do nothing
        } catch (Exception ex) {
            // Unexpected exception
        }
    }
}
