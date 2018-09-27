package com.ca.apm.systemtest.fld.server.rest;

public class AgentDistroIsAlreadyPreconfigured extends RuntimeException {

    private static final long serialVersionUID = -7354769689498970903L;

    public AgentDistroIsAlreadyPreconfigured() {
        
    }
    
    public AgentDistroIsAlreadyPreconfigured(String message) {
        super(message);
    }

    
}
