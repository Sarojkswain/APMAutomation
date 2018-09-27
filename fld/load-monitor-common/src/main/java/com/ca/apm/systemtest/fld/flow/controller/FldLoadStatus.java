/**
 * 
 */
package com.ca.apm.systemtest.fld.flow.controller;


/**
 * @author keyja01
 *
 */
public enum FldLoadStatus implements HasSuffix {
    DOSTART("dostart"), ISSTARTING("isstarting"), STARTED("started"), FAILEDSTART("failedstart"),
    DOSTOP("dostop"), ISSTOPPING("isstopping"), STOPPED("stopped"), FAILEDSTOP("failedstop"),
    ISSHUTTINGDOWN("exiting"), DOSHUTDOWN("doshutdown"), NEW("new");

    private final String suffix;
    private final String effectiveSuffix;

    FldLoadStatus(String suffix) {
        this.suffix = suffix;
        this.effectiveSuffix = "." + suffix;
    }

    @Override
    public String getSuffix() {
        return suffix;
    }

    @Override
    public String getEffectiveSuffix() {
        return effectiveSuffix;
    }
}
