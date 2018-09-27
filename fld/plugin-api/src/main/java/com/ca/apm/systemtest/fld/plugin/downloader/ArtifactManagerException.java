/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.downloader;

/**
 * @author KEYJA01
 *
 */
@SuppressWarnings("serial")
public class ArtifactManagerException extends Exception {

	public ArtifactManagerException() {
		super();
	}

	public ArtifactManagerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ArtifactManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ArtifactManagerException(String message) {
		super(message);
	}

	public ArtifactManagerException(Throwable cause) {
		super(cause);
	}
	
}
