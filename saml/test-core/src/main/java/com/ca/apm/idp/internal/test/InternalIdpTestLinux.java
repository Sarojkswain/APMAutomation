package com.ca.apm.idp.internal.test;

import java.net.MalformedURLException;

import com.ca.apm.test.testbed.SamlEmInternalIdpTestbedLinux;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.Test;

@Test(groups = {"idpTest", "linux"})
@Tas(testBeds = @TestBed(name = SamlEmInternalIdpTestbedLinux.class, executeOn =
    SamlEmInternalIdpTestbedLinux.MACHINE_ID), exclusivity = ExclusivityType.EXCLUSIVE, size =
    SizeType.SMALL)
public class InternalIdpTestLinux extends InternalIdpTest {

    @Override
    public WebDriver createWebDriver() throws MalformedURLException {
        return createWebDriver(DesiredCapabilities.chrome());
    }

    @Test(enabled = false)
    @Override
    public void installationTest() {
        super.installationTest();
    }
}
