package com.ca.apm.tests.tests;

import com.ca.tas.test.TasTestNgTest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.tests.testbed.ConfigUtilityWindowsTestbed;
import com.ca.apm.tests.utils.Utility;
import com.ca.apm.tests.utils.XmlFileUtils;
import com.ca.apm.tests.utils.XmlModifications;
import com.ca.apm.tests.testbed.ConfigUtilityLinuxTestbed;

public class SampleTest extends TasTestNgTest 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SampleTest.class);
    private Utility  util = new Utility();
    private XmlFileUtils xmlUtils = new XmlFileUtils();

    //Install locations for Various installation instances
    public String InstLoc_EMFULL ;
    public String InstLoc_EMFULL_old ;
    public String InstLoc_EM ;
    public String InstLoc_WV ;
    public String InstLoc_ACC ;
    public String InstLoc_WS ;
    public String OLD_EM_VERSION ;

    {
        //Install locations for Various installation instances

        if (System.getProperty("os.name").toUpperCase().contains("WINDOWS")) {
            InstLoc_EMFULL = ConfigUtilityWindowsTestbed.InstLoc_EMFULL;
            InstLoc_EMFULL_old = ConfigUtilityWindowsTestbed.InstLoc_EMFULL_old;
            InstLoc_EM = ConfigUtilityWindowsTestbed.InstLoc_EM;
            InstLoc_WV = ConfigUtilityWindowsTestbed.InstLoc_WV;
            InstLoc_ACC = ConfigUtilityWindowsTestbed.InstLoc_ACC;
            InstLoc_WS = ConfigUtilityWindowsTestbed.InstLoc_WS;
            OLD_EM_VERSION = ConfigUtilityWindowsTestbed.OLD_EM_VERSION;
        }
        else if (System.getProperty("os.name").toUpperCase().contains("LINUX")) {
            InstLoc_EMFULL = ConfigUtilityLinuxTestbed.InstLoc_EMFULL;
            InstLoc_EMFULL_old = ConfigUtilityLinuxTestbed.InstLoc_EMFULL_old;
            InstLoc_EM = ConfigUtilityLinuxTestbed.InstLoc_EM;
            InstLoc_WV = ConfigUtilityLinuxTestbed.InstLoc_WV;
            InstLoc_ACC = ConfigUtilityLinuxTestbed.InstLoc_ACC;
            InstLoc_WS = ConfigUtilityLinuxTestbed.InstLoc_WS;
            OLD_EM_VERSION = ConfigUtilityLinuxTestbed.OLD_EM_VERSION;
        }

    }
    private String sourceConfig = InstLoc_EMFULL+"/config";
    private String destinationConfig = InstLoc_EMFULL_old+"/config";
    private String configLocation = InstLoc_EMFULL+"/config";
    XmlModifications xml = new XmlModifications();
   
    
    @Test(groups = { "BAT","EM"  })
    public void verify_ALM_XXXX_Test(){
       
        Assert.assertTrue(true);
       
    }


}