package com.ca.apm.tests.testbed.jvm6;

import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Jass Automation - JVM 6
 *
 * @author kurma05
 */
@TestBedDefinition
public class Websphere8NoRedefTestBed extends Websphere8TestBed {

    public Websphere8NoRedefTestBed () {
        isNoRedefEnabled = true;
    }
}
