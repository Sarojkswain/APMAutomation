package com.ca.apm.tests.tests;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.ca.apm.tests.utils.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.tests.testbed.ConfigUtilityLinuxTestbed;
import com.ca.apm.tests.testbed.ConfigUtilityWindowsTestbed;
import com.ca.apm.tests.utils.Utility;
import com.ca.tas.test.TasTestNgTest;

public class ConfigUtilityTests extends TasTestNgTest 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigUtilityTests.class);
    private Utility  util = new Utility();
    private FileUtils futils = new FileUtils();

    //Install locations for Various installation instances
    public String InstLoc_EMFULL ;
    public String InstLoc_EM ;
    public String InstLoc_WV ;
    public String InstLoc_ACC ;
    public String InstLoc_WS ;
    public String OLD_EM_VERSION ;
    public String InstLoc_EMFULL_old ;

    {
        //Install locations for Various installation instances

        if (System.getProperty("os.name").toUpperCase().contains("WINDOWS")) {
            InstLoc_EMFULL = ConfigUtilityWindowsTestbed.InstLoc_EMFULL;
            InstLoc_EM = ConfigUtilityWindowsTestbed.InstLoc_EM;
            InstLoc_WV = ConfigUtilityWindowsTestbed.InstLoc_WV;
            InstLoc_ACC = ConfigUtilityWindowsTestbed.InstLoc_ACC;
            InstLoc_WS = getWsInstallDir(ConfigUtilityWindowsTestbed.InstLoc_WS);
            OLD_EM_VERSION = ConfigUtilityWindowsTestbed.OLD_EM_VERSION;
            InstLoc_EMFULL_old = ConfigUtilityWindowsTestbed.InstLoc_EMFULL_old;
        }
        else if (System.getProperty("os.name").toUpperCase().contains("LINUX")) {
            InstLoc_EMFULL = ConfigUtilityLinuxTestbed.InstLoc_EMFULL;
            InstLoc_EM = ConfigUtilityLinuxTestbed.InstLoc_EM;
            InstLoc_WV = ConfigUtilityLinuxTestbed.InstLoc_WV;
            InstLoc_ACC = ConfigUtilityLinuxTestbed.InstLoc_ACC;
            //InstLoc_WS = getWsInstallDir(ConfigUtilityLinuxTestbed.InstLoc_WS);
            InstLoc_EMFULL_old = ConfigUtilityLinuxTestbed.InstLoc_EMFULL_old;
        }
    }
    private String configLocation = InstLoc_EMFULL+"/config";
    private String getWsInstallDir(String baseLoc){
        String installLocation = baseLoc;
        File file = new File(baseLoc);
        String[] names = file.list();

        for(String name : names)
        {
            if (new File(baseLoc+"/" + name).isDirectory())
            {
                installLocation = baseLoc+"/" + name;
            }
        }
        return installLocation;

    }

    /**
     * Test Case ID : 454906
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454906_runConfigUtilityWithInvalidDestinationConfigFolder() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        try {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454906 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL);

            Boolean flag =  util.verifyCommandOutput(p,"Configuration Comparison completed!!");

            Assert.assertTrue(flag);
        } catch (Exception e) {

        }}

    /**
     * Test Case ID : 454767
     * Test case description
     */

    @Test(groups = {"configUtility", "SMOKE"})

    public void verify_ALM_454767_runFromNonDefaultLocation() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        try {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454767 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            //copy jar to cusotom location
            ApmbaseUtil.copy(InstLoc_EMFULL+"/tools/ConfigUtility.jar", InstLoc_EMFULL+"/ConfigUtility.jar");
            ApmbaseUtil.fileExists(InstLoc_EMFULL+"/ConfigUtility.jar");
            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL);
            //       ApmbaseUtil.deleteFile(InstLoc_EMFULL+"/ConfigUtility.jar");
            Boolean flag =  util.verifyCommandOutput(p,"Configuration Comparison completed!!");

            Assert.assertTrue(flag);
        } catch (Exception e) {

        }

    }

    /**
     * Test Case ID : 454855
     * Test case description
     */

    @Test(groups = {"configUtility", "SMOKE"})

    public void verify_ALM_454855_ReportFileAlreadyExists() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        try {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454855 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            //run jar to create report file
            Process p1 = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");

            //run the jar again
            Process p2 = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");

            Boolean flag1 =  util.verifyCommandOutput(p2,"Configuration Comparison completed!!");
            Boolean flag2 = ApmbaseUtil.fileExists(InstLoc_EMFULL+"/tools/ConfigChanges.txt");

            Assert.assertTrue(flag1&&flag2);
        } catch (Exception e) {

        }
    }


    /**
     * Test Case ID : 455116
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_455116_defaultResultFileLocation() {
        String[] command1 = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        String[] command2 = {"java -jar ConfigUtility.jar -listchanges -d "+configLocation};
        try {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 455116 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            //run cmd for view changes
            Process p1 = ApmbaseUtil.getProcess(command1, InstLoc_EMFULL +"/tools");
            Boolean flag1 =  (util.verifyCommandOutput(p1,"Report Generated")&&ApmbaseUtil.fileExists(InstLoc_EMFULL+"/tools/ConfigChanges.txt"));

            //run cmd for list changes
            Process p2 = ApmbaseUtil.getProcess(command2, InstLoc_EMFULL +"/tools");
            Boolean flag2 = ( util.verifyCommandOutput(p2,"Report Generated")&&ApmbaseUtil.fileExists(InstLoc_EMFULL+"/tools/ConfigChangeList.txt"));



            Assert.assertTrue(flag1&&flag2);
        } catch (Exception e) {

        }
    }

    /**
     * Test Case ID : 454764
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454764_runWithzeroInputs() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges "};
        try {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454764 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");

            Boolean flag =  util.verifyCommandOutput(p,"Configuration Comparison completed!!");

            Assert.assertTrue(flag);
        } catch (Exception e) {

        }
    }

    /**
     * Test Case ID : 454766
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454766_runWithcustomResultFileLocation() {
        String[] command1 = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation+" -f "+InstLoc_EMFULL+"/testViewChanges.txt"};
        String[] command2 = {"java -jar ConfigUtility.jar -listchanges -d "+configLocation+" -f "+InstLoc_EMFULL+"/testListChanges.txt"};
        try {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454766 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            //run cmd for view changes
            Process p1 = ApmbaseUtil.getProcess(command1, InstLoc_EMFULL+"/tools");
            Process p2 = ApmbaseUtil.getProcess(command2, InstLoc_EMFULL+"/tools");

            util.verifyCommandOutput(p1, "Configuration Comparison completed!!");
            util.verifyCommandOutput(p2, "Configuration Comparison completed!!");

            Boolean flag1 =  ApmbaseUtil.fileExists(InstLoc_EMFULL+"/testViewChanges.txt");
            Boolean flag2 =  ApmbaseUtil.fileExists(InstLoc_EMFULL+"/testListChanges.txt");

            Assert.assertTrue(flag1&&flag2);
        } catch (Exception e) {

        }}

    /**
     * Test Case ID : 454765
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454765_runWithInvalidConfigFolder() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation+"/invalid"};
        try {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454765 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");

            Boolean flag =  util.verifyCommandOutput(p,"Config Directory is not Available!!");

            Assert.assertTrue(flag);
        } catch (Exception e) {

        }}



    /**
     * Test Case ID : 454839
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454839_runWithInvalidRreportFileLocation() {
        String[] command1 = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation+" -f "+InstLoc_EMFULL+"/invalid/testviewChanges.txt"};
        String[] command2 = {"java -jar ConfigUtility.jar -listchanges -d "+configLocation+" -f "+InstLoc_EMFULL+"/invalid/testlistChanges.txt"};
        try {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454839 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            //run cmd for view changes
            Process p1 = ApmbaseUtil.getProcess(command1, InstLoc_EMFULL+"/tools");
            Boolean flag1 =  util.verifyCommandOutput(p1,"Invalid directory. Please provide a valid directory for the output file");
            //run cmd for list changes
            Process p2 = ApmbaseUtil.getProcess(command2, InstLoc_EMFULL+"/tools");
            Boolean flag2 =  util.verifyCommandOutput(p2,"Invalid directory. Please provide a valid directory for the output file");

            Assert.assertTrue(flag1&&flag2);
        } catch (Exception e) {

        }}

    /**
     * Test Case ID : 454867
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454867_runWithInvalidRreportFileExtension() {
        String[] command1 = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation+" -f "+InstLoc_EMFULL+"/testviewChanges.pdf"};
        String[] command2 = {"java -jar ConfigUtility.jar -listchanges -d "+configLocation+" -f "+InstLoc_EMFULL+"/testviewChanges.pdf"};
        try {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454867 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            Process p1 = ApmbaseUtil.getProcess(command1, InstLoc_EMFULL+"/tools");
            Process p2 = ApmbaseUtil.getProcess(command2, InstLoc_EMFULL);

            util.verifyCommandOutput(p1, "Configuration Comparison completed!!");
            util.verifyCommandOutput(p2, "Configuration Comparison completed!!");
            Boolean flag1 =  ApmbaseUtil.fileExists(InstLoc_EMFULL+"/testviewChanges.pdf.txt");
            Boolean flag2 =  ApmbaseUtil.fileExists(InstLoc_EMFULL+"/testviewChanges.pdf.txt");

            Assert.assertTrue(flag1&&flag2);
        } catch (Exception e) {

        }}

    /**
     * Test Case ID : 454775
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454775_resultFileDefaultLocation() {
        String[] command1 = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        String[] command2 = {"java -jar ConfigUtility.jar -listchanges -d "+configLocation};
        try {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454775 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            Process p1 = ApmbaseUtil.getProcess(command1, InstLoc_EMFULL+"/tools");
            Process p2 = ApmbaseUtil.getProcess(command2, InstLoc_EMFULL+"/tools");

            util.verifyCommandOutput(p1, "Configuration Comparison completed!!");
            util.verifyCommandOutput(p2, "Configuration Comparison completed!!");
            Boolean flag1 =  ApmbaseUtil.fileExists(InstLoc_EMFULL+"/tools/ConfigChanges.txt");
            Boolean flag2 =  ApmbaseUtil.fileExists(InstLoc_EMFULL+"/tools/ConfigChangeList.txt");

            Assert.assertTrue(flag1&&flag2);
        } catch (Exception e) {

        }}


    /**
     * Test Case ID : 454776
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454776_resultFileDefaultExtension() {
        String[] command1 = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        String[] command2 = {"java -jar ConfigUtility.jar -listchanges -d "+configLocation};
        try {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454776 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            Process p1 = ApmbaseUtil.getProcess(command1, InstLoc_EMFULL+"/tools");
            Process p2 = ApmbaseUtil.getProcess(command2, InstLoc_EMFULL+"/tools");

            util.verifyCommandOutput(p1, "Configuration Comparison completed!!");
            util.verifyCommandOutput(p2, "Configuration Comparison completed!!");
            Boolean flag1 =  ApmbaseUtil.fileExists(InstLoc_EMFULL+"/tools/ConfigChanges.txt");
            Boolean flag2 =  ApmbaseUtil.fileExists(InstLoc_EMFULL+"/tools/ConfigChangeList.txt");

            Assert.assertTrue(flag1&&flag2);
        } catch (Exception e) {

        }}

    /**
     * Test Case ID : 454817
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454817_APMEnterpriseManagerPropertiesFileTest() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454817 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        String fileName = "APMEnterpriseManager.properties";
        String property = "c3p0.maxPoolSize";
        String value = "4";
        String modifiedValue = "5";
        String newProperty = "new.property";
        String newValue = "newValue";
        String filePath = configLocation+"/"+fileName;


        try{

            //comment existing property
            ApmbaseUtil.fileBackUp(filePath);
            futils.commentPropertyInPropertiesFile(filePath, property);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Modify an existing property value
            ApmbaseUtil.fileBackUp(filePath);
            futils.modifyPropertyInPropertiesFile(filePath, property, modifiedValue );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property,  value,modifiedValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Delete an existing property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.removeProperties(fileName, configLocation, Arrays.asList(property+"="+value));
            //   futils.deletePropertyInPropertiesFile(configLocation + "/APMEnterpriseManager.properties", "c3p0.maxPoolSize", "4" );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Add any new property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.appendProperties(Arrays.asList(newProperty+"="+newValue),fileName, configLocation);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,newProperty,  "",newValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }
        catch(Exception e){ }}

    /*
     * Testvase Id #454816
     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454816_catalystPropertiesFileTest() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454816 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        String fileName = "Catalyst.properties";
        String property = "catalyst.alert.filter.total";
        String value = "250";
        String modifiedValue = "300";
        String newProperty = "new.property";
        String newValue = "newValue";
        String filePath = configLocation+"/"+fileName;
        String commentedProperty = "catalyst.alert.filter.time";
        String commentedPropValue = "72";

        try{

            //comment existing property
            ApmbaseUtil.fileBackUp(filePath);
            futils.commentPropertyInPropertiesFile(filePath, property);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Modify an existing property value
            ApmbaseUtil.fileBackUp(filePath);
            futils.modifyPropertyInPropertiesFile(filePath, property, modifiedValue );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property,  value,modifiedValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Delete an existing property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.removeProperties(fileName, configLocation, Arrays.asList(property+"="+value));
            //   futils.deletePropertyInPropertiesFile(configLocation + "/APMEnterpriseManager.properties", "c3p0.maxPoolSize", "4" );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Add any new property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.appendProperties(Arrays.asList(newProperty+"="+newValue),fileName, configLocation);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,newProperty, "",newValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //uncomment a property
            ApmbaseUtil.fileBackUp(filePath);
            futils.uncommentPropertyinPropertiesFile(filePath,commentedProperty);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,commentedProperty,"",commentedPropValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }
        catch (Exception e){}
    }


    /*
     * Testvase Id #454807
     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454807_CustomDBRecordTypesPropertiesFileTest(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454807 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        String fileName = "CustomDBRecordTypes.properties";
        String newProperty = "new.property";
        String newValue = "newValue";
        String filePath = configLocation+"/"+fileName;
        String commentedProperty = "introscope.enterprisemanager.database.recordtypes.typestring.#";
        String commentedPropValue = "Record Type Here";

        //There are only commented properties in this file

        try{
            //Add any new property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.appendProperties(Arrays.asList(newProperty+"="+newValue),fileName, configLocation);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,newProperty, "",newValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //uncomment a property
            ApmbaseUtil.fileBackUp(filePath);
            futils.uncommentPropertyinPropertiesFile(filePath,commentedProperty);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,commentedProperty,"",commentedPropValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }
        catch(Exception e){ }
    }


    /*
     * Testvase Id #454819
     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454819_ESAPIPropertiesFileTest() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454819 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        String fileName = "ESAPI.properties";
        String property = "ESAPI.printProperties";
        String value = "true";
        String modifiedValue = "false";
        String newProperty = "new.property";
        String newValue = "newValue";
        String filePath = configLocation+"/esapi/"+fileName;
        String commentedProperty = "ESAPI.Randomizer";
        String commentedPropValue = "org.owasp.esapi.reference.DefaultRandomizer";

        try{

            //comment existing property
            ApmbaseUtil.fileBackUp(filePath);
            futils.commentPropertyInPropertiesFile(filePath, property);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Modify an existing property value
            ApmbaseUtil.fileBackUp(filePath);
            futils.modifyPropertyInPropertiesFile(filePath, property, modifiedValue );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property,  value,modifiedValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Delete an existing property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.removeProperties(fileName, configLocation, Arrays.asList(property+"="+value));
            //   futils.deletePropertyInPropertiesFile(configLocation + "/APMEnterpriseManager.properties", "c3p0.maxPoolSize", "4" );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Add any new property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.appendProperties(Arrays.asList(newProperty+"="+newValue),fileName, configLocation);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,newProperty, "",newValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //uncomment a property
            ApmbaseUtil.fileBackUp(filePath);
            futils.uncommentPropertyinPropertiesFile(filePath,commentedProperty);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,commentedProperty,"",commentedPropValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }
        catch (Exception e){}
    }

    /*
     * Testvase Id #454783
     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454783_introscopeEmLaxFileTest() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454783 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        String fileName = "Introscope_Enterprise_Manager.lax";
        String property = "lax.stderr.redirect";
        String value = "console";
        String modifiedValue = "logFile";
        String newProperty = "new.property";
        String newValue = "newValue";
        String filePath = InstLoc_EMFULL+"/"+fileName;
        //no commented properties in this file

        try{

            //comment existing property
            ApmbaseUtil.fileBackUp(filePath);
            futils.commentPropertyInPropertiesFile(filePath, property);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Modify an existing property value
            ApmbaseUtil.fileBackUp(filePath);
            futils.modifyPropertyInPropertiesFile(filePath, property, modifiedValue );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property,  value,modifiedValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Delete an existing property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.removeProperties(fileName, configLocation, Arrays.asList(property+"="+value));
            //   futils.deletePropertyInPropertiesFile(configLocation + "/APMEnterpriseManager.properties", "c3p0.maxPoolSize", "4" );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Add any new property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.appendProperties(Arrays.asList(newProperty+"="+newValue),fileName, configLocation);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,newProperty, "",newValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }
        catch (Exception e){}
    }



    /*
     * Testvase Id #454782
     */
    @Test(groups = { "SMOKE","Webview"  })
    public void verify_ALM_454782_IntroscopeWebviewLaxFileTest() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454782 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        String fileName = "Introscope_WebView.lax";
        String property = "lax.application.name";
        String value = "Introscope_WebView";
        String modifiedValue = "Introscope_new_Webview";
        String newProperty = "new.property";
        String newValue = "newValue";
        String filePath = InstLoc_EMFULL+"/"+fileName;
        //no commented properties in this file

        try{

            //comment existing property
            ApmbaseUtil.fileBackUp(filePath);
            futils.commentPropertyInPropertiesFile(filePath, property);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Modify an existing property value
            ApmbaseUtil.fileBackUp(filePath);
            futils.modifyPropertyInPropertiesFile(filePath, property, modifiedValue );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property,  value,modifiedValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Delete an existing property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.removeProperties(fileName, configLocation, Arrays.asList(property+"="+value));
            //   futils.deletePropertyInPropertiesFile(configLocation + "/APMEnterpriseManager.properties", "c3p0.maxPoolSize", "4" );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Add any new property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.appendProperties(Arrays.asList(newProperty+"="+newValue),fileName, configLocation);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,newProperty, "",newValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }
        catch (Exception e){}
    }


    /*
     * Testvase Id #454781
     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454781_IntroscopeWorkstationLaxFileTest() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454781 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        try{
            if (System.getProperty("os.name").toUpperCase().contains("WINDOWS")) {

                String fileName = "Introscope_Workstation.lax";
                String property = "lax.application.name";
                String value = "Introscope_Workstation.exe";
                String modifiedValue = "new_application.exe";
                String newProperty = "new.property";
                String newValue = "newValue";
                String filePath = InstLoc_WS+"/"+fileName;
                //no commented properties in this file

                //comment existing property
                ApmbaseUtil.fileBackUp(filePath);
                futils.commentPropertyInPropertiesFile(filePath, property);
                p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
                if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                    flag = true;
                }
                ApmbaseUtil.revertFile(filePath);

                //Modify an existing property value
                ApmbaseUtil.fileBackUp(filePath);
                futils.modifyPropertyInPropertiesFile(filePath, property, modifiedValue );
                p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
                if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property,  value,modifiedValue))){
                    flag = true;
                }
                ApmbaseUtil.revertFile(filePath);

                //Delete an existing property
                ApmbaseUtil.fileBackUp(filePath);
                ApmbaseUtil.removeProperties(fileName, configLocation, Arrays.asList(property+"="+value));
                //   futils.deletePropertyInPropertiesFile(configLocation + "/APMEnterpriseManager.properties", "c3p0.maxPoolSize", "4" );
                p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
                if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                    flag = true;
                }
                ApmbaseUtil.revertFile(filePath);

                //Add any new property
                ApmbaseUtil.fileBackUp(filePath);
                ApmbaseUtil.appendProperties(Arrays.asList(newProperty+"="+newValue),fileName, configLocation);
                p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
                if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,newProperty, "",newValue))){
                    flag = true;
                }
                ApmbaseUtil.revertFile(filePath);
            }
            else  if (System.getProperty("os.name").toUpperCase().contains("LINUX")) {
                LOGGER.info("This test case scenario is valid only on Windows OS");
                LOGGER.info("Skipping test case steps for this test case on LINUX OS");
                flag=true;
            }

            Assert.assertTrue(flag);
        }
        catch (Exception e){}
    }





    /*
     * Testvase Id #454820
     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454820_IntroscopeAgentPropertiesFileTest(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454820 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        String fileName = "IntroscopeAgent.profile";
        String property = "introscope.agent.defaultProcessName";
        String value = "APM Introscope WebView";
        String modifiedValue = "New APM Introscope Webview";
        String newProperty = "new.property";
        String newValue = "newValue";
        String filePath = InstLoc_EMFULL+"/product/webview/agent/wily/core/config/"+fileName;
        String commentedProperty = "introscope.agent.customProcessName";
        String commentedPropValue = "CustomProcessName";

        try{

            //comment existing property
            ApmbaseUtil.fileBackUp(filePath);
            futils.commentPropertyInPropertiesFile(filePath, property);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Modify an existing property value
            ApmbaseUtil.fileBackUp(filePath);
            futils.modifyPropertyInPropertiesFile(filePath, property, modifiedValue );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property,  value,modifiedValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Delete an existing property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.removeProperties(fileName, configLocation, Arrays.asList(property+"="+value));
            //   futils.deletePropertyInPropertiesFile(configLocation + "/APMEnterpriseManager.properties", "c3p0.maxPoolSize", "4" );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Add any new property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.appendProperties(Arrays.asList(newProperty+"="+newValue),fileName, configLocation);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,newProperty, "",newValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //uncomment a property
            ApmbaseUtil.fileBackUp(filePath);
            futils.uncommentPropertyinPropertiesFile(filePath,commentedProperty);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,commentedProperty,"",commentedPropValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }
        catch (Exception e){}
    }



    /*
     * Testvase Id #454814
     */
    @Test(groups = { "BAT","EM"  })
    public void verify_ALM_454814_IntroscopeEnterpriseManagerPropertiesFileTest() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454814 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        String fileName = "IntroscopeEnterpriseManager.properties";
        String property = "introscope.enterprisemanager.db.reconnect.intervalInSeconds";
        String value = "30";
        String modifiedValue = "50";
        String newProperty = "new.property";
        String newValue = "newValue";
        String filePath = configLocation+"/"+fileName;
        String commentedProperty = "introscope.enterprisemanager.webserver.max.threads";
        String commentedPropValue = "100";

        try{

            //comment existing property
            ApmbaseUtil.fileBackUp(filePath);
            futils.commentPropertyInPropertiesFile(filePath, property);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Modify an existing property value
            ApmbaseUtil.fileBackUp(filePath);
            futils.modifyPropertyInPropertiesFile(filePath, property, modifiedValue );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property,  value,modifiedValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Delete an existing property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.removeProperties(fileName, configLocation, Arrays.asList(property+"="+value));
            //   futils.deletePropertyInPropertiesFile(configLocation + "/APMEnterpriseManager.properties", "c3p0.maxPoolSize", "4" );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Add any new property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.appendProperties(Arrays.asList(newProperty+"="+newValue),fileName, configLocation);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,newProperty, "",newValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //uncomment a property
            ApmbaseUtil.fileBackUp(filePath);
            futils.uncommentPropertyinPropertiesFile(filePath,commentedProperty);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,commentedProperty,"",commentedPropValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }
        catch (Exception e){}
    }


    /*
     * Testvase Id #454813
     */
    @Test(groups = { "BAT","EM"  })
    public void verify_ALM_454813_IntroscopeWebviewPropertiesFileTest() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454813 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        String fileName = "IntroscopeWebview.properties";
        String property = "introscope.webview.startup.emcheck.interval";
        String value = "10";
        String modifiedValue = "15";
        String newProperty = "new.property";
        String newValue = "newValue";
        String filePath = configLocation+"/"+fileName;
        String commentedProperty = "introscope.webview.jetty.configurationFile";
        String commentedPropValue = "webview-jetty-config.xml";

        try{

            //comment existing property
            ApmbaseUtil.fileBackUp(filePath);
            futils.commentPropertyInPropertiesFile(filePath, property);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Modify an existing property value
            ApmbaseUtil.fileBackUp(filePath);
            futils.modifyPropertyInPropertiesFile(filePath, property, modifiedValue );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property,  value,modifiedValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Delete an existing property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.removeProperties(fileName, configLocation, Arrays.asList(property+"="+value));
            //   futils.deletePropertyInPropertiesFile(configLocation + "/APMEnterpriseManager.properties", "c3p0.maxPoolSize", "4" );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Add any new property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.appendProperties(Arrays.asList(newProperty+"="+newValue),fileName, configLocation);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,newProperty, "",newValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //uncomment a property
            ApmbaseUtil.fileBackUp(filePath);
            futils.uncommentPropertyinPropertiesFile(filePath,commentedProperty);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,commentedProperty,"",commentedPropValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }
        catch (Exception e){}
    }


    /*
     * Testvase Id #454812
     */
    @Test(groups = { "BAT","EM"  })
    public void verify_ALM_454812_workstationPropertiesFileTest() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454812 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        String fileName = "IntroscopeWorkstation.properties";
        String property = "introscope.workstation.startup.emcheck.interval";
        String value = "10";
        String modifiedValue = "15";
        String newProperty = "new.property";
        String newValue = "newValue";
        String filePath = InstLoc_WS+"/config/"+fileName;


        try{

            if (System.getProperty("os.name").toUpperCase().contains("WINDOWS")) {
            //comment existing property
            ApmbaseUtil.fileBackUp(filePath);
            futils.commentPropertyInPropertiesFile(filePath, property);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Modify an existing property value
            ApmbaseUtil.fileBackUp(filePath);
            futils.modifyPropertyInPropertiesFile(filePath, property, modifiedValue );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property,  value,modifiedValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Delete an existing property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.removeProperties(fileName, configLocation, Arrays.asList(property+"="+value));
            //   futils.deletePropertyInPropertiesFile(configLocation + "/APMEnterpriseManager.properties", "c3p0.maxPoolSize", "4" );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Add any new property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.appendProperties(Arrays.asList(newProperty+"="+newValue),fileName, configLocation);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,newProperty, "",newValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);
            }
            else  if (System.getProperty("os.name").toUpperCase().contains("LINUX")) {
                           LOGGER.info("This test case scenario is valid only on WINDOWS OS as TAS does not have Workstartion builder yet on Linux");
                           LOGGER.info("Skipping test case steps for this test case on LINUX OS");
                           flag=true;
                       }
            Assert.assertTrue(flag);
        }
        catch (Exception e){}
    }




    /*
     * Testvase Id #454811
     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454811_log4jPropertiesFileTest(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454811 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        String fileName = "log4j.properties";
        String property = "log4j.rootCategory";
        String value = "INFO, stdout";
        String modifiedValue = "DEBUG, stdout";
        String newProperty = "new.property";
        String newValue = "newValue";
        String filePath = configLocation+"/"+fileName;
        String commentedProperty = "log4j.appender.stdout";
        String commentedPropValue = "com.wily.org.apache.log4j.ConsoleAppender";


        try{

            //comment existing property
            ApmbaseUtil.fileBackUp(filePath);
            futils.commentPropertyInPropertiesFile(filePath, property);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Modify an existing property value
            ApmbaseUtil.fileBackUp(filePath);
            futils.modifyPropertyInPropertiesFile(filePath, property, modifiedValue );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property,  value,modifiedValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Delete an existing property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.removeProperties(fileName, configLocation, Arrays.asList(property+"="+value));
            //   futils.deletePropertyInPropertiesFile(configLocation + "/APMEnterpriseManager.properties", "c3p0.maxPoolSize", "4" );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Add any new property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.appendProperties(Arrays.asList(newProperty+"="+newValue),fileName, configLocation);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,newProperty, "",newValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //uncomment a property
            ApmbaseUtil.fileBackUp(filePath);
            futils.uncommentPropertyinPropertiesFile(filePath,commentedProperty);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,commentedProperty,"",commentedPropValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }
        catch (Exception e){}
    }



    /*
     * Testvase Id #454806
     */
    @Test(groups = { "BAT","EM"  })
    public void verify_ALM_454806_teamcenter_status_mappingPropertiesFileTest() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454806 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        String fileName = "teamcenter-status-mapping.properties";
        String property = "SERVLET.1";
        String value = "Servlets|<servletClassname>";
        String modifiedValue = "Servlets|<newClass>";
        String newProperty = "new.property";
        String newValue = "newValue";
        String filePath = configLocation+"/"+fileName;
        // no commented property in this file


        try{

            //comment existing property
            ApmbaseUtil.fileBackUp(filePath);
            futils.commentPropertyInPropertiesFile(filePath, property);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Modify an existing property value
            ApmbaseUtil.fileBackUp(filePath);
            futils.modifyPropertyInPropertiesFile(filePath, property, modifiedValue );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property,  value,modifiedValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Delete an existing property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.removeProperties(fileName, configLocation, Arrays.asList(property+"="+value));
            //   futils.deletePropertyInPropertiesFile(configLocation + "/APMEnterpriseManager.properties", "c3p0.maxPoolSize", "4" );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Add any new property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.appendProperties(Arrays.asList(newProperty+"="+newValue),fileName, configLocation);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,newProperty, "",newValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }
        catch (Exception e){}
    }


    /*
     * Testvase Id #454804
     */
    @Test(groups = { "BAT","EM"  })
    public void verify_ALM_454804_tess_defaultPropertiesFileTest(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454804 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        String fileName = "tess-default.properties";
        String property = "ENABLE_LOW_SEVERITY";
        String value = "true";
        String modifiedValue = "false";
        String newProperty = "new.property";
        String newValue = "newValue";
        String filePath = configLocation+"/"+fileName;
        String commentedProperty = "stats.bytesPerRow";
        String commentedPropValue = "2225";


        try{

            //comment existing property
            ApmbaseUtil.fileBackUp(filePath);
            futils.commentPropertyInPropertiesFile(filePath, property);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Modify an existing property value
            ApmbaseUtil.fileBackUp(filePath);
            futils.modifyPropertyInPropertiesFile(filePath, property, modifiedValue );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property,  value,modifiedValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Delete an existing property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.removeProperties(fileName, configLocation, Arrays.asList(property+"="+value));
            //   futils.deletePropertyInPropertiesFile(configLocation + "/APMEnterpriseManager.properties", "c3p0.maxPoolSize", "4" );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Add any new property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.appendProperties(Arrays.asList(newProperty+"="+newValue),fileName, configLocation);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,newProperty, "",newValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //uncomment a property
            ApmbaseUtil.fileBackUp(filePath);
            futils.uncommentPropertyinPropertiesFile(filePath,commentedProperty);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,commentedProperty,"",commentedPropValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);


            Assert.assertTrue(flag);
        }
        catch (Exception e){}
    }


    /*
     * Testvase Id #454818
     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454818_validationPropertiesFileTest() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454818 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        String fileName = "validation.properties";
        String property = "Validator.Email";
        String value = "^[A-Za-z0-9._%'-]+@[A-Za-z0-9.-]+\\.[a-zA-Z]{2,4}$";
        String modifiedValue = "^*+@*";
        String newProperty = "new.property";
        String newValue = "newValue";
        String filePath = configLocation+"/esapi/"+fileName;
        // no commented properties


        try{

            //comment existing property
            ApmbaseUtil.fileBackUp(filePath);
            futils.commentPropertyInPropertiesFile(filePath, property);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Modify an existing property value
            ApmbaseUtil.fileBackUp(filePath);
            futils.modifyPropertyInPropertiesFile(filePath, property, modifiedValue );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property,  value,modifiedValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Delete an existing property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.removeProperties(fileName, configLocation, Arrays.asList(property+"="+value));
            //   futils.deletePropertyInPropertiesFile(configLocation + "/APMEnterpriseManager.properties", "c3p0.maxPoolSize", "4" );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Add any new property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.appendProperties(Arrays.asList(newProperty+"="+newValue),fileName, configLocation);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,newProperty, "",newValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }
        catch (Exception e){}
    }


    /*
     * Testvase Id #454809
     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454809_CEMHibernate_dailyAggregationPropertiesFileTest(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454809 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        String fileName = "CEMHibernate-dailyAggregation.properties";
        String property = "dataSourceName";
        String value = "cemDataSource";
        String modifiedValue = "newDataSource";
        String newProperty = "new.property";
        String newValue = "newValue";
        String filePath = configLocation+"/"+fileName;
        // no commented properties


        try{

            //comment existing property
            ApmbaseUtil.fileBackUp(filePath);
            futils.commentPropertyInPropertiesFile(filePath, property);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Modify an existing property value
            ApmbaseUtil.fileBackUp(filePath);
            futils.modifyPropertyInPropertiesFile(filePath, property, modifiedValue );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property,  value,modifiedValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Delete an existing property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.removeProperties(fileName, configLocation, Arrays.asList(property+"="+value));
            //   futils.deletePropertyInPropertiesFile(configLocation + "/APMEnterpriseManager.properties", "c3p0.maxPoolSize", "4" );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Add any new property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.appendProperties(Arrays.asList(newProperty+"="+newValue),fileName, configLocation);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,newProperty, "",newValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }
        catch (Exception e){}
    }


    /*
     * Testvase Id #454808
     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454808_CEMHibernate_defectAggregationPropertiesFileTest() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454808 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        String fileName = "CEMHibernate-defectAggregation.properties";
        String property = "dataSourceName";
        String value = "cemDataSource";
        String modifiedValue = "newDataSource";
        String newProperty = "new.property";
        String newValue = "newValue";
        String filePath = configLocation+"/"+fileName;
        // no commented properties


        try{

            //comment existing property
            ApmbaseUtil.fileBackUp(filePath);
            futils.commentPropertyInPropertiesFile(filePath, property);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Modify an existing property value
            ApmbaseUtil.fileBackUp(filePath);
            futils.modifyPropertyInPropertiesFile(filePath, property, modifiedValue );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property,  value,modifiedValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Delete an existing property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.removeProperties(fileName, configLocation, Arrays.asList(property+"="+value));
            //   futils.deletePropertyInPropertiesFile(configLocation + "/APMEnterpriseManager.properties", "c3p0.maxPoolSize", "4" );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Add any new property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.appendProperties(Arrays.asList(newProperty+"="+newValue),fileName, configLocation);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,newProperty, "",newValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }
        catch (Exception e){}

    }

    /*
     * Testvase Id #454810
     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454810_ResourceMetricMapPropertiesFileTest() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454810 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        String fileName = "ResourceMetricMap.properties";
        String property = "threads.used.path.1";
        String value = "WebSpherePMI|threadPoolModule|WebContainer:ActiveCount";
        String modifiedValue = "WebSpherePMI|WebContainer:ActiveCount";
        String newProperty = "new.property";
        String newValue = "newValue";
        String filePath = configLocation+"/"+fileName;
        // no commented properties

        try{

            //comment existing property
            ApmbaseUtil.fileBackUp(filePath);
            futils.commentPropertyInPropertiesFile(filePath, property);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Modify an existing property value
            ApmbaseUtil.fileBackUp(filePath);
            futils.modifyPropertyInPropertiesFile(filePath, property, modifiedValue );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property,  value,modifiedValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Delete an existing property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.removeProperties(fileName, configLocation, Arrays.asList(property+"="+value));
            //   futils.deletePropertyInPropertiesFile(configLocation + "/APMEnterpriseManager.properties", "c3p0.maxPoolSize", "4" );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Add any new property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.appendProperties(Arrays.asList(newProperty+"="+newValue),fileName, configLocation);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,newProperty, "",newValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }
        catch (Exception e){}

    }



    /*
     * Testvase Id #454815
     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454815_CEMHibernatePropertiesFileTest() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454815 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        String fileName = "CEMHibernate.properties";
        String property = "dataSourceName";
        String value = "cemDataSource";
        String modifiedValue = "cemDataSourceNew";
        String newProperty = "new.property";
        String newValue = "newValue";
        String filePath = configLocation+"/"+fileName;

        try{

            //comment existing property
            ApmbaseUtil.fileBackUp(filePath);
            futils.commentPropertyInPropertiesFile(filePath, property);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Modify an existing property value
            ApmbaseUtil.fileBackUp(filePath);
            futils.modifyPropertyInPropertiesFile(filePath, property, modifiedValue );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property,  value,modifiedValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Delete an existing property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.removeProperties(fileName, configLocation, Arrays.asList(property+"="+value));
            //   futils.deletePropertyInPropertiesFile(configLocation + "/APMEnterpriseManager.properties", "c3p0.maxPoolSize", "4" );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Add any new property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.appendProperties(Arrays.asList(newProperty+"="+newValue),fileName, configLocation);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,newProperty, "",newValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }
        catch (Exception e){}
    }

    /*
     * Testvase Id #454780
     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454780_emServiceConfFileTest(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454780 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        try{ 
            if (System.getProperty("os.name").toUpperCase().contains("WINDOWS")) {

                String fileName = "EMService.conf";
                String property = "wrapper.lang.folder";
                String value = "./lib/lang";
                String modifiedValue = "./lib/newLang";
                String newProperty = "new.property";
                String newValue = "newValue";
                String filePath = InstLoc_EMFULL+"/bin/"+fileName;
                String commentedProperty = "wrapper.license.debug";
                String commentedPropValue = "TRUE";


                //comment existing property
                ApmbaseUtil.fileBackUp(filePath);
                futils.commentPropertyInPropertiesFile(filePath, property);
                p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
                if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                    flag = true;
                }
                ApmbaseUtil.revertFile(filePath);

                //Modify an existing property value
                ApmbaseUtil.fileBackUp(filePath);
                futils.modifyPropertyInPropertiesFile(filePath, property, modifiedValue );
                p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
                if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property,  value,modifiedValue))){
                    flag = true;
                }
                ApmbaseUtil.revertFile(filePath);

                //Delete an existing property
                ApmbaseUtil.fileBackUp(filePath);
                ApmbaseUtil.removeProperties(fileName, InstLoc_EMFULL+"/bin/", Arrays.asList(property+"="+value));
                //   futils.deletePropertyInPropertiesFile(configLocation + "/APMEnterpriseManager.properties", "c3p0.maxPoolSize", "4" );
                p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
                if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                    flag = true;
                }
                ApmbaseUtil.revertFile(filePath);

                //Add any new property
                ApmbaseUtil.fileBackUp(filePath);
                ApmbaseUtil.appendProperties(Arrays.asList(newProperty+"="+newValue),fileName, InstLoc_EMFULL+"/bin/");
                p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
                if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,newProperty, "",newValue))){
                    flag = true;
                }
                ApmbaseUtil.revertFile(filePath);

                //uncomment a property
                ApmbaseUtil.fileBackUp(filePath);
                futils.uncommentPropertyinPropertiesFile(filePath,commentedProperty);
                p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
                if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,commentedProperty,"",commentedPropValue))){
                    flag = true;
                }
                ApmbaseUtil.revertFile(filePath);


                Assert.assertTrue(flag);   
            } 
            else  if (System.getProperty("os.name").toUpperCase().contains("LINUX")) {
                LOGGER.info("This test case scenario is valid only on Windows OS");
                LOGGER.info("Skipping test case steps for this test case on LINUX OS");
                flag=true;
            }
        }
        catch(Exception e){ }
    }


    /*
     * Testvase Id #454779
     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454779_wvServiceConfFileTest() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454779 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        try{ 
            if (System.getProperty("os.name").toUpperCase().contains("WINDOWS")) {

                String fileName = "Wvservice.conf";
                String property = "wrapper.lang.folder";
                String value = "./lib/lang";
                String modifiedValue = "./bin/newLang";
                String newProperty = "new.property";
                String newValue = "newValue";
                String filePath = InstLoc_EMFULL+"/bin/"+fileName;
                String commentedProperty = "wrapper.license.debug";
                String commentedPropValue = "TRUE";


                //comment existing property
                ApmbaseUtil.fileBackUp(filePath);
                futils.commentPropertyInPropertiesFile(filePath, property);
                p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
                if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                    flag = true;
                }
                ApmbaseUtil.revertFile(filePath);

                //Modify an existing property value
                ApmbaseUtil.fileBackUp(filePath);
                futils.modifyPropertyInPropertiesFile(filePath, property, modifiedValue );
                p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
                if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property,  value,modifiedValue))){
                    flag = true;
                }
                ApmbaseUtil.revertFile(filePath);

                //Delete an existing property
                ApmbaseUtil.fileBackUp(filePath);
                ApmbaseUtil.removeProperties(fileName, InstLoc_EMFULL+"/bin", Arrays.asList(property+"="+value));
                //   futils.deletePropertyInPropertiesFile(configLocation + "/APMEnterpriseManager.properties", "c3p0.maxPoolSize", "4" );
                p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
                if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                    flag = true;
                }
                ApmbaseUtil.revertFile(filePath);

                //Add any new property
                ApmbaseUtil.fileBackUp(filePath);
                ApmbaseUtil.appendProperties(Arrays.asList(newProperty+"="+newValue),fileName, InstLoc_EMFULL+"/bin");
                p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
                if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,newProperty, "",newValue))){
                    flag = true;
                }
                ApmbaseUtil.revertFile(filePath);

                //uncomment a property
                ApmbaseUtil.fileBackUp(filePath);
                futils.uncommentPropertyinPropertiesFile(filePath,commentedProperty);
                p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
                if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,commentedProperty,"",commentedPropValue))){
                    flag = true;
                }
                ApmbaseUtil.revertFile(filePath);
            } 
            else  if (System.getProperty("os.name").toUpperCase().contains("LINUX")) {
                LOGGER.info("This test case scenario is valid only on Windows OS");
                LOGGER.info("Skipping test case steps for this test case on LINUX OS");
                flag=true;
            }
            Assert.assertTrue(flag);
        }
        catch(Exception e){ }

    }

    /*
     * Testvase Id #454803
     */
    @Test(groups = { "BAT","ACC"  })
    public void verify_ALM_454803_apmccsrvPropertiesFileTest() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454803 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        try{ 
            String fileName = "apmccsrv.properties";
            String property = "javax.net.ssl.keyStore";
            String value = "config/security/default.keystore";
            String modifiedValue = "config/security/new.keystore";
            String newProperty = "new.property";
            String newValue = "newValue";
            String filePath = InstLoc_ACC+"/config/"+fileName;

            //comment existing property
            ApmbaseUtil.fileBackUp(filePath);
            futils.commentPropertyInPropertiesFile(filePath, property);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Modify an existing property value
            ApmbaseUtil.fileBackUp(filePath);
            futils.modifyPropertyInPropertiesFile(filePath, property, modifiedValue );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property,  value,modifiedValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Delete an existing property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.removeProperties(fileName, InstLoc_ACC+"/config/", Arrays.asList(property+"="+value));
            //   futils.deletePropertyInPropertiesFile(configLocation + "/APMEnterpriseManager.properties", "c3p0.maxPoolSize", "4" );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            //Add any new property
            ApmbaseUtil.fileBackUp(filePath);
            ApmbaseUtil.appendProperties(Arrays.asList(newProperty+"="+newValue),fileName, InstLoc_ACC+"/config/");
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,newProperty, "",newValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);   
        }
        catch(Exception e){ }    
    }



    /*
     * Testvase Id #454805
     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454805_tessCustomPropertiesFileTest() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454805 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        try{ 
            String fileName = "tess-custom.properties";
            String property = "db.oracle.tablespaceWarning";
            String value = "2000";
            String modifiedValue = "2500";
            String newProperty = "new.property";
            String newValue = "newValue";
            String filePath = InstLoc_EMFULL+"/config/"+fileName;

            if(new File(filePath).exists()){

                //comment existing property
                ApmbaseUtil.fileBackUp(filePath);
                futils.commentPropertyInPropertiesFile(filePath, property);
                p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
                if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                    flag = true;
                }
                ApmbaseUtil.revertFile(filePath);

                //Modify an existing property value
                ApmbaseUtil.fileBackUp(filePath);
                futils.modifyPropertyInPropertiesFile(filePath, property, modifiedValue );
                p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
                if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property,  value,modifiedValue))){
                    flag = true;
                }
                ApmbaseUtil.revertFile(filePath);

                //Delete an existing property
                ApmbaseUtil.fileBackUp(filePath);
                ApmbaseUtil.removeProperties(fileName, configLocation, Arrays.asList(property+"="+value));
                //   futils.deletePropertyInPropertiesFile(configLocation + "/APMEnterpriseManager.properties", "c3p0.maxPoolSize", "4" );
                p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
                if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, property, value,""))){
                    flag = true;
                }
                ApmbaseUtil.revertFile(filePath);

                //Add any new property
                ApmbaseUtil.fileBackUp(filePath);
                ApmbaseUtil.appendProperties(Arrays.asList(newProperty+"="+newValue),fileName, configLocation);
                p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
                if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt",fileName,newProperty, "",newValue))){
                    flag = true;
                }
                ApmbaseUtil.revertFile(filePath);

            }
            else{
                LOGGER.info(" UPDATED : Tess-custom file doesnot come by default, skipping the testcase.");
                flag = true;
            }
            Assert.assertTrue(flag);   

        }
        catch(Exception e){ }  
    }

    /*
     * Testvase Id #454868
     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454868_NumericValuesTest() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454868 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        try{ 

            String fileName = "Catalyst.properties";
            String property = "catalyst.alert.filter.total";
            String value = "250";
            String modifiedValue = "300";
            String filePath = configLocation+"/"+fileName;

            //Modify an existing property value
            ApmbaseUtil.fileBackUp(filePath);
            futils.modifyPropertyInPropertiesFile(filePath, property, modifiedValue );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property,  value,modifiedValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);   
        }
        catch(Exception e){ }  
    }

    /*
     * Testvase Id #454869
     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454869_StringValuesTest() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454869 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        try{ 

            String fileName = "Introscope_Enterprise_Manager.lax";
            String property = "lax.stderr.redirect";
            String value = "console";
            String modifiedValue = "logFile";
            String filePath = InstLoc_EMFULL+"/"+fileName;

            //Modify an existing property value
            ApmbaseUtil.fileBackUp(filePath);
            futils.modifyPropertyInPropertiesFile(filePath, property, modifiedValue );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property,  value,modifiedValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);   
        }
        catch(Exception e){ }  
    }

    /*
     * Testvase Id #454874
     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454874_ValuesWithSpaceTest() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454874 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        try{ 

            String fileName = "log4j.properties";
            String property = "log4j.rootCategory";
            String value = "INFO, stdout";
            String modifiedValue = "DEBUG, stdout";
            String filePath = configLocation+"/"+fileName;

            //Modify an existing property value
            ApmbaseUtil.fileBackUp(filePath);
            futils.modifyPropertyInPropertiesFile(filePath, property, modifiedValue );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property,  value,modifiedValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);   
        }
        catch(Exception e){ }  
    }

    /*
     * Testvase Id #454870
     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454870_ValuesWithDotTest() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454870 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        try{ 
            String fileName = "validation.properties";
            String property = "Validator.Email";
            String value = "^[A-Za-z0-9._%'-]+@[A-Za-z0-9.-]+\\.[a-zA-Z]{2,4}$";
            String modifiedValue = "^*+@*";
            String filePath = configLocation+"/esapi/"+fileName;

            //Modify an existing property value
            ApmbaseUtil.fileBackUp(filePath);
            futils.modifyPropertyInPropertiesFile(filePath, property, modifiedValue );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property,  value,modifiedValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);   
        }
        catch(Exception e){ }  
    }

    /*
     * Testvase Id #454871
     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454871_ValuesWithRegExTest() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454871 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        try{ 
            String fileName = "validation.properties";
            String property = "Validator.Email";
            String value = "^[A-Za-z0-9._%'-]+@[A-Za-z0-9.-]+\\.[a-zA-Z]{2,4}$";
            String modifiedValue = "^*+@*";
            String filePath = configLocation+"/esapi/"+fileName;

            //Modify an existing property value
            ApmbaseUtil.fileBackUp(filePath);
            futils.modifyPropertyInPropertiesFile(filePath, property, modifiedValue );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property,  value,modifiedValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);   
        }
        catch(Exception e){ }  
    }

    /*
     * Testvase Id #454872
     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454872_PathValuesTest() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454872 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        try{ 
            String fileName = "EMService.conf";
            String property = "wrapper.lang.folder";
            String value = "./lib/lang";
            String modifiedValue = "./lib/newLang";
            String filePath = InstLoc_EMFULL+"/bin/"+fileName;

            //Modify an existing property value
            ApmbaseUtil.fileBackUp(filePath);
            futils.modifyPropertyInPropertiesFile(filePath, property, modifiedValue );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property,  value,modifiedValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);   
        }
        catch(Exception e){ }  
    }
    /*
     * Testvase Id #454852
     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454852_AlphaNumericValuesTest() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454852 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        try{ 

            String fileName = "IntroscopeEnterpriseManager.properties";
            String property = "introscope.enterprisemanager.enabled.channels";
            String value = "channel1";
            String modifiedValue = "channel2";
            String filePath = configLocation+"/"+fileName;

            //Modify an existing property value
            ApmbaseUtil.fileBackUp(filePath);
            futils.modifyPropertyInPropertiesFile(filePath, property, modifiedValue );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property,  value,modifiedValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);   
        }
        catch(Exception e){ }  
    }

    /*
     * Testvase Id #454873
     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454873_BooleanValuesTest() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454873 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        try{ 

            String fileName = "ESAPI.properties";
            String property = "ESAPI.printProperties";
            String value = "true";
            String modifiedValue = "false";
            String filePath = configLocation+"/esapi/"+fileName;

            //Modify an existing property value
            ApmbaseUtil.fileBackUp(filePath);
            futils.modifyPropertyInPropertiesFile(filePath, property, modifiedValue );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property,  value,modifiedValue))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);   
        }
        catch(Exception e){ }  
    }

    /*
     * Testvase Id #454771
     */
    @Test(groups = { "BAT","EM"  })
    public void verify_ALM_454771_jarPresentAfterEmInstalation() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454771 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        Boolean flag = false;
        flag =  ApmbaseUtil.fileExists(InstLoc_EMFULL+"/tools/ConfigUtility.jar");

        Assert.assertTrue(flag);   
    }

    /*
     * Testvase Id #454772
     */
    @Test(groups = { "BAT","EM"  })
    public void verify_ALM_454772_jarPresentAfterWebviewInstalation() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454772 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        Boolean flag = false;
        flag =  ApmbaseUtil.fileExists(InstLoc_WV+"/tools/ConfigUtility.jar");

        Assert.assertTrue(flag);   
    }
    /*
     * Testvase Id #454853
     */
    @Test(groups = { "BAT","EM"  })
    public void verify_ALM_454853_ViewChangesForMultipleChanges() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454853 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag1 = false;
        Boolean flag2 = false;
        Process p1 ;
        Process p2;
        try{ 

            String fileName = "IntroscopeEnterpriseManager.properties";
            String property1 = "introscope.enterprisemanager.enabled.channels";
            String value1 = "channel1";
            String modifiedValue1 = "channel2";
            String property2 = "introscope.enterprisemanager.db.reconnect.intervalInSeconds";
            String value2 = "30";
            String modifiedValue2 = "50";
            String filePath = configLocation+"/"+fileName;

            //Modify an existing property value
            ApmbaseUtil.fileBackUp(filePath);
            futils.modifyPropertyInPropertiesFile(filePath, property1, modifiedValue1 );
            futils.modifyPropertyInPropertiesFile(filePath, property2, modifiedValue2 );
            p1 = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            p2 = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p1,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property1,  value1,modifiedValue1))){
                flag1 = true;
            }
            if((util.verifyCommandOutput(p2,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName,property2,  value2,modifiedValue2))){
                flag2 = true;
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag1&&flag2);   
        }
        catch(Exception e){ }  
    }

    /*
     * Testvase Id #454889
     */
    @Test(groups = { "BAT","EM"  })
    public void verify_ALM_454889_ListChangesForMultipleChanges() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454889 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        String[] command = {"java -jar ConfigUtility.jar -listchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;
        int x ;
        try{ 

            String fileName = "IntroscopeEnterpriseManager.properties";
            String property1 = "introscope.enterprisemanager.enabled.channels";
            String value1 = "channel1";
            String modifiedValue1 = "channel2";
            String property2 = "introscope.enterprisemanager.db.reconnect.intervalInSeconds";
            String value2 = "30";
            String modifiedValue2 = "50";
            String filePath = configLocation+"/"+fileName;

            //Modify an existing property value
            ApmbaseUtil.fileBackUp(filePath);
            futils.modifyPropertyInPropertiesFile(filePath, property1, modifiedValue1 );
            futils.modifyPropertyInPropertiesFile(filePath, property2, modifiedValue2 );
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            x = ApmbaseUtil.checkMessages(new LinkedList<String>(Arrays.asList(fileName)), new File (InstLoc_EMFULL+"/tools/ConfigChangeList.txt"));
            System.out.println(">>>>>>>>> "+x);
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(x==1);   
        }
        catch(Exception e){ }  
    }

    /**
     * Test Case ID : 454854
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454854_VerifyFileGeneration() {
        String[] command = {"useradd nonRoot", "su nonRoot", "java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        try {
            if (System.getProperty("os.name").toUpperCase().contains("LINUX")) {
                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454854 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

                //run cmd
                Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
                flag =  util.verifyCommandOutput(p,"Couldn't complete process : Couldn't write to file "+InstLoc_EMFULL+"/tools/ConfigChangeList.txt (Permission denied)");
            }
            else  if (System.getProperty("os.name").toUpperCase().contains("WINDOWS")) {
                LOGGER.info("This test case scenario is valid only on LINUX OS");
                LOGGER.info("Skipping test case steps for this test case on WINDOWS OS");
                flag=true;
            } 
            Assert.assertTrue(flag);
        } catch (Exception e) {
        }
    }

    /**
     * Test Case ID : 455113
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_455113_VerifyCustomConfigLocation() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+InstLoc_WV+"/config"};
        Boolean flag = false;
        try {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 455113 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            flag =  util.verifyCommandOutput(p,"Configuration Comparison completed!!");

            Assert.assertTrue(flag);
        } catch (Exception e) {
        }
    }

    /**
     * Test Case ID : 454890
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454890_VerifyWithNoModifications() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        try {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454890 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            flag =  util.verifyCommandOutput(p,"Configuration Comparison completed!!");

            Assert.assertTrue(flag);
        } catch (Exception e) {
        }
    }

    /**
     * Test Case ID : 454858
     * Test case description : Verify that console output does not have nay errors
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454858_VerifyConsoleOutput() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        try {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454858 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            flag =  util.verifyCommandOutput(p,"[ERROR]");

            Assert.assertTrue(!flag);
        } catch (Exception e) {
        }
    }

    /**
     * Test Case ID : 454862
     * Test case description : Verify that console output for debug messages
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454862_VerifyDebugConsoleOutput() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation+" -debug"};
        Boolean flag = false;
        try {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454862 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            flag =  util.verifyCommandOutput(p,"[DEBUG]");

            Assert.assertTrue(flag);
        } catch (Exception e) {
        }
    }

    /**
     * Test Case ID : 455112
     * Test case description : Verify that console output has no errors for default command for webview-only installation
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_455112_VerifyCommandWithZeroInputsWebviewOnly() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges"};
        Boolean flag = false;
        try {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 455112 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_WV+"/tools");
            flag =  util.verifyCommandOutput(p,"Configuration Comparison completed!!");

            Assert.assertTrue(flag);
        } catch (Exception e) {
        }
    }

    /**
     * Test Case ID : 455114
     * Test case description : Verify help menu
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_455114_VerifyHelpMenu() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -help "};
        Boolean flag = false;
        String line1 = "java -jar ConfigUtility.jar -help";
        String line2 = "java -jar ConfigUtility.jar -listchanges [-d <configdest>] [-f <filename>] [-v] [-debug]";
        String line3 = "java -jar ConfigUtility.jar -viewchanges [-d <configdest>] [-f <filename>] [-v] [-debug]";
        String line4 = "-listchanges          List only the name of the files whose configuration have been customized.";
        String line5 = "-viewchanges          Show the configuration customization details.";
        String line6 = "-debug                Show Debug Logs.";
        String line7 = "-help                 Show Usage.";
        String line8 = "-d                    Destination configuration directory.";
        String line9 = "-f                    Output file name.";
        String line10 = "-v                    Destination Directory APM Version.";

        List consoleText = Arrays.asList(line1,line2,line3,line4,line5,line6,line7,line8,line9,line10);
        try {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 455114 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            flag = util.verifyCommandOutput(p,consoleText);

            Assert.assertTrue(flag);
        } catch (Exception e) {
        }
    }
    /**
     * Test Case ID : 455115
     * Test case description : Verify that console output has no errors for default command for webview-only installation
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_455115_VerifyDebugConsoleOutput() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+"asfasdf"};
        Boolean flag1 = false;
        Boolean flag2 = false;
        try {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 455115 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_WV+"/tools");
            flag1 =  !util.verifyCommandOutput(p,"Exception");
            flag2 = !util.verifyCommandOutput(p,"at com.");
            Assert.assertTrue(flag1&&flag2);
        } catch (Exception e) {
        }
    }

    /**
     * Test Case ID : 455120
     * Test case description : Verify that -v option accepts upto 2 decimals (3 digits) , ex: -v 9.5.3 
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_455120_VerifyVersionOption() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -v 9.5.3"};
        Boolean flag = false;
        String line1 = "Identified Enterprise Manager version as 9.5.3";
        String line2 = "Configuration Comparison completed!!";

        List consoleText = Arrays.asList(line1,line2); 
        try {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 455120 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            flag =  util.verifyCommandOutput(p,consoleText);
            Assert.assertTrue(flag);
        } catch (Exception e) {
        }
    }

    /**
     * Test Case ID : 455121
     * Test case description : Verify that -v option doesnot accept after 3rd decimal , ex: -v 9.5.3.asdf should be invalid 
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_455121_VerifyVersionOptionInvalidCharactersAfterThirdDecimal() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -v 9.5.3.asdf"};
        Boolean flag = false;
        try {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 455121 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            flag =  util.verifyCommandOutput(p,"Not a valid version. Please provide a valid version.");

            Assert.assertTrue(flag);
        } catch (Exception e) {
        }
    }

    /**
     * Test Case ID : 455701
     * Test case description : Verify console log
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_455701_VerifyConsoleLogForFileNamesInDebug() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -debug"};
        Boolean flag = false;

        String line1 = "Comparing File:tess-custom.properties";
        String line2 = "Comparing File:Introscope_WebView.lax";
        String line3 = "Comparing File:Introscope_Enterprise_Manager.lax";

        List consoleText = Arrays.asList(line1,line2,line3);

        try {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 455701 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            flag =  util.verifyCommandOutput(p,consoleText);

            Assert.assertTrue(flag);
        } catch (Exception e) {
        }
    }


    /**
     * Test Case ID : 455117
     * Test case description : Run ConfigUtility jar for older Version EM
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_455117_runConfigUtilityForOlderEM() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+InstLoc_EMFULL_old+"/config"};
        Boolean flag = false;
        try {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 455117 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            flag =  util.verifyCommandOutput(p,"Configuration Comparison completed!!");

            Assert.assertTrue(flag);
        } catch (Exception e) {
        }
    }


    /**
     * Test Case ID : 455330
     * Test case description : Run ConfigUtility jar for older Version EM
     */

    @Test(groups = {"configUtility", "SMOKE"})

    public void verify_ALM_455330_VerifyConsoleLogForInvalidFileNames() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -debug"};
        Boolean flag = false;


        String line1 = "Comparing File:SystemDBRecordTypes.properties";
        String line2 = "Comparing File:tess-dynamicViewRefId.properties";
        String line3 = "Comparing File:em-include.conf";
        String line4 = "Comparing File:wv-include.conf";
        String line5 = "Comparing file:Cross-Enterprise_APM_CTG_Config_Template.profile";
        String line6 = "Comparing File:CTG_Tran_Trace_Template.profile";
        String line7 = "Comparing file handler.xml";
        String line8 = "Comparing file internal.xml";
        String line9 = "Comparing file login.config";
        String line10 = "Comparing file idp-metadata.xml";

        List consoleText = Arrays.asList(line1,line2,line3,line4,line5,line6,line7,line8,line9,line10);

        try {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 455330 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            flag =  util.verifyCommandOutput(p,consoleText);

            Assert.assertTrue(!flag);
        } catch (Exception e) {
        }
    }

}
