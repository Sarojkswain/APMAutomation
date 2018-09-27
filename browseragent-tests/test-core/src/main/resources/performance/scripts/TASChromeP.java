/**
 * This script file measures Page load timings
 * It opens the ACC app, logs in, and captures the page load timnigs by dumping the
 * Navigation Timing API in a file
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

public class TASChromeP {
    private WebDriver driver;
    private String baseUrl;
    private boolean acceptNextAlert = true;
    private StringBuffer verificationErrors = new StringBuffer();
    private int count;
    private long totalIterations;
    private long sleepTime;
    private long startTime;
    private long endTime;
    private boolean headerFlag;
    private String serverName;
    private JavascriptExecutor js;
    private String fileName;
    private String[] headerArray;
    private static List durationList;

    private static final String CSV_FILE_NAME = "PageTimingValues-";
    private static final String CSV_FILE_FORMAT = ".csv";

    public static void main(String arg[]) {
        TASChromeP tsc = new TASChromeP();
        if (arg.length == 0) {
            tsc.SOP("Please enter parameters 'server name as http or https://hostname:portNo' 'total number of iterations' 'sleep time between iterations in seconds'");
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
                tsc.SOP("*************** DONE ***************");
                // If tearDown() is called, the browser window will be exited after the automation.
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

        headerFlag = false;
        headerArray = null;
        fileName = CSV_FILE_NAME + getCurrentDate() + CSV_FILE_FORMAT;
        // *** If we want to automate with Firefox, just toggle the commented out line for Firefox
        // with Chrome
        // driver=new FirefoxDriver();
        baseUrl = serverName;

    }


    public void clickMarathon() throws Exception {
        // set the correct path for chrome driver
        System.setProperty("webdriver.chrome.driver", "libs\\chromedriver.exe");
        driver = new ChromeDriver();
        js = (JavascriptExecutor) driver;
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        driver.get(baseUrl);
        // We sleep this amount of time to let the page be loaded and display UI elements
        Thread.sleep(5000);

        driver.findElement(By.id("username1")).clear();
        driver.findElement(By.id("username1")).sendKeys("user@example.com");
        driver.findElement(By.id("password1")).clear();
        driver.findElement(By.id("password1")).sendKeys("acc");
        driver.findElement(By.id("login-form-btn")).click();

        // We sleep this amount of time to let index.html page be loaded
        Thread.sleep(5000);

        count++;
        callNavigationTimingAPI();
        Thread.sleep(sleepTime);
        driver.quit();
    }


    // public void tearDown() throws Exception {
    // driver.quit();
    // String verificationErrorString = verificationErrors.toString();
    // if (!"".equals(verificationErrorString)) {
    // fail(verificationErrorString);
    // }
    // }


    private boolean checkTime() {
        return System.currentTimeMillis() < endTime;
    }

    private void SOP(String s) {
        System.out.println(s);
    }

    private void callNavigationTimingAPI() {

        Object obj = js.executeScript("return window.performance.timing");

        // Get and store the headers to the output file
        if (!headerFlag) {
            headerArray = getHeaders(obj);
            String header = getHeaderString(headerArray);
            appendToFile(fileName, header);
            headerFlag = true;
        }
        StringBuffer sb = new StringBuffer();
        Map<String, ?> map = (Map<String, ?>) obj;
        for (int i = 0; i < headerArray.length; i++) {
            // get Each key
            String key = headerArray[i];
            Object value = map.get(key);
            if (i == (headerArray.length - 1))
                sb.append(value);
            else
                sb.append(value + ",");
        }
        appendToFile(fileName, sb.toString());
    }

    private String[] getHeaders(Object obj) {
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
}
