package com.ca.apm.systemtest.fld.plugin.dotnet;

/**
 * Created by haiva01 on 18.8.2015.
 */
public class Configuration {
    public String profilerType;
    public boolean enableSoa = false;
    public boolean enableCd = false;
    public String cdAgentId;
    public boolean enableSpp = false;
    public boolean installSpMonitor = false;

    public String getProfilerType() {
        return profilerType;
    }

    public void setProfilerType(String profilerType) {
        this.profilerType = profilerType;
    }

    public boolean isEnableSoa() {
        return enableSoa;
    }

    public void setEnableSoa(boolean enableSoa) {
        this.enableSoa = enableSoa;
    }

    public boolean isEnableCd() {
        return enableCd;
    }

    public void setEnableCd(boolean enableCd) {
        this.enableCd = enableCd;
    }

    public String getCdAgentId() {
        return cdAgentId;
    }

    public void setCdAgentId(String cdAgentId) {
        this.cdAgentId = cdAgentId;
    }

    public boolean isEnableSpp() {
        return enableSpp;
    }

    public void setEnableSpp(boolean enableSpp) {
        this.enableSpp = enableSpp;
    }

    public boolean isInstallSpMonitor() {
        return installSpMonitor;
    }

    public void setInstallSpMonitor(boolean installSpMonitor) {
        this.installSpMonitor = installSpMonitor;
    }
}
