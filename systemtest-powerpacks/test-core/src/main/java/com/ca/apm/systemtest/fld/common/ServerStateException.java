/**
 * 
 */
package com.ca.apm.systemtest.fld.common;

/**
 * Exception to indicate some problems in an application server. 
 * By catching this exception a user can decide to restart the server or 
 * shut down the running test.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class ServerStateException extends Throwable {

    private static final long serialVersionUID = 5167626344104262544L;

    public ServerStateException() {
        
    }

    public ServerStateException(String message) {
        super(message);
    }

}
