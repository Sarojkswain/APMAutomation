package com.ca.apm.systemtest.fld.shared.vo;

import java.util.List;

public class ErrorMessage extends Response {

    private List<String> errors;

    public ErrorMessage() {}

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

}
