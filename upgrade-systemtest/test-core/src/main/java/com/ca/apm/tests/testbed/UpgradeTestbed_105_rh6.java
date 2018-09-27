package com.ca.apm.tests.testbed;

import com.ca.tas.type.Platform;

/**
 * Created by jirji01 on 5/18/2017.
 */
public class UpgradeTestbed_105_rh6 extends UpgradeAgcTestbed {
    @Override
    public Platform platform() {
        return Platform.LINUX;
    }

    @Override
    public String version() {
        return "10.5.0.12";
    }

    @Override
    public String template() {
        return "co66";
    }
}
