package com.ca.apm.tests.testbed;

import com.ca.tas.artifact.IArtifactVersion;
import com.ca.tas.tests.annotations.TestBedDefinition;

@TestBedDefinition(cleanUpTestBed = ClusterRegressionTestBedCleaner.class)
public class ClusterRegressionBaseTestBed extends ClusterRegressionTestBed {

    @Override
    public IArtifactVersion getEmVersion() {
        return new IArtifactVersion() {
            @Override
            public String getValue() {
                return "10.5.2.10";
            }
        };

    }

}
