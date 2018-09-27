/**
 * 
 */
package com.ca.apm.systemtest.fld.flow.controller;


/**
 * @author bocto01
 * @author keyja01
 *
 */
public enum FldLoadCommand implements HasSuffix {
    DOSTART("dostart"), DOSTOP("dostop"), FORCEDOSTART("forcedostart"), FORCEDOSTOP("forcedostop");

    private String suffix;
    private String effectiveSuffix;

    private FldLoadCommand(String suffix) {
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
