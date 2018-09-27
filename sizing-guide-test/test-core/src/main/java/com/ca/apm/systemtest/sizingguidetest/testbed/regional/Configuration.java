package com.ca.apm.systemtest.sizingguidetest.testbed.regional;


public interface Configuration {

    String getTestbedEmVersion();

    String getTestbedDomainConfigVersion();

    String getTestbedDbTargetReleaseVersion();

    String getTestbedTessSmtpHost();

    String getTestbedReportEmail();

    Long getTestDurationMs();

}
