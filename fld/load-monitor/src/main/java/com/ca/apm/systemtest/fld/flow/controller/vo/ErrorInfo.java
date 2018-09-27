package com.ca.apm.systemtest.fld.flow.controller.vo;

/**
 * @author haiva01
 */
public class ErrorInfo extends BaseVO {
    String url;
    String message;

    public ErrorInfo(String url, String message) {
        super("error");
        this.url = url;
        this.message = message;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
