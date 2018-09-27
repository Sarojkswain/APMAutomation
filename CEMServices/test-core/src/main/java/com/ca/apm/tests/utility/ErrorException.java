package com.ca.apm.tests.utility;

public class ErrorException extends Exception {
	
	public ErrorException() {}
	//constructor for exception description
	public ErrorException(String description)
	    {
	    	super(description);
	    }

}
