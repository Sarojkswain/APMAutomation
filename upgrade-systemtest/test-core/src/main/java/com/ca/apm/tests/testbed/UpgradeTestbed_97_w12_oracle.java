package com.ca.apm.tests.testbed;

import com.ca.tas.type.Platform;

/**
 * Created by jirji01 on 5/18/2017.
 */
public class UpgradeTestbed_97_w12_oracle extends UpgradeOracleTestbed {
    @Override
    public Platform platform() {
        return Platform.WINDOWS;
    }

    @Override
    public String version() {
        return "9.7.1.20";
    }

    @Override
    public String template() {
        return "w12";
    }
}
