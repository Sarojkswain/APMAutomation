package com.ca.apm.systemtest.alertstateload.testbed.regional;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;

import com.ca.tas.resolver.ITasResolver;

class ConfigurationImpl implements Configuration {

    private String testbedEmVersion;
    private boolean testbedLoaddMachinesOnWindows;
    private boolean testbedDbMachineOnWindows;
    private boolean testRunInitPhase;

    ConfigurationImpl(InputStream in) throws Exception {
        Properties props = new Properties();
        props.load(in);

        testbedEmVersion = props.getProperty("testbed.emVersion");
        testbedLoaddMachinesOnWindows =
            Boolean.valueOf(getPropOrDefault(props, "testbed.loadMachinesOnWindows", "true"));
        testbedDbMachineOnWindows =
            Boolean.valueOf(getPropOrDefault(props, "testbed.dbMachineOnWindows", "false"));
        testRunInitPhase = Boolean.valueOf(getPropOrDefault(props, "test.runInitPhase", "true"));
    }

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

    @Override
    public String getTestbedEmVersion(ITasResolver tasResolver) {
        return StringUtils.isBlank(testbedEmVersion)
            ? tasResolver.getDefaultVersion()
            : testbedEmVersion;
    }

    @Override
    public boolean isTestbedLoadMachinesOnWindows() {
        return testbedLoaddMachinesOnWindows;
    }

    @Override
    public boolean isTestbedDbMachineOnWindows() {
        return testbedDbMachineOnWindows;
    }

    @Override
    public boolean isTestRunInitPhase() {
        return testRunInitPhase;
    }

}
