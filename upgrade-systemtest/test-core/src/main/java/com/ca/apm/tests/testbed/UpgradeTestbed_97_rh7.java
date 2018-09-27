package com.ca.apm.tests.testbed;

import com.ca.tas.type.Platform;

/**
 * Created by jirji01 on 5/18/2017.
 */
public class UpgradeTestbed_97_rh7 extends UpgradeTestbed {
    @Override
    public Platform platform() {
        return Platform.LINUX;
    }

    @Override
    public String version() {
        return "9.7.1.42";
    }

    @Override
    public String template() {
        return "rh7";
    }
}
