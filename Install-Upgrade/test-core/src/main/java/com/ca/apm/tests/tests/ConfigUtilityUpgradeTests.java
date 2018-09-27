package com.ca.apm.tests.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.ca.tas.builder.TasBuilder;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.tests.testbed.SampleEM;
import com.ca.apm.tests.testbed.ConfigUtilityWindowsTestbed;
import com.ca.apm.tests.utils.Utility;
import com.ca.apm.tests.testbed.ConfigUtilityLinuxTestbed;

public class ConfigUtilityUpgradeTests extends TasTestNgTest 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigUtilityUpgradeTests.class);
    private Utility  util = new Utility();

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

    /**
     * Test Case ID : 454908
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454908_explicitlySpecifyingApmVersion() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -s "+sourceConfig+" -d "+destinationConfig+" -v 10.0.0"};
        try {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454908 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");

            Boolean flag =  util.verifyCommandOutput(p,"Ignoring option -v;  -v option is not required when both -s and -d options are provided.");

            Assert.assertTrue(flag);

        } catch (Exception e) {
             
        }
    }



    /**
     * Test Case ID : 454905
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454905_runFromCustomLocation() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -s "+sourceConfig+" -d "+destinationConfig};
        try {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454905 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            //copy jar to cusotom location
            ApmbaseUtil.copy(InstLoc_EMFULL+"/tools/ConfigUtility.jar", InstLoc_EMFULL+"/ConfigUtility.jar");
            ApmbaseUtil.fileExists(InstLoc_EMFULL+"/ConfigUtility.jar");
            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL);
            //         ApmbaseUtil.deleteFile(InstLoc_EMFULL+"/ConfigUtility.jar");
            Boolean flag =  util.verifyCommandOutput(p,"Configuration Comparison completed!!");

            Assert.assertTrue(flag);
        } catch (Exception e) {
             
        }
    }


    /**
     * Test Case ID : 454899
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454899_SourceAndDestinationConfigInstancesAreOfDifferentApmReleases() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -s "+sourceConfig+" -d "+destinationConfig};
        try {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454899 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");

            Boolean flag =  util.verifyCommandOutput(p,"Configuration Comparison completed!!");

            Assert.assertTrue(flag);
        } catch (Exception e) {
             
        }
    }



    /**
     * Test Case ID : 454898
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454898_SourceAndDestinationConfigInstancesAreSame() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -s "+sourceConfig+" -d "+sourceConfig};
        try {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454898 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");

            Boolean flag =  util.verifyCommandOutput(p," -s and -d options cannot point to the same directory.");

            Assert.assertTrue(flag);


        } catch (Exception e) {
             
        }
    }


    /**
     * Test Case ID : 454901
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454901_InvalidSourceConfigFolder() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -s "+sourceConfig+"/invalid"+" -d "+destinationConfig};
        try {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454901 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");

            Boolean flag =  util.verifyCommandOutput(p,"-s option value is not a valid directory. Please provide a valid directory.");

            Assert.assertTrue(flag);

        } catch (Exception e) {
             
        }
    }

    /**
     * Test Case ID : 454903
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454903_NoConfigDdifferencesInSourceAndDestinationFolder() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -s "+sourceConfig+" -d "+destinationConfig};
        try {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454903 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            //rename dir for em_old with em_full
            ApmbaseUtil.copyDirectory(new File(destinationConfig), new File (InstLoc_EMFULL_old+"/config_backup"));
            ApmbaseUtil.copyDirectory(new File(InstLoc_EMFULL_old+"/bin"), new File (InstLoc_EMFULL_old+"/bin_backup"));
            //delete em_old dirs
            ApmbaseUtil.deleteDir(new File(InstLoc_EMFULL_old+"/config"));
            ApmbaseUtil.deleteDir(new File(InstLoc_EMFULL_old+"/bin"));

            //copy dir to em_old
            ApmbaseUtil.copyDirectory(new File(sourceConfig), new File(destinationConfig));
            ApmbaseUtil.copyDirectory(new File(InstLoc_EMFULL+"/bin"), new File(InstLoc_EMFULL_old+"/bin"));

            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");

            //delete dir in em_old
            ApmbaseUtil.deleteDir(new File(destinationConfig));
            ApmbaseUtil.deleteDir(new File(InstLoc_EMFULL+"/bin"));

            //rename back dir 
            ApmbaseUtil.copyDirectory(new File(InstLoc_EMFULL_old+"/config_backup"), new File(destinationConfig));
            ApmbaseUtil.deleteDir(new File(InstLoc_EMFULL_old+"/config_backup"));
            ApmbaseUtil.copyDirectory(new File(InstLoc_EMFULL_old+"/bin_backup"), new File(InstLoc_EMFULL_old+"/bin"));
            ApmbaseUtil.deleteDir(new File(InstLoc_EMFULL_old+"/bin_backup"));

            Boolean flag =  util.verifyCommandOutput(p,"No Changes found!!");

            Assert.assertTrue(flag);

        } catch (Exception e) {
             
        }
    }


    /**
     * Test Case ID : 454902
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454902_SameEmInstanceForSourceAndDestinationFolder() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -s "+sourceConfig+" -d "+sourceConfig};
        try {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454902 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");

            Boolean flag =  util.verifyCommandOutput(p,"-s and -d options cannot point to the same directory");

            Assert.assertTrue(flag);

        } catch (Exception e) {
             
        }
    }


    /**
     * Test Case ID : 454904
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454904_DestinationFolderNotSpecified() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -s "+sourceConfig};
        try {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454904 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");

            Boolean flag =  util.verifyCommandOutput(p," Please provide destination directory(-d).");

            Assert.assertTrue(flag);

        } catch (Exception e) {
             
        }
    }


    /**
     * Test Case ID : 454907
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454907_SourceFolderNotSpecified() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+destinationConfig};
        try {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454907 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL +"/tools");

            //   Boolean flag1 =  util.verifyCommandOutput(p,"Source folder is not specified!!");
            Boolean flag =  util.verifyCommandOutput(p,"Identified Enterprise Manager version as");
            Assert.assertTrue(flag);

        } catch (Exception e) {
             
        }
    }


    /**
     * Test Case ID : 454888
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454888_CompareTwoEmConfigInstances() {
        String[] command1 = {"java -jar ConfigUtility.jar -viewchanges -s"+sourceConfig+" -d "+destinationConfig};
        String[] command2 = {"java -jar ConfigUtility.jar -listchanges -s"+sourceConfig+" -d "+destinationConfig};
        try {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454888 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            //run cmd
            Process p1 = ApmbaseUtil.getProcess(command1, InstLoc_EMFULL +"/tools");
            Process p2 = ApmbaseUtil.getProcess(command2, InstLoc_EMFULL +"/tools");

            //   Boolean flag1 =  util.verifyCommandOutput(p,"Source folder is not specified!!");
            Boolean flag1 = (util.verifyCommandOutput(p1,"Report Generated") && ApmbaseUtil.fileExists(InstLoc_EMFULL+"/tools/ConfigChanges.txt"));
            Boolean flag2 = (util.verifyCommandOutput(p2,"Report Generated") && ApmbaseUtil.fileExists(InstLoc_EMFULL+"/tools/ConfigChangeList.txt"));

            Assert.assertTrue(flag1&&flag2);

        } catch (Exception e) {
             
        }
    }


    /////////////////////////////Validate reports testcases///////////////////////////////

    /**
     * Test Case ID : 454895
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454895_VerifyReportFileLocation() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -s "+sourceConfig+" -d "+destinationConfig};
        try {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454895 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL +"/tools");
            Boolean flag1 =  util.verifyCommandOutput(p,"Report Generated");
            Boolean flag2 =  ApmbaseUtil.fileExists(InstLoc_EMFULL+"/tools/ConfigChanges.txt");

            Assert.assertTrue(flag1&&flag2);
        } catch (Exception e) {
             
        }
    }

    /**
     * Test Case ID : 454849
     * Test case description
     */

    @Test(groups = {"configUtility", "SMOKE"})

    public void verify_ALM_454849_VerifyReportForModificationInConfFiles() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -s "+sourceConfig+" -d "+destinationConfig};
        try {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454849 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            if (System.getProperty("os.name").toUpperCase().contains("WINDOWS")) {

                //create backup
                ApmbaseUtil.fileBackUp(InstLoc_EMFULL_old+"/bin/EMservice.conf");
                //edit file
                ApmbaseUtil.appendProperties(Arrays.asList("newProperty=newValue"), "EMservice.conf", InstLoc_EMFULL_old+"/bin");
                //run cmd
                Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
                //restore file
                ApmbaseUtil.revertFile(InstLoc_EMFULL_old+"/bin/EMservice.conf");
                
                Boolean flag =  util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", "EMservice.conf", "newProperty", "", "newValue");
                Assert.assertTrue(flag);
            }

            else if (System.getProperty("os.name").toUpperCase().contains("LINUX")) {
                LOGGER.info("This test case scenario is valid only on Windows OS");
                LOGGER.info("Skipping test case steps for this test case on LINUX OS");
            }
        } catch (Exception e) {
             
        }
    }


    /**
     * Test Case ID : 454847
     * Test case description
     */

    @Test(groups = {"configUtility", "SMOKE"})

    public void verify_ALM_454847_VerifyReportForModificationInConfigFiles() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -s "+sourceConfig+" -d "+destinationConfig};
        try {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454847 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            //create backup
            ApmbaseUtil.fileBackUp(InstLoc_EMFULL_old+"/config/eiam.config");
            //edit file
            ApmbaseUtil.appendProperties(Arrays.asList("newProperty=newValue"),"eiam.config", InstLoc_EMFULL_old+"/config");
            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            //restore file
            ApmbaseUtil.revertFile(InstLoc_EMFULL_old+"/config/eiam.config");

            Boolean flag =  util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", "eiam.config", "newProperty", "", "newValue");
            Assert.assertTrue(flag);

        } catch (Exception e) {
             
        }
    }

    /**
     * Test Case ID : 454848
     * Test case fails for 99.99.x branch EMs
     */

    @Test(groups = {"configUtility", "SMOKE"})

    public void verify_ALM_454848_VerifyReportForModificationInLaxFiles() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -s "+sourceConfig+" -d "+destinationConfig};
        try {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454848 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            //create backup
            ApmbaseUtil.fileBackUp(InstLoc_EMFULL_old+"/Introscope_Enterprise_Manager.lax");
            //edit file
            ApmbaseUtil.appendProperties(Arrays.asList("newProperty=newValue"),"Introscope_Enterprise_Manager.lax", InstLoc_EMFULL_old);
            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            //restore file
            ApmbaseUtil.revertFile(InstLoc_EMFULL_old+"Introscope_Enterprise_Manager.lax");

            Boolean flag =  util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", "Introscope_Enterprise_Manager.lax", "newProperty", "", "newValue");
            Assert.assertTrue(flag);
        } catch (Exception e) {
             
        }
    }


    /**
     * Test Case ID : 454774
     * Test case description
     */

    @Test(groups = {"configUtility", "SMOKE"})

    public void verify_ALM_454774_VerifyReportForModificationInPropertiesFiles() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -s "+sourceConfig+" -d "+destinationConfig   };
        try {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454774 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            //create backup
            System.out.println("creating backup");
            ApmbaseUtil.fileBackUp(InstLoc_EMFULL_old+"/config/log4j.properties");
            //edit file
            LOGGER.info("editing file");
            ApmbaseUtil.appendProperties(Arrays.asList("newProperty=newValue"),"log4j.properties", InstLoc_EMFULL_old+"/config");
            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            //restore file
            LOGGER.info("restoring file");
            ApmbaseUtil.revertFile(InstLoc_EMFULL_old+"/config/log4j.properties");

            Boolean flag =  util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", "log4j.properties", "newProperty", "", "newValue");
            Assert.assertTrue(flag);

        } catch (Exception e) {
             
        }
    }

    /**
     * Test Case ID : 454900
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454900_VerifyReportFileFormat() {
        String[] command = {"java -jar ConfigUtility.jar -listchanges -s "+sourceConfig+" -d "+destinationConfig};
        try {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454900 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            Boolean flag1 =  ApmbaseUtil.fileExists(InstLoc_EMFULL+"/tools/ConfigChanges.txt");
            Boolean flag2 = ApmbaseUtil.checkMessages(Arrays.asList(""), new File(InstLoc_EMFULL+"/tools/ConfigChanges.txt"))==0;
            Assert.assertTrue(flag1&&flag2);

        } catch (Exception e) {
             
        }
    }

    /////////////////////////////Validate types of modifications testcases///////////////////////////////

    /**
     * Test Case ID : 454866
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454866_PropertyAddedInDestinationFiles() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -s "+sourceConfig+" -d "+destinationConfig};
        try {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454866 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            //create backup
            ApmbaseUtil.fileBackUp(InstLoc_EMFULL_old+"/config/log4j.properties");
            //edit file
            ApmbaseUtil.appendProperties(Arrays.asList("newProperty=newValue"), "log4j.properties", InstLoc_EMFULL_old+"/config");
            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            //restore file
            ApmbaseUtil.revertFile(InstLoc_EMFULL_old+"/config/log4j.properties");


            Boolean flag1 =  util.verifyCommandOutput(p,"Report Generated"); 
            Boolean flag2 = util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", "log4j.properties", "newProperty", null, "newValue");
            Assert.assertTrue(flag1&&flag2);

        } catch (Exception e) {
             
        }
    }
    /**
     * Test Case ID : 454802
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454802_PropertyAddedInSourceFiles() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -s "+sourceConfig+" -d "+destinationConfig};
        try {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454802 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            //create backup
            ApmbaseUtil.fileBackUp(InstLoc_EMFULL+"/config/log4j.properties");
            //edit file
            ApmbaseUtil.appendProperties(Arrays.asList("newProperty=newValue"), "log4j.properties", InstLoc_EMFULL+"/config");
            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            //restore file
            ApmbaseUtil.rollFile(new File(InstLoc_EMFULL+"/config/log4j.properties"));

            Boolean flag1 =  util.verifyCommandOutput(p,"Report Generated"); 
            Boolean flag2 = util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", "log4j.properties", "newProperty",  "newValue",null);
            Assert.assertTrue(flag1&&flag2);

        } catch (Exception e) {
             
        }
    }

    /**
     * Test Case ID : 454861
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454861_PropertyDeletedInDestinationFiles() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -s "+sourceConfig+" -d "+destinationConfig};
        try {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454861 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            //create backup
            ApmbaseUtil.fileBackUp(InstLoc_EMFULL_old+"/config/log4j.properties");
            //edit file
            ApmbaseUtil.removeProperties("log4j.properties", InstLoc_EMFULL_old+"/config", Arrays.asList("log4j.rootCategory=INFO, stdout"));
            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL);
            //restore file
            ApmbaseUtil.rollFile(new File(InstLoc_EMFULL_old+"/config/log4j.properties"));


            Boolean flag1 =  util.verifyCommandOutput(p,"Report Generated"); 
            Boolean flag2 = util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", "log4j.properties", "newProperty",  "newValue",null);
            Assert.assertTrue(flag1&&flag2);

        } catch (Exception e) {
             
        }
    }

    /**
     * Test Case ID : 454860
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454860_PropertyDeletedInSourceFiles() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -s "+sourceConfig+" -d "+destinationConfig};
        try {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454860 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            //create backup
            ApmbaseUtil.fileBackUp(InstLoc_EMFULL+"/config/log4j.properties");
            //edit file
            ApmbaseUtil.removeProperties("log4j.properties", InstLoc_EMFULL+"/config", Arrays.asList("log4j.rootCategory=INFO, stdout"));
            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL);
            //restore file
            ApmbaseUtil.revertFile(InstLoc_EMFULL+"/config/log4j.properties");

            Boolean flag1 =  util.verifyCommandOutput(p,"Report Generated"); 
            Boolean flag2 = util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", "log4j.properties", "newProperty", null, "newValue");
            Assert.assertTrue(flag1&&flag2);

        } catch (Exception e) {
             
        }
    }

    /**
     * Test Case ID : 454865
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454865_PropertyModifiedInSourceAndDestinationFiles() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -s "+sourceConfig+" -d "+destinationConfig};
        try {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454865 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            //create backup
            ApmbaseUtil.fileBackUp(InstLoc_EMFULL+"/config/log4j.properties");
            ApmbaseUtil.fileBackUp(InstLoc_EMFULL_old+"/config/log4j.properties");
            //edit files
            ApmbaseUtil.setproperties("log4j.properties", InstLoc_EMFULL+"/config", "log4j.rootCategory","DEBUG, stdout");
            ApmbaseUtil.setproperties("log4j.properties", InstLoc_EMFULL_old+"/config", "log4j.rootCategory","VERBOSE, stdout");
            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL);
            //restore files
            ApmbaseUtil.revertFile(InstLoc_EMFULL+"/config/log4j.properties");
            ApmbaseUtil.revertFile(InstLoc_EMFULL_old+"/config/log4j.properties");

            Boolean flag1 =  util.verifyCommandOutput(p,"Report Generated"); 
            Boolean flag2 = util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", "log4j.properties", "log4j.rootCategory",  "DEBUG, stdout","VERBOSE, stdout");
            Assert.assertTrue(flag1&&flag2);
        } catch (Exception e) {
             
        }
    }

    /**
     * Test Case ID : 454863
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454863_PropertyModifiedInSourceFiles() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -s "+sourceConfig+" -d "+destinationConfig};
        try {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454863 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            //create backup
            ApmbaseUtil.fileBackUp(InstLoc_EMFULL+"/config/log4j.properties");
            //edit files
            ApmbaseUtil.setproperties("log4j.properties", InstLoc_EMFULL+"/config", "log4j.rootCategory","DEBUG, stdout");
            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL);
            //restore files
            ApmbaseUtil.revertFile(InstLoc_EMFULL+"/config/log4j.properties");

            Boolean flag1 =  util.verifyCommandOutput(p,"Report Generated"); 
            Boolean flag2 = util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", "log4j.properties", "log4j.rootCategory",  "DEBUG, stdout","INFO, stdout");
            Assert.assertTrue(flag1&&flag2);

        } catch (Exception e) {
             
        }
    }

    /**
     * Test Case ID : 454864
     * Test case description
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_454864_PropertyModifiedInDestinationFiles() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -s "+sourceConfig+" -d "+destinationConfig};
        try {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454864 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            //create backup
            ApmbaseUtil.fileBackUp(InstLoc_EMFULL_old+"/config/log4j.properties");
            //edit files
            ApmbaseUtil.setproperties("log4j.properties", InstLoc_EMFULL_old+"/config", "log4j.rootCategory","VERBOSE, stdout");
            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL);
            //restore files
            ApmbaseUtil.revertFile(InstLoc_EMFULL_old+"/config/log4j.properties");


            Boolean flag1 =  util.verifyCommandOutput(p,"Report Generated"); 
            Boolean flag2 = util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", "log4j.properties", "log4j.rootCategory",  "INFO, stdout","DEBUG, stdout");
            Assert.assertTrue(flag1&&flag2);

        } catch (Exception e) {
             
        }
    }

    
    /**
     * Test Case ID : 455349
     * Test case description : Run ConfigUtility jar for older Version EM
     */

    @Test(groups = {"configUtility", "BAT"})

    public void verify_ALM_455349_runConfigUtilityListChangesForDefaultValues() {
        String[] command = {"java -jar ConfigUtility.jar -listchanges -s "+sourceConfig+" -d "+destinationConfig};
        Boolean flag = false;
        try {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 455349 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            flag =  util.verifyCommandOutput(p,"Config Comparison completed!!");
            
            Assert.assertTrue(flag);
        } catch (Exception e) {
        }
    }
    
   
    
    /**
     * Test Case ID : 454856
     * Test case description : Run ConfigUtility jar for older Version EM
     */

    @Test(groups = {"configUtility", "SMOKE"})

    public void verify_ALM_454856_runConfigUtilityListchangesForBackedOutChanges() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+InstLoc_EMFULL_old+"/config"};
        Boolean flag = false;
        String actualValue = "";
        String modifiedValue = "DEBUG, stdout";
        try {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454856 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            ApmbaseUtil.fileBackUp(InstLoc_EMFULL+"/config/log4j.properties");
            //edit files
            actualValue =  ApmbaseUtil.getPropertyValue("log4j.rootCategory", "log4j.properties", InstLoc_EMFULL+"/config");
            ApmbaseUtil.setproperties("log4j.properties", InstLoc_EMFULL+"/config", "log4j.rootCategory",modifiedValue);
            ApmbaseUtil.setproperties("log4j.properties", InstLoc_EMFULL+"/config", "log4j.rootCategory",actualValue);
            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL);
            //restore files
            ApmbaseUtil.revertFile(InstLoc_EMFULL+"/config/log4j.properties");

            Boolean flag1 =  util.verifyCommandOutput(p,"Report Generated"); 
            Boolean flag2 = util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", "log4j.properties", "log4j.rootCategory", actualValue,modifiedValue);

            
          

            Assert.assertTrue(flag1&&!flag2);
        } catch (Exception e) {
        }
    }
   
  
   
}