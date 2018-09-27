package com.ca.apm.tests.testbed;

import com.ca.tas.type.Platform;

/**
 * Created by jirji01 on 5/18/2017.
 */
public class UpgradeTestbed_103_rh6 extends UpgradeAgcTestbed {
    @Override
    public Platform platform() {
        return Platform.LINUX;
    }

    @Override
    public String version() {
        return "10.3.0.16";
    }

    @Override
    public String template() {
        return "co66";
    }
}
