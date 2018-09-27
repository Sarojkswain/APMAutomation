/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.downloader;

/**
 * @author keyja01
 *
 */
@SuppressWarnings("serial")
public class InvalidArtifactSpecificationException extends RuntimeException {

    /**
     * 
     */
    public InvalidArtifactSpecificationException() {
    }

    /**
     * @param message
     */
    public InvalidArtifactSpecificationException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public InvalidArtifactSpecificationException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public InvalidArtifactSpecificationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public InvalidArtifactSpecificationException(String message, Throwable cause,
        boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
