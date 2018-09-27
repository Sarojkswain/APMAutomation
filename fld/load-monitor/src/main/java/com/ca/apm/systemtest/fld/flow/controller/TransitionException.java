/**
 * 
 */
package com.ca.apm.systemtest.fld.flow.controller;

/**
 * @author keyja01
 *
 */
@SuppressWarnings("serial")
public class TransitionException extends Exception {

    /**
     * 
     */
    public TransitionException() {
    }

    /**
     * @param message
     */
    public TransitionException(String message) {
        super(message);
    }
}
