package com.ca.apm.systemtest.fld.server.util;

import java.util.Collection;

import org.activiti.validation.ValidationError;

/**
 * This exception is thrown as a result of BPMN validation error.
 * Created by haiva01 on 20.3.2015.
 */
public class BpmnValidationError extends RuntimeException {
    private static final long serialVersionUID = -5599076978560278691L;

    private Collection<ValidationError> errors;
    private String name;

    public BpmnValidationError(String name, Collection<ValidationError> errors) {
        super(name + " has " + errors.size() + " errors.");
        this.name = name;
        this.errors = errors;
    }

    public Collection<ValidationError> getErrors() {
        return errors;
    }

    public void setErrors(Collection<ValidationError> errors) {
        this.errors = errors;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getMessage() {
        String basicMessage = super.getMessage();
        StringBuffer sb = new StringBuffer(basicMessage);
        for (ValidationError err : errors) {
            sb.append("\n" + err.toString());
        }
        return sb.toString();
    }
}
