package com.ca.apm.tests.testbed.jvm8;

import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @author kurma05
 */
@TestBedDefinition
public class Weblogic12NoRedefTestBed extends Weblogic12TestBed {

    public Weblogic12NoRedefTestBed () {
        isNoRedefEnabled = true;
    }
}
