package com.ca.apm.systemtest.fld.flow.controller.vo;

import java.util.List;

/**
 * @author haiva01
 */
public class LoadsStatusesVO extends BaseVO {
    List<LoadStatusVO> statuses;

    public LoadsStatusesVO(
        List<LoadStatusVO> statuses) {
        this.statuses = statuses;
    }

    public List<LoadStatusVO> getStatuses() {
        return statuses;
    }

    public void setStatuses(
        List<LoadStatusVO> statuses) {
        this.statuses = statuses;
    }
}
