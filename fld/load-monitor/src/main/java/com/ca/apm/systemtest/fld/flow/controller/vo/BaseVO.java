package com.ca.apm.systemtest.fld.flow.controller.vo;

/**
 * Created by haiva01 on 17.5.2016.
 */
public class BaseVO {
    String result = "OK";

    public BaseVO() {

    }

    public BaseVO(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
