package com.ca.apm.systemtest.fld.plugin.downloader;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class ArtifactManagerImplTest {
    ArtifactManagerImpl am;

    @BeforeMethod
    public void setUp() throws Exception {
        am = new ArtifactManagerImpl();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        am = null;
    }


}