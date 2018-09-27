package com.ca.apm.tests.testbed.jvm7.acc.onprem;

import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @author kurma05
 */
@TestBedDefinition
public class Websphere8NoRedefTestBed extends Websphere8TestBed {

    public Websphere8NoRedefTestBed () {
        isNoRedefEnabled = true;
    }
}
