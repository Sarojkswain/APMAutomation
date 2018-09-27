package com.ca.apm.tests.testbed.jvm8.acc.onprem;

import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @author kurma05
 */
@TestBedDefinition
public class Tomcat8NoRedefTestBed extends Tomcat8TestBed {

    public Tomcat8NoRedefTestBed () {
        isNoRedefEnabled = true;
    }
}
