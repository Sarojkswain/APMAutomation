package com.ca.apm.saas.test.utils;

import static java.lang.String.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;

import com.ca.tas.builder.TasBuilder;

public class SaaSUtils {
	
    public static final String LOGIN_ERROR = "Instance home page didn't load correctly";
    private static final Logger logger = LoggerFactory.getLogger(SaaSUtils.class);
    public static final String SCREENSHOTS_FOLDER_NAME = "screenshots";
    
    public static final Map<String, String> LOGIN_MAP = new HashMap<>();
    static {
        final Map<String, String> m = LOGIN_MAP;
        m.put("production_email", "Jyotsna.Akula@automation.com"); 
        m.put("production_password", "interOP@1234"); 
        m.put("production_tenant", "Jyotsna.Akula@automation.com"); 
        m.put("production_loginURL", "https://cloud.ca.com/sppclient/#/login");
        m.put("staging_email", "automation@staging.com"); 
        m.put("staging_password", "interOP@1234"); 
        m.put("staging_tenant", "automation@staging.com"); 
        m.put("staging_loginURL", "https://adminui-route-8080-axa-ng.app.unvnp1.cs.saas.ca.com/sppclient/#/login");
        m.put("staging_registrationURL", "http://nansw02-u187776.ca.com:8081/signup-axang/#/register");
        m.put("onprem_user", "Admin"); 
        m.put("onprem_password", ""); 
        m.put("onprem_loginURL", "http://{host}:8082/ApmServer/");
        m.put("dev_loginURL", "https://adminui-route-edge-axa.app.unvdev1.cs.saas.ca.com/sppclient/#/login");
        m.put("dev_registrationURL", "http://nansw02-u187776:8081/signup-dev/#/register");
        m.put("staging_testngEmailRecipients", "Marina.Kur@ca.com,Team-DXI-SaaSHosting-Alerts@ca.com,Anand.Krishnamurthy@ca.com,Martin.Janda@ca.com,Balamurugan.Kannan@ca.com,RaviKanth.Bandari@ca.com,Aleem.Ahmad@ca.com,JayAndrew.Key@ca.com");
       // m.put("staging_testngEmailRecipients", "Marina.Kur@ca.com");
        m.put("production_testngEmailRecipients", "Marina.Kur@ca.com,Team-DXI-SaaSHosting-Alerts@ca.com,Anand.Krishnamurthy@ca.com,Martin.Janda@ca.com,Balamurugan.Kannan@ca.com,RaviKanth.Bandari@ca.com,Aleem.Ahmad@ca.com,JayAndrew.Key@ca.com");
        m.put("onprem_testngEmailRecipients", "Marina.Kur@ca.com,Anand.Krishnamurthy@ca.com,Martin.Janda@ca.com,Swetha.Bhamidipati@ca.com,Abhijit.Bhadra@ca.com,SarojK.Swain@ca.com");
    }
    
    /**
     * Get OS type
     * 
     * @return os type
     */
    public static String getOSType() {
        
        String osName = System.getProperty("os.name");
        String osType;
        if (osName.toLowerCase().contains("windows")) {
            osType = "Windows";
        } else if (osName.toLowerCase().contains("unix")
                || osName.toLowerCase().contains("linux")
                || osName.toLowerCase().startsWith("mac os")) {
            osType = "Unix";
        } else {
            osType = "Unsupported";
        }
        
        logger.info("Returning os type '" + osType + "'");
        return osType;
    }
	
	/**
	 * Get full deployment path
	 * 
	 * @param destFolder destination sub directory
	 * @return
	 */
	public static String getDeployPath(String destFolder) {
	    
	    String deployPath = TasBuilder.WIN_SOFTWARE_LOC + destFolder;
        
        if(getOSType().contains("Unix")) {
            deployPath = TasBuilder.LINUX_SOFTWARE_LOC + destFolder;
        } 
        
        logger.info("Deployment folder is " + deployPath);
        return deployPath;
	}
		
