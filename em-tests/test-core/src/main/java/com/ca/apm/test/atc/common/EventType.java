package com.ca.apm.test.atc.common;

public enum EventType {
    TOPOLOGICAL_CHANGE("TOPOLOGICAL_CHANGE"), STATUS_CHANGE("STATUS_CHANGE"), ATTRIBUTE_CHANGE("GATHERED_ATTRIBUTES_CHANGE");
    
    private final String text;
    
    private EventType(String text) {
        this.text = text;
    }
    
    public String asText() {
        return text;
    }
}