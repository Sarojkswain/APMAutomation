/**
 * This script file measures Resource timings
 * It opens the ACC app, logs in, clicks on various buttons and captures the
 * Resource timings by dumping the Resource Timing API in a file
 */
import java.util.*;
import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.net.*;

public class TASChrome {
    private WebDriver driver;
    private String baseUrl;
    private boolean acceptNextAlert = true;
    private StringBuffer verificationErrors = new StringBuffer();
    private long sleepTime;
    private long totalTime;
    private long startTime;
    private long endTime;
    private String serverName;
    private JavascriptExecutor js;

    private static final String CSV_FILE_NAME = "ResourceTimingValues-";
    private static final String CSV_FILE_FORMAT = ".csv";

    public static void main(String arg[]) {
        TASChrome tsc = new TASChrome();
        if (arg.length == 0) {
            tsc.SOP("Please enter parameters 'server name as http or https://hostname:portNo' 'total time in minutes' 'sleep time in seconds'");
            return;
        }
        tsc.serverName = arg[0];
        tsc.totalTime = Long.parseLong(arg[1]);
        tsc.sleepTime = Long.parseLong(arg[2]) * 1000;

        try {
            tsc.setUp();
            tsc.clickMarathon();
            tsc.SOP("********* DONE ***************");
            // If tearDown() is called, the browser window will be exited after the automation.
            // tsc.tearDown();
            // tsc.SOP("###### Now do some calculations ######");
            // tsc.callResourceApi();
        } catch (Exception e) {
            tsc.SOP("Exception occurred in set up" + e);
        }
    }


    public void setUp() throws Exception {
        // set the correct path for chrome driver
        System.setProperty("webdriver.chrome.driver", "libs\\chromedriver.exe");
        driver = new ChromeDriver();
        // *** If we want to automate with Firefox, just toggle the commented out line for Firefox
        // with Chrome
        // driver=new FirefoxDriver();

        // ***** change the machine name as required
        // baseUrl = "https://tas-scx-ne6:8443/";
        baseUrl = serverName;
        js = (JavascriptExecutor) driver;

        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }


    public void clickMarathon() throws Exception {
        // driver.get(baseUrl + "/login#/");
        startTime = System.currentTimeMillis();
        endTime = startTime + (totalTime * 60 * 1000);
        SOP("**********CONFIGURATION**************");
        SOP("start time: " + (new SimpleDateFormat("hh:mm:ss a")).format(new Date(startTime)));
        SOP("end time: " + (new SimpleDateFormat("hh:mm:ss a")).format(new Date(endTime)));
        SOP("sleep delay: " + (sleepTime / 1000) + " seconds");
        SOP("*********STARTED**********");
        driver.get(baseUrl);
        driver.findElement(By.id("username1")).clear();
        driver.findElement(By.id("username1")).sendKeys("user@example.com");
        driver.findElement(By.id("password1")).clear();
        driver.findElement(By.id("password1")).sendKeys("acc");
        driver.findElement(By.id("login-form-btn")).click();
        // This is the sleep time which is used to disable cache.
        Thread.sleep(10000);
        js.executeScript("window.performance.setResourceTimingBufferSize(3000)");
        // SOP("result of setting buffer size= "+bufferSize);
        while (true) {
            if (checkTime())
                driver.findElement(By.cssSelector("#accLinkAgents > img.menuBarIcon")).click();
            else
                break;
            Thread.sleep(sleepTime);
            if (checkTime())
                driver.findElement(By.cssSelector("#accLinkReports > img.menuBarIcon")).click();
            else
                break;
            Thread.sleep(sleepTime);
            if (checkTime())
                driver.findElement(By.cssSelector("#accLinkPackages > img.menuBarIcon")).click();
            else
                break;
            Thread.sleep(sleepTime);
            if (checkTime())
                driver.findElement(By.cssSelector("#accLinkHome > img.menuBarIcon")).click();
            else
                break;
            Thread.sleep(sleepTime);
        }

        dumpResourceAPI();
    }


    public void tearDown() throws Exception {
        driver.quit();
        String verificationErrorString = verificationErrors.toString();
        if (!"".equals(verificationErrorString)) {
            fail(verificationErrorString);
        }
    }


    private void dumpResourceAPI() {
        String filename = CSV_FILE_NAME + getCurrentDate() + CSV_FILE_FORMAT;
        Object obj = js.executeScript("return window.performance.getEntriesByType(\"resource\");");
        // The object returned is an ArrayList
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

        // Make header string
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < keyArray.length; i++) {
            if (i == (keyArray.length - 1))
                sb.append(keyArray[i]);
            else
                sb.append(keyArray[i] + ",");
        }
        String header = sb.toString();
        // Store these header string to a file
        appendToFile(filename, header);
        SOP("header string :" + header);
        // Each object inside is TransformedEntriesSet
        for (int i = 0; i < al.size(); i++) {
            // get Each key
            map = (Map<String, ?>) al.get(i);
            sb = new StringBuffer();
            for (int j = 0; j < keyArray.length; j++) {
                String key = keyArray[j];
                Object value = map.get(key);
                if (key.equals("name")) {
                    value = URLEncoder.encode((String) value);
                }
                if (j == (keyArray.length - 1))
                    sb.append(value);
                else
                    sb.append(value + ",");
            }
            appendToFile(filename, sb.toString());
        }
    }



    private double addToValue(double value, Object obj) {
        if (obj instanceof Double) {
            value += (double) obj;
        } else {
            if (obj instanceof Long) {
                value += (long) obj;
            }
        }
        return value;
    }

    private boolean checkTime() {
        return System.currentTimeMillis() < endTime;
    }

    private void SOP(String s) {
        System.out.println(s);
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
            // exception handling left as an exercise for the reader
            e.printStackTrace();
        } finally {
            try {
                if (out != null) out.close();
            } catch (Exception e) {
                // exception handling left as an exercise for the reader
                e.printStackTrace();
            }
            try {
                if (bw != null) bw.close();
            } catch (IOException e) {
                // exception handling left as an exercise for the reader
                e.printStackTrace();
            }
            try {
                if (fw != null) fw.close();
            } catch (IOException e) {
                // exception handling left as an exercise for the reader
                e.printStackTrace();
            }
        }

    }
}