	/**
	 * Parse logs
	 * 
	 * @param dirPath
	 * @param fileNamePattern
	 * @param searchPhrase
	 * @return
	 * @throws InterruptedException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static HashMap<Integer, String> parseLog(String dirPath,
			String fileNamePattern, String searchPhrase)
			throws InterruptedException, FileNotFoundException, IOException {

		ArrayList<String> logFiles = new ArrayList<String>();
		HashMap<Integer, String> linesMatched = new HashMap<Integer, String>();

		// look for all agent logs in the folder dirPath matching
		// fileNamePattern
		logFiles = getFilesMatchingName(dirPath, fileNamePattern);

		int lineCount = 0;

		for (String sLogs : logFiles) {
			BufferedReader br;
			br = new BufferedReader(new FileReader(new File(sLogs)));
			logger.info("Parsing log [" + sLogs + "] for "
					+ searchPhrase);
			for (String line = br.readLine(); line != null; line = br
					.readLine()) {
				lineCount++;
				
				/*line = line.toLowerCase();
				searchPhrase = searchPhrase.toLowerCase();*/
				
				if (line.contains(searchPhrase)) {
					linesMatched.put(lineCount, ("[" + sLogs + "]" + line));

					logger.info(lineCount + "-[" + sLogs + "]" + line);
				}
			}
			br.close();
		}

		return linesMatched;
	}

	/**
	 * Identify each log (autoprobe and agent logs) in all the agent bundles,
	 * logs folders and search the log file if its name contains provided pattern.
     * 
	 * @param sLogsDirectory log directory
	 * @param fileNamePattern pattern
	 * @return
	 */
	public static ArrayList<String> getFilesMatchingName(String sLogsDirectory,
			String fileNamePattern) {
		
		File fLogsDir = new File(sLogsDirectory);
		File[] logFiles = fLogsDir.listFiles();
		ArrayList<String> agentLogFiles = new ArrayList<String>();

		for (File f : logFiles) {
			// If log file name present in logs directory contains the file name
			// pattern match, search for lines which contain searchPhrase in
			// that particular log.
			String sFileName = f.getName();
			String sFilePath = f.getAbsolutePath();
			
			sFileName = sFileName.toLowerCase();
			fileNamePattern = fileNamePattern.toLowerCase();
			
			if (sFileName.contains(fileNamePattern)) {
				logger.info("Log file path  " + sFilePath);
				agentLogFiles.add(sFilePath);
			}
		}
		return agentLogFiles;
	}
	
	/**
	 * Log all UI elements; use for debugging only (takes long)
	 * @param driver
	 */
	public static void logAllUIElements(WebDriver driver) {
        
	    logger.info("*********page elements **********");
	    
        List<WebElement> elements = driver.findElements(By.xpath("//*"));
        for (WebElement element: elements) {
            logger.info("class: " + element.getClass() + "; tag: " + element.getTagName() + 
                "; text: " + element.getText() + "; id: " + element.getAttribute("id"));
        }    
        
        logger.info("*********page elements **********");
    }
	
    /** 
     * Get test property value.
     * If system property not provided, it reads data from xml suite; 
     * If none provided, it will use default value.
     * 
     * @param key property key
     * @param context test context
     * @param defvalue default value
     * @return value for the provided key name
     */
    public static String getPropertyValue(String key, ITestContext context, String defvalue) {

        String value = System.getProperty(key, context.getCurrentXmlTest().getParameter(key));
        if (value == null) {
            logger.info("Using default for: " + key + "=" + defvalue);
            value = defvalue;
        }

        logger.info("Value for property '" + key + "' is " + value);
        return value;
    }
    
    /**
     * Fill chrome options with any options that are necessary for test class.
     * 
     * @param opt
     */
    public static void fillChromeOptions(ChromeOptions opt) {
        // needed for performance tests
        opt.addArguments("--enable-precise-memory-info");
        opt.addArguments("--disable-extensions");
        // Added to disable chrome's credentials management option (a pop-up by the URL bar)
        Map<String, Object> prefs = new HashMap<String, Object>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        opt.setExperimentalOption("prefs", prefs);
    }
    
    /**
     * Takes a screenshot of the current browser page we see once the test step
     * is done.
     * 
     * @param driver selenium driver
     * @param stepName the name of test step executed.
     * @param className test class name
     * @param methodName test method name
     */
    public static void takeScreenshot(RemoteWebDriver driver, 
                                      String stepName,
                                      String className,
                                      String methodName) {
        
        logger.info("Taking a screenshot...");
        try {
            File screenshotFile = driver.getScreenshotAs(OutputType.FILE);
            String screenshotExtension =
                FilenameUtils.getExtension(screenshotFile.getAbsolutePath());
            File targetScreenshotFile =
                FileUtils.getFile(SCREENSHOTS_FOLDER_NAME, className,
                    methodName + "-" + stepName + "." + screenshotExtension);
            targetScreenshotFile.getParentFile().mkdirs();
            FileUtils.copyFile(screenshotFile, targetScreenshotFile);
            logger.info(format("Created a screenshots for test %s: %s", methodName,
                targetScreenshotFile.getAbsolutePath()));
        } catch (Exception e) {
            logger.warn(
                format("Unable to take a screenshots for test %s!", methodName), e);
        }
    }
    
    public static void updateProperties(String fileName, HashMap<String,String> props) throws Exception {

        logger.info("Updating properties {}...", fileName);
        
        File backup = new File(fileName + ".backup");
        if(!backup.exists()) {
            FileUtils.copyFile(new File(fileName), backup);
        }
        
        PropertiesConfiguration conf = new PropertiesConfiguration(fileName);
        for (Map.Entry<String, String> property : props.entrySet()) {
            conf.setProperty(property.getKey(), property.getValue());
        }
       
        conf.save();  
    }
    
    public static String getProperty(String fileName, String key) throws Exception {

        logger.info("Reading property {} from {}...", key, fileName);
       
        PropertiesConfiguration conf = new PropertiesConfiguration(fileName);
        return conf.getString(key);
    }
    
    public static void revertBackupProperties(String fileName, String testName) throws Exception {
        
        logger.info("Restoring {} to original properties...", fileName);
        
        File profile = new File(fileName);
        File backup = new File(fileName + ".backup");
        
        if(backup.exists()) {
            FileUtils.copyFile(profile, new File(profile + "." + testName));
            FileUtils.copyFile(backup, profile);
        }
    }
	
    public static boolean containsIgnoreCase(String mainString, String subString){
    	mainString = mainString.toLowerCase();
    	subString = subString.toLowerCase();
    	if(mainString.contains(subString)) 
    		return true;
    	else 
    		return false;
    }
    
    public static String getResmanInfo(String resmanApi, String taskId) {
         
        try {
            if(resmanApi != null) {
                String host = resmanApi.split(":")[1].replace("//", "").replace("\\", "");
                String buildName = getResmanBuildName(host, taskId);                 
                String buildUrl = "<br/><br/>Resman build url:<br/>http://" + host + ":8080/resman/#/builds/" + buildName;
                String taskUrl = "<br/>Resman task url:<br/>http://" + host + "/tasks/" + taskId + "<br/><br/>";
                
                return buildUrl + taskUrl;
            }
        } catch (Exception e) {
            logger.warn("Error occurred trying to parse resman url: " + e.getMessage());
            e.printStackTrace();
        }
             
        return "";
    }
    
    private static String getResmanBuildName(String host,
                                             String taskId) {

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet jsonRequest = new HttpGet("http://" + host + ":8080/resman/api/tasks/" + taskId);
        try {
            HttpResponse jsonResponse = client.execute(jsonRequest);
            HttpEntity responseEntity = jsonResponse.getEntity();
            String jsonData = EntityUtils.toString(responseEntity);
            logger.debug("Resman api output: " + jsonData);
            
            JSONObject obj = new JSONObject(jsonData); 
            return obj.getString("buildName");
          
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        logger.info("Unable to find build name for task id: " + taskId);
        return null;       
    }
    
    public static void addEmailAttachments(String dir, String fileExtension, ArrayList<String> attachments) {

        if(new File(dir).exists()) {
            
            List<File> files = (List<File>) FileUtils.listFiles(new File(dir), new String[]{fileExtension}, true);
            for (File file : files) {
                try {
                    attachments.add(file.getCanonicalPath());
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.warn("Error attaching files with extension '{}': {}", fileExtension, e.getMessage());
                } 
            }
        }
    }   
}
