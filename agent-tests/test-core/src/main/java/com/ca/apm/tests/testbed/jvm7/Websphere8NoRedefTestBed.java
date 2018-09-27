package com.ca.apm.tests.testbed.jvm7;

import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Jass Automation - JVM 7
 *
 * @author kurma05
 */
@TestBedDefinition
public class Websphere8NoRedefTestBed extends Websphere8TestBed {

    public Websphere8NoRedefTestBed () {
        isNoRedefEnabled = true;
    }
}
