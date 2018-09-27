package com.ca.apm.tests.testbed;

import com.ca.tas.type.Platform;

/**
 * Created by jirji01 on 5/18/2017.
 */
public class UpgradeTestbed_100_rh6 extends UpgradeTestbed {
    @Override
    public Platform platform() {
        return Platform.LINUX;
    }

    @Override
    public String version() {
        return "10.0.0.36";
    }

    @Override
    public String template() {
        return "co66";
    }
}
