package com.ca.apm.tests.testbed.jvm6;

import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @author kurma05
 */
@TestBedDefinition
public class Weblogic103NoRedefTestBed extends Weblogic103TestBed {

    public Weblogic103NoRedefTestBed () {
        isNoRedefEnabled = true;
    }
}
