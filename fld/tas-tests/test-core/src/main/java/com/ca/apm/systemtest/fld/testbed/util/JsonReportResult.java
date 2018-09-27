package com.ca.apm.systemtest.fld.testbed.util;

/**
 * Wrapper object for reports REST controller's responses.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class JsonReportResult {
    public static final String ERROR_RESULT = "error";
    public static final String OK_RESULT = "ok";
    
    private String result;
    private String url;
    private String message;

    /**
     * @return the result
     */
    public String getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    public void setResult(String result) {
        this.result = result;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * Tells if the request failed on server side.
     * 
     * @return
     */
    public boolean isError() {
        return ERROR_RESULT.equalsIgnoreCase(result);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "JsonReportResult [result=" + result + ", url=" + url + ", message=" + message + "]";
    }
    
}