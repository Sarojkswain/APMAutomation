package com.ca.apm.systemtest.fld.flow.controller.vo;

import com.ca.apm.systemtest.fld.flow.controller.FldLoadStatus;

/**
 * @author haiva01
 */
public class LoadStatusVO {
    String name;
    FldLoadStatus status;

    public LoadStatusVO(String name, FldLoadStatus status) {
        this.name = name;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FldLoadStatus getStatus() {
        return status;
    }

    public void setStatus(FldLoadStatus status) {
        this.status = status;
    }
}
