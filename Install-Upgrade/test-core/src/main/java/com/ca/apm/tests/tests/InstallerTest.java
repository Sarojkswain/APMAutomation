package com.ca.apm.tests.tests;

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.util.DirectoryScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.commandline.Execution;
import com.ca.apm.automation.action.utils.Utils;
import com.ca.apm.automation.utils.configuration.ConfigurationFileFactory;
import com.ca.apm.tests.testbed.InstallerTestbed;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

// this is a sample test written to check the newly written installer flow and role

public class InstallerTest extends TasTestNgTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(InstallerTest.class);
    private static final String DEFAULT_RESPONSE_FILE = "SampleResponseFile.Introscope.txt";
    File responseFile=new File("C:\\automation\\deployed\\installers\\em","installer.properties");
    File targetDir=new File("C:\\automation\\deployed\\installers\\em");
    private static Map<String, String> installerProperties = new HashMap<>();
    
    DirectoryScanner ds = new DirectoryScanner();
    
    
    protected final ConfigurationFileFactory configFileFactory=new ConfigurationFileFactory();
    
   
    
    private void createSampleResponseFile() throws Exception
    {
        
        
        if(targetDir.exists())
        {
            responseFile.createNewFile();
        }
        
        FileUtils.copyInputStreamToFile(getClass().getResourceAsStream(DEFAULT_RESPONSE_FILE), responseFile);
        setProperty();
        configFileFactory.create(responseFile).properties(installerProperties);
    }
    private void setProperty()
    {
        installerProperties.put("ca-eulaFile", "ca-eula.silent.txt");
        installerProperties.put("USER_INSTALL_DIR","C:\\automation\\deployed\\em");
        installerProperties.put("silentInstallChosenFeatures","");
        installerProperties.put("dbHost","localhost");
        installerProperties.put("dbPort","5432");
        installerProperties.put("dbName","cemdb");
        installerProperties.put("dbUser","admin");
        installerProperties.put("dbPassword","quality");
        installerProperties.put("databaseDir","C:\\automation\\deployed\\database");
        installerProperties.put("dbAdminUser","postgres");
        installerProperties.put("dbAdminPassword","Lister@123");
    }
    private void acceptCaEula() throws Exception
    {
        String encoding = System.getProperty("file.encoding");
        File caEulaFile=new File(targetDir,"ca-eula.silent.txt");
        FileUtils.write(caEulaFile,
            FileUtils.readFileToString(caEulaFile, encoding).replaceAll("CA-EULA=reject", "CA-EULA=accept"), encoding);

    }
    protected File getInstallerFile() {
        
        System.out.println("Scanning {} for introscope-installer");
        
        ds.setBasedir(targetDir);
        ds.setIncludes(new String[]{"**\\introscope-installer*"});
        ds.scan();
        if (ds.getIncludedFiles().length == 0) {
            throw new IllegalStateException("Could not find EM installer executable");
        }

        return new File(targetDir, ds.getIncludedFiles()[0]);
    }
    
    @Tas(testBeds = @TestBed(name = InstallerTestbed.class, executeOn = InstallerTestbed.EM_MACHINE_ID), size = SizeType.SMALL, owner = "pmfkey")
    @Test(groups = { "installer" })
    public void myTest() throws Exception {
        
        createSampleResponseFile();
        File execFile=getInstallerFile();
        String[] args = {"-f", new File(targetDir, "installer.properties").getPath()};
        int retVal=new Execution.Builder(execFile.getPath(),LOGGER).args(args).workDir(targetDir).build().go();
        if(retVal!=1)
        {
            LOGGER.info("testcase passed");
            ds.setBasedir(targetDir);
            ds.setIncludes(new String[]{"**\\silent.install.failed.txt"});
            ds.scan();
            if(ds.getIncludedFiles().length ==0)
            {
                assertTrue(false);
            }
            File failedText=new File(targetDir,ds.getIncludedFiles()[0]);
            Collection<String> fileContent=new ArrayList<>();
            fileContent.add("Invalid CA EULA Specified");
            fileContent.add("----------------------");
            fileContent.add("C:\\automation\\deployed\\installers\\em\\ca-eula.silent.txt does not indicate acceptance of the CA End User License Agreement.");
            fileContent.add("Edit the responsefile so that the ca-eulaFile property refers to a valid CA End User License Agreement file whose terms have been accepted, then rerun the silent installer.");
            Scanner sc=new Scanner(failedText);
            List<String> list=new ArrayList<>();
            while(sc.hasNextLine())
            {
                list.add(sc.nextLine());
            }
            
            assertTrue(list.containsAll(fileContent));
            System.out.println("testcase execution is done with status of passed");
            sc.close();
            
            assertTrue(failedText.renameTo(new File(targetDir,"myTestEulaNotAccepted.txt")));   
            System.out.println("file renamed");
            
        }
    }
    @Tas(testBeds = @TestBed(name = InstallerTestbed.class, executeOn = InstallerTestbed.EM_MACHINE_ID), size = SizeType.SMALL, owner = "pmfkey")
    @Test(groups = { "installer" })
    public void mySecondTest() throws Exception {
        createSampleResponseFile();
        acceptCaEula();
        installerProperties.put("USER_INSTALL_DIR","");
        configFileFactory.create(responseFile).properties(installerProperties);
        File execFile=getInstallerFile();
        String[] args = {"-f", new File(targetDir, "installer.properties").getPath()};
        int retVal=new Execution.Builder(execFile.getPath(),LOGGER).args(args).workDir(targetDir).build().go();
        if(retVal!=1)
        {
            LOGGER.info("testcase passed");
            ds.setBasedir(targetDir);
            ds.setIncludes(new String[]{"**\\silent.install.failed.txt"});
            ds.scan();
            if(ds.getIncludedFiles().length ==0)
            {
                assertTrue(false);
            }
            File failedText=new File(targetDir,ds.getIncludedFiles()[0]);
            Collection<String> fileContent=new ArrayList<>();
            fileContent.add("Invalid CA EULA Specified");
            Scanner sc=new Scanner(failedText);
            List<String> list=new ArrayList<>();
            while(sc.hasNextLine())
            {
                list.add(sc.nextLine());
            }
            //assertTrue(list.containsAll(fileContent));
            System.out.println("testcase execution is done with status of passed");
            sc.close();
            
            assertTrue(failedText.renameTo(new File(targetDir,"mySecondTestUserDirNotSpecified.txt")));   
            System.out.println("file renamed");
            
        }
    }
    @Tas(testBeds = @TestBed(name = InstallerTestbed.class, executeOn = InstallerTestbed.EM_MACHINE_ID), size = SizeType.SMALL, owner = "pmfkey")
    @Test(groups = { "installer" })
    public void myThirdTest() throws Exception {
        File execFile=getInstallerFile();
        String[] args = {"-f", new File(targetDir, "installer.properties").getPath()};
        int retVal=new Execution.Builder(execFile.getPath(),LOGGER).args(args).workDir(targetDir).build().go();
        if(retVal!=1)
        {
            ds.setBasedir(targetDir);
            ds.setIncludes(new String[]{"**\\silent.install.failed.txt"});
            ds.scan();
            if(ds.getIncludedFiles().length ==0)
            {
                assertTrue(false);
            }
            File failedText=new File(targetDir,ds.getIncludedFiles()[0]);
            Collection<String> fileContent=new ArrayList<>();
            fileContent.add("The selected folder is not eligible to be upgraded, because it contains an Introscope version equal to or newer than this installer's version. ");
            Scanner sc=new Scanner(failedText);
            List<String> list=new ArrayList<>();
            while(sc.hasNextLine())
            {
                list.add(sc.nextLine());
            }
            
            assertTrue(list.containsAll(fileContent));
            System.out.println("testcase execution is done with status of passed");
            sc.close();
            
            assertTrue(failedText.renameTo(new File(targetDir,"myThirdTest.txt")));   
            System.out.println("file renamed");
        }
    }
    @Tas(testBeds = @TestBed(name = InstallerTestbed.class, executeOn = InstallerTestbed.EM_MACHINE_ID), size = SizeType.SMALL, owner = "pmfkey")
    @Test(groups = { "installer" })
    public void uninstallEmTest() throws Exception {
       File execFile=new File("C:\\Program Files\\CA APM\\Introscope99.99.0.sys\\UninstallerData\\base","Uninstall_Introscope.exe");
       int retVal=new Execution.Builder(execFile.getPath(),LOGGER).workDir(new File("C:\\Program Files\\CA APM\\Introscope99.99.0.sys\\UninstallerData\\base")).build().go();
       if(retVal!=0)
       {
           LOGGER.info("uninstallation failed");
       }
        
    }
    @Tas(testBeds = @TestBed(name = InstallerTestbed.class, executeOn = InstallerTestbed.EM_MACHINE_ID), size = SizeType.SMALL, owner = "pmfkey")
    @Test(groups = { "installer" })
    public void copyEmLog() throws Exception {
    
        File resultDir = new File(System.getProperty("java.io.tmpdir"), "results");  //results directory initialized 
       File resultTaskDir = new File(resultDir, envProperties.getTestbedPropertyById("taskId"));
       String[] args = {"/C","COPY","/Y","EMService.log", resultTaskDir.getPath()+"\\test-output\\logs" };
       
       int retVal=Utils.exec("C:\\Program Files\\CA APM\\Introscope99.99.0.sys\\logs", "CMD", args, LOGGER);
       if(retVal!=0)
       {
           LOGGER.info("command execution failed");
       }
        
    }
    

}
