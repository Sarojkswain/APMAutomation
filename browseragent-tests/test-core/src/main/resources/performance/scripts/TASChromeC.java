/**
 * This script file measures Page load timing and Resource Timing
 * It opens the ACC app, logs in, and captures the page load timnigs by dumping the
 * Navigation Timing API in a file.
 * It then click on several button to generate resources
 * It captures the Resource timings by dumping the resource timing API
 * Rinse and repeat
 */

import java.util.*;
import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
// import org.junit.*;
// import static org.junit.Assert.*;
// import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.net.*;

public class TASChromeC {
    private WebDriver driver;
    private String baseUrl;
    private boolean acceptNextAlert = true;
    private StringBuffer verificationErrors = new StringBuffer();
    private int count;
    private long totalIterations;
    private long sleepTime;
    private long startTime;
    private long endTime;
    private boolean pageHeaderFlag;
	 private boolean resourceHeaderFlag;
    private String serverName;
    private JavascriptExecutor js;
    private String pageFileName;
	private String resourceFileName;
    private String[] pageHeaderArray;
	private String[] resourceHeaderArray;
    private static List durationList;


    private static final String PAGE_CSV_FILE_NAME = "PageTimingValues-";
    private static final String CSV_FILE_FORMAT = ".csv";
	private static final String RESOURCE_CSV_FILE_NAME = "ResourceTimingValues-";
  

    public static void main(String arg[]) {
        TASChromeC tsc = new TASChromeC();
        if (arg.length == 0) {
            tsc.SOP("Please enter parameters 'server name as http or https://hostname:portNo or http/s://IP:portNo' 'total number of iterations' 'sleep time between iterations in seconds'");
            return;
        }
        tsc.serverName = arg[0];
        tsc.totalIterations = Long.parseLong(arg[1]);
        tsc.sleepTime = Long.parseLong(arg[2]) * 1000;
        try {
            tsc.setUp();
        } catch (Exception e) {
            tsc.SOP("Exception occurred in set up: " + e);
        }
        tsc.count = 0;
        tsc.startTime = System.currentTimeMillis();
        tsc.SOP("start time: "
            + (new SimpleDateFormat("hh:mm:ss a")).format(new Date(tsc.startTime)));

        for (int i = 0; i < tsc.totalIterations; i++) {
            try {
                tsc.SOP("*************** STARTING iteration " + (i + 1) + "*************** ");
                tsc.clickMarathon();
                tsc.SOP("*************** FINISHED iteration "+(i+1)+" ***************");
                // If tearDown() is called, the browser window will be exit after the automation.
                // tsc.tearDown();
            } catch (Exception e) {
                tsc.SOP("Exception occurred in click marathon: " + e);
            }
        }// end of for loop

        tsc.SOP("Final Count value: " + tsc.count);
        tsc.endTime = System.currentTimeMillis();
        tsc.SOP("end time: " + (new SimpleDateFormat("hh:mm:ss a")).format(new Date(tsc.endTime)));
        tsc.SOP("*************** RUN COMPLETION ***************");

    }


    public void setUp() throws Exception {

        pageHeaderFlag = false;
		resourceHeaderFlag=false;
        pageHeaderArray = null;
		resourceHeaderArray=null;
        pageFileName = PAGE_CSV_FILE_NAME + getCurrentDate() + CSV_FILE_FORMAT;
		resourceFileName= RESOURCE_CSV_FILE_NAME + getCurrentDate() + CSV_FILE_FORMAT;
        baseUrl = serverName;
    }

    public void clickMarathon() throws Exception {
        // Set the correct path for chrome driver
        System.setProperty("webdriver.chrome.driver", "libs\\chromedriver.exe");
		// If we want to automate with Firefox, just use the appropriate driver
        // driver=new FirefoxDriver();
        driver = new ChromeDriver();
        js = (JavascriptExecutor) driver;
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        driver.get(baseUrl);
        // We sleep this amount of time to let the page be loaded and display UI elements
        Thread.sleep(5000);

		// Update the username and password as required
        driver.findElement(By.id("username1")).clear();
        driver.findElement(By.id("username1")).sendKeys("user@example.com");
        driver.findElement(By.id("password1")).clear();
        driver.findElement(By.id("password1")).sendKeys("acc");
        driver.findElement(By.id("login-form-btn")).click();
		
        // We sleep this amount of time to let index.html page be loaded
        Thread.sleep(5000);
		// Increase the Resource timing buffer
		js.executeScript("window.performance.setResourceTimingBufferSize(1000)");

        count++;
        callNavigationTimingAPI();
       // Thread.sleep(sleepTime);
		for(int i=0;i<5;i++){
			//click on Agent tab
			driver.findElement(By.cssSelector("#accLinkAgents > img.menuBarIcon")).click();
			//sleep
			Thread.sleep(sleepTime);
			//click on Reports tab
			driver.findElement(By.cssSelector("#accLinkReports > img.menuBarIcon")).click();
			Thread.sleep(sleepTime);
			//click on Packages tab
			driver.findElement(By.cssSelector("#accLinkPackages > img.menuBarIcon")).click();
			Thread.sleep(sleepTime);
			//click on Home tab
			driver.findElement(By.cssSelector("#accLinkHome > img.menuBarIcon")).click();
			Thread.sleep(sleepTime);
			dumpResourceAPI();
			//Reload the page
			js.executeScript("location.reload()");
			Thread.sleep(5000);
			js.executeScript("window.performance.setResourceTimingBufferSize(1000)");
			if(i!=4)
				callNavigationTimingAPI();
		}

        driver.quit();
		
		//Used to delete cache files for Browser
		//deleteChromeCache();
    }
	// ****************************************** Utility function ***************************************************************
	  private void dumpResourceAPI() {
        Object obj = js.executeScript("return window.performance.getEntriesByType(\"resource\");");
		
	    if (!resourceHeaderFlag) {
			resourceHeaderArray = getResourceHeaders(obj);
			String header = getHeaderString(resourceHeaderArray);
			appendToFile(resourceFileName, header);
			resourceHeaderFlag = true;
		}
		 ArrayList<?> al = (ArrayList<?>) obj;
		 StringBuffer sb = null;
		 Map<String, ?> map=null;
        // Each object inside is TransformedEntriesSet
        for (int i = 0; i < al.size(); i++) {
            // get Each key
            map = (Map<String, ?>) al.get(i);
            sb = new StringBuffer();
            for (int j = 0; j < resourceHeaderArray.length; j++) {
                String key = resourceHeaderArray[j];
                Object value = map.get(key);
                if (key.equals("name")) {
                    value = URLEncoder.encode((String) value);
                }
                if (j == (resourceHeaderArray.length - 1))
                    sb.append(value);
                else
                    sb.append(value + ",");
            }
            appendToFile(resourceFileName, sb.toString());
        }
    }


