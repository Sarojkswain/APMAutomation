package com.ca.apm.systemtest.fld.shared.vo;

import java.io.Serializable;

import com.ca.apm.systemtest.fld.plugin.vo.Operation;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * JSON wrapper for FLD plugins
 * @author ZUNPA01
 *
 */
@JsonInclude(Include.NON_NULL)
public class PluginVO implements Serializable {
    private static final long serialVersionUID = -6986071016517014273L;

    private String name;
    private Operation[] operations;

    public PluginVO() {
    }

    public PluginVO(String name, Operation[] operations) {
        this.name = name;
        this.operations = operations;
    }

    public String getName() {
        return (name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public Operation[] getOperations() {
        return (operations);
    }

    public void setOperations(Operation[] operations) {
        this.operations = operations;
    }

}
