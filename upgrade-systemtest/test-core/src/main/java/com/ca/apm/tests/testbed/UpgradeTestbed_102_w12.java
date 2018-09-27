package com.ca.apm.tests.testbed;

import com.ca.tas.type.Platform;

/**
 * Created by jirji01 on 5/18/2017.
 */
public class UpgradeTestbed_102_w12 extends UpgradeTestbed {
    @Override
    public Platform platform() {
        return Platform.WINDOWS;
    }

    @Override
    public String version() {
        return "10.2.0.15";
    }

    @Override
    public String template() {
        return "w12";
    }
}