    // public void tearDown() throws Exception {
    // driver.quit();
    // String verificationErrorString = verificationErrors.toString();
    // if (!"".equals(verificationErrorString)) {
    // fail(verificationErrorString);
    // }
    // }


	private void deleteChromeCache(){
		//Update the location of browser cache if this function is called
		File f=new File("C:\\Users\\sanya03\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\Cache");
		for(File f1:f.listFiles()){
			if(!f1.isDirectory()){
					f1.delete();
			}
		}
	}
	//Not used
    private boolean checkTime() {
        return System.currentTimeMillis() < endTime;
    }

    private void SOP(String s) {
        System.out.println(s);
    }

    private void callNavigationTimingAPI() {

        Object obj = js.executeScript("return window.performance.timing");

        // Get and store the headers to the output file
        if (!pageHeaderFlag) {
            pageHeaderArray = getPageHeaders(obj);
            String header = getHeaderString(pageHeaderArray);
            appendToFile(pageFileName, header);
            pageHeaderFlag = true;
        }
        StringBuffer sb = new StringBuffer();
        Map<String, ?> map = (Map<String, ?>) obj;
        for (int i = 0; i < pageHeaderArray.length; i++) {
            // get Each key
            String key = pageHeaderArray[i];
            Object value = map.get(key);
            if (i == (pageHeaderArray.length - 1))
                sb.append(value);
            else
                sb.append(value + ",");
        }
        appendToFile(pageFileName, sb.toString());
    }

    private String[] getPageHeaders(Object obj) {
        // The object is of type TransformedEntriesMap
        Map<String, ?> map = (Map<String, ?>) obj;
        Set<String> set = map.keySet();
        // Remove the header toJSON
        if (set.contains("toJSON")) {
            set.remove("toJSON");
        }
        // Convert the set to array
        String keyArray[] = new String[set.size()];
        keyArray = set.toArray(keyArray);
        return keyArray;
    }
	private String[] getResourceHeaders(Object obj){
		// The object is of type ArrayList
        ArrayList<?> al = (ArrayList<?>) obj;
        // SOP(al.get(0).getClass().getName());
        // Get the headers
        Map<String, ?> map = (Map<String, ?>) al.get(0);
        Set<String> set = map.keySet();
        // Remove the header toJSON
        if (set.contains("toJSON")) {
            set.remove("toJSON");
        }

        // Convert the set to array
        String keyArray[] = new String[set.size()];
        keyArray = set.toArray(keyArray);
		return keyArray;
	}
	

    private String getHeaderString(String[] headerArray) {
        // Make header string
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < headerArray.length; i++) {
            if (i == (headerArray.length - 1))
                sb.append(headerArray[i]);
            else
                sb.append(headerArray[i] + ",");
        }
        return sb.toString();
    }


    private String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");
        // get current date time with Date()
        Date date = new Date();
        return (dateFormat.format(date)).toString();
    }

    private void appendToFile(String name, String data) {

        FileWriter fw = null;
        BufferedWriter bw = null;
        PrintWriter out = null;

        try {
            fw = new FileWriter(name, true);
            bw = new BufferedWriter(fw);
            out = new PrintWriter(bw);
            out.println(data);
            out.close();
        } catch (IOException e) {
            SOP("Error while appending data to file: " + e);
        } finally {
            try {
                if (out != null) out.close();
            } catch (Exception e) {
                SOP("Error while closing PrintWriter connection: " + e);
            }
            try {
                if (bw != null) bw.close();
            } catch (IOException e) {
                SOP("Error while closing BufferedWriter connection: " + e);
            }
            try {
                if (fw != null) fw.close();
            } catch (IOException e) {
                SOP("Error while closing FileWriter connection: " + e);
            }
        }

    }
	
	// ****************************************** End of Utility function ***************************************************************
}
