package com.ca.apm.systemtest.fld.shared.vo;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Response {

    private String status;

    public Response() {}

    public Response(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public Response setStatus(String status) {
        this.status = status;
        return this;
    }

    public Response setStatus(HttpStatus status) {
        return setStatus(status.toString());
    }

}
