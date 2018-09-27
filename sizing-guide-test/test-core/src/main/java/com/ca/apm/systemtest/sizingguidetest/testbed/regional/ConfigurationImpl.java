package com.ca.apm.systemtest.sizingguidetest.testbed.regional;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;

class ConfigurationImpl implements Configuration {

    private String testbedEmVersion;
    private String testbedDomainConfigVersion;
    private String testbedDbTargetReleaseVersion;
    private String testbedTessSmtpHost;
    private String testbedReportEmail;
    private Long testDurationMs;

    ConfigurationImpl(InputStream in) throws Exception {
        Properties props = new Properties();
        props.load(in);

        testbedEmVersion = props.getProperty("testbed.emVersion");
        testbedDomainConfigVersion = props.getProperty("testbed.testbedDomainConfigVersion");
        testbedDbTargetReleaseVersion = props.getProperty("testbed.testbedDbTargetReleaseVersion");
        testbedTessSmtpHost = props.getProperty("testbed.testbedTessSmtpHost");
        testbedReportEmail = props.getProperty("testbed.testbedTestbedReportEmail");
        testDurationMs = getLong(props.getProperty("test.durationMs"));
    }

    @SuppressWarnings("unused")
    private static String getPropOrDefault(Properties props, String key, String defaultValue) {
        String value = props.getProperty(key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        return value;
    }

    @SuppressWarnings("unused")
    private static Map<String, String> parseRoleTemplateMap(String src) {
        Map<String, String> map = new HashMap<>();
        StringTokenizer st = new StringTokenizer(src, ",");
        while (st.hasMoreElements()) {
            String token = st.nextToken();
            StringTokenizer st2 = new StringTokenizer(token, "=");
            if (st2.countTokens() != 2) {
                throw new IllegalArgumentException(
                    "Error while parsing role template map: invalid pair => " + token);
            }
            map.put(st2.nextToken().trim(), st2.nextToken().trim());
        }
        return map;
    }

    private static Long getLong(String s) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String getTestbedEmVersion() {
        return testbedEmVersion;
    }

    @Override
    public String getTestbedDomainConfigVersion() {
        return testbedDomainConfigVersion;
    }

    @Override
    public String getTestbedDbTargetReleaseVersion() {
        return testbedDbTargetReleaseVersion;
    }

    @Override
    public String getTestbedTessSmtpHost() {
        return testbedTessSmtpHost;
    }

    @Override
    public String getTestbedReportEmail() {
        return testbedReportEmail;
    }

    @Override
    public Long getTestDurationMs() {
        return testDurationMs;
    }

}
