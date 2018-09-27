/*
 * Copyright (c) 2016 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.browseragent.testsupport.collector.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ca.apm.browseragent.testsupport.collector.BATestCollector;
import com.ca.apm.browseragent.testsupport.collector.pojo.Attributes;
import com.ca.apm.browseragent.testsupport.collector.pojo.Configuration;
import com.ca.apm.eum.datamodel.EUM;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

/**
 * Utils for the test collector related classes
 *
 * @author Doug Briere
 */

public class BATestCollectorUtils {

    public static final String BIG_FILE = "BA.js";
    public static final String BIG_FILE_EXT = "BAExt.js";


    // Requestion options
    public static final String METHOD_OPTIONS = "OPTIONS";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_GET = "GET";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";

    // Status codes, for now lets not depend on third party for them. Defining them here
    public static final int HTTP_OK = 200;
    public static final int HTTP_OK_NO_CONTENT = 204;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_SERVER_ERROR = 500;

    private final static Logger LOGGER = Logger.getLogger(BATestCollectorUtils.class);

    public static final String DEFAULT_TENANT = "defaulttenant";
    public static final String DEFAULT_APP = "defaultapp";

    private static final String COLLECTOR_TEMP_DIRECTORY = "BATestCollectorTestsTempDir";

    /**
     * To instruct this test file to use a user specified directory (that the user previously
     * created)
     * Pass -DbaCollectorWorkingDir=C:\\MyCollectorWorkingDir as a VM argument to the eclipse
     * run configuration
     */
    private static final String BA_LOCAL_DIRECTORY_PROPERTY = "baCollectorWorkingDir";


    /**
     * Keeps track of wait metrics for test reporting purposes only
     */

    private static final List<String> waitStats = new ArrayList<String>();


    private static final ObjectMapper JACKSON_OBJECT_MAPPER = new ObjectMapper();

    /**
     * Returns a default Configuration
     *
     * @return Configuration
     */

    public static Configuration getDefaultConfiguration(String collectionUrl) {
        Configuration returnConfig = new Configuration();

        returnConfig.setProfileId(1);
        returnConfig.setProfileName("BA");

        long createdTime = System.currentTimeMillis();
        returnConfig.setCreated(createdTime);
        returnConfig.setLastUpdated(createdTime);

        // Set defaults
        Attributes attributes = new Attributes();
        returnConfig.setBaAttributes(attributes);

        attributes.setBrowserAgentEnabled(true);
        attributes.setPageLoadMetricsEnabled(true);
        attributes.setPageLoadMetricsThreshold(0);
        attributes.setAjaxMetricsEnabled(true);

        // This should remain 0 so that automation doesnt have to wait a delay on each test
        // which over time could add up.
        attributes.setAjaxMetricsThreshold(0);
        attributes.setJsFunctionMetricsEnabled(false);
        attributes.setJsFunctionMetricsThreshold(0);
        attributes.setGeoEnabled(false);
        attributes.setUrlExcludeList(new String[] {});
        attributes.setUrlIncludeList(new String[] {});
        attributes.setMetricFrequency(3750);
        attributes.setJsErrorsEnabled(true);
        attributes.setAjaxErrorsEnabled(false);
        attributes.setBrowserLoggingEnabled(true);
        attributes.setUrlMetricOff(false);
        attributes.setSessionTimeout(1000 * 60 * 60); // 1 hour
        attributes.setGeoHighAccuracyEnabled(false);
        attributes.setGeoMaximumAge(10000);
        attributes.setGeoTimeout(5000);
        attributes.setCookieCaptureEnabled(false);


        // Softpages - SPA - single page app support
        attributes.setSoftPageMetricsEnabled(true);
        attributes.setDomChangePollingInterval(100);
        attributes.setDomChangeTimeout(10000);


        //
        // Set the passed in value
        //
        attributes.setCollectorUrl(collectionUrl);

        return returnConfig;
    }

    /**
     * Helper to write a string to file
     * 
     * @param contents
     * @param file
     * @throws IOException
     */

    public static void writeStringToFile(String contents, File file) throws IOException {
        Path fileAsPath = file.toPath();
        Files.write(fileAsPath, contents.getBytes());
    }

    /**
     * Helper to read string from file.
     * 
     * @param file
     * @return contents of the passed in file as String
     * @throws IOException
     */

    public static String readStringFromFile(File file) throws IOException {

        Path fileAsPath = file.toPath();
        byte[] allBytes = Files.readAllBytes(fileAsPath);
        String returnString = new String(allBytes);
        return returnString;
    }


    public static String readString(BufferedReader bufferedReader) throws IOException {
        StringBuffer returnString = new StringBuffer();
        int ch = bufferedReader.read();
        while (ch != -1) {
            returnString.append((char) ch);
            ch = bufferedReader.read();
        }

        return returnString.toString();
    }


    /**
     * Returns a string of pretty formated json
     *
     * @param o
     * @return
     */

    public static String getPrettyJson(Object o) {
        String prettyJson = "";

        try {
            ObjectMapper mapper = new ObjectMapper();
            prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
        } catch (Exception e) {
            LOGGER.error("Failed to make pretty json with exception: \n", e);
        }

        return prettyJson;
    }

    /**
     * Helper method for the handler classes, this will close the passed in exchange.
     *
     * @param httpExchange
     * @param response
     * @throws IOException
     */

    public static void sendResponse(HttpExchange httpExchange, String response) throws IOException {
        sendResponse(httpExchange, response, HTTP_OK);
    }

    /**
     * Helper method for the handler classes, this will close the passed in exchange.
     *
     * @param httpExchange
     * @param response
     * @param statusCode
     * @throws IOException
     */

    public static void sendResponse(HttpExchange httpExchange, String response, int statusCode)
        throws IOException {

        httpExchange.sendResponseHeaders(statusCode, response.length());

        // While the faint of heart may look at this and see a hack... allow me to explain
        // According to https://httpstatuses.com/204 , 204 is:
        // The server has successfully fulfilled the request and that there is no
        // additional content to send in the response payload body
        // Because of this the outputstream on the request body will be closed after the above
        // response headers are sent.
        // so inorder to avoid StreamClosedException this check is being made.
        if (HTTP_OK_NO_CONTENT == statusCode) {
            String codeStr = "HTTP_OK_NO_CONTENT(" + HTTP_OK_NO_CONTENT + ")";

            if (response != null && response.length() > 0) {
                LOGGER.error("sendResponse has found response content for code " + codeStr);
                LOGGER.error("sendResponse content WONT be sent, caller trying to send : \n "
                    + response);
            }

            LOGGER.info("sendResponse called for " + codeStr + " no output being writen");
        } else {
            OutputStream outputSream = httpExchange.getResponseBody();
            outputSream.write(response.getBytes());
            outputSream.flush();
            outputSream.close();
        }

        // Close the exchange
        httpExchange.close();

        // No exceptions assume success, log as such.
        int length = response != null ? response.length() : -1; // too verbose to print entire
                                                                // content
        LOGGER.debug("sendResponse statusCode: " + statusCode + " response length: " + length);
    }

    /**
     * Helper for MetricCollectionHandler wait method for test reporting purposes to see
     * which tests might be waiting longer than they need to.
     * 
     * @param collectorInstance
     * @param actualTime
     * @param allowedTime
     */

    public static void addNewWaitPerformanceMetric(String collectorInstance, long actualTime,
        long allowedTime) {
        long diff = allowedTime - actualTime;
        waitStats.add(collectorInstance + "\t\tActual: " + actualTime + "\t\tAllowed: "
            + allowedTime + "\t\tSurplus: " + diff);
    }

    /**
     * Returns a copy of the metics added by addNewWaitPerformanceMetric
     */

    public static List<String> getPerfMetrics() {
        return new ArrayList(waitStats);
    }

    /**
     * Helper determine working directory, either a user specified or system
     * 
     * @return String workig directory
     * @throws IOException
     */

    public static String getCollectorWorkingDir() throws IOException {
        String userSpecifiedWorkingDirectory = System.getProperty(BA_LOCAL_DIRECTORY_PROPERTY);

        System.out.println("userSpecifiedWorkingDirectory " + userSpecifiedWorkingDirectory);

        String collectorWorkingDir = null;
        if (userSpecifiedWorkingDirectory != null && userSpecifiedWorkingDirectory.length() > 0) {
            collectorWorkingDir = userSpecifiedWorkingDirectory;
        } else {
            collectorWorkingDir = getTempDirForCollector();

        }

        return collectorWorkingDir;
    }

    /**
     * This method will insert the snippet into the specified page. The page must have a head tag
     * 
     * @param fullPathFileName
     * @return String result message
     */

    public static String insertSnippetIntoPage(String fullPathFileName) throws Exception {
        String returnMessage = "Success";

        String jspPage = BATestCollectorUtils.readStringFromFile(new File(fullPathFileName));

        File backupFile = new File(fullPathFileName + ".orig");


        String searchString = "BA_AXA";

        boolean backupExists = backupFile.exists();
        boolean snippetExists = jspPage.contains(searchString);

        LOGGER.debug("insertSnippetIntoPage for request file: " + fullPathFileName
            + " backupExists: " + backupExists + ", snippetExists: " + snippetExists);

        // If the backup doesnt exist and they didnt already add a snippet
        //
        if (!backupFile.exists() && !snippetExists) {

            // First lets do a backup
            com.google.common.io.Files.copy(new File(fullPathFileName), backupFile);

            // Create instance of the collector, it uses default tenant and app
            // which assumes the test pages will do the same... Perhaps needs work in the
            // future,
            // but for now..

            synchronized (BATestCollectorUtils.class) {
                if (BATestCollector.lastSnippet == null) {
                    // This will set the lastSnippet
                    BATestCollector baTC = new BATestCollector(null, null);
                    baTC.stopServer();
                }
            }

            final String HEAD_LOWER_CASE = "<head>";
            final String HEAD_UPPER_CASE = "<HEAD>";

            // Handle two types of tags...
            String newJspPage = null;
            if (jspPage.contains(HEAD_LOWER_CASE)) {
                newJspPage =
                    jspPage.replace(HEAD_LOWER_CASE, HEAD_LOWER_CASE + "\r\n"
                        + BATestCollector.lastSnippet);
            } else if (jspPage.contains(HEAD_UPPER_CASE)) {
                newJspPage =
                    jspPage.replace(HEAD_UPPER_CASE, HEAD_UPPER_CASE + "\r\n"
                        + BATestCollector.lastSnippet);
            }

            // Still null, indicate as such, just return the page
            if (newJspPage == null) {
                returnMessage =
                    "Couldnt not find head tag either as " + HEAD_LOWER_CASE + " or "
                        + HEAD_UPPER_CASE;
                newJspPage = jspPage;
            }

            PrintWriter pw = new PrintWriter(fullPathFileName);
            pw.print(newJspPage);
            pw.close();
        } else {
            if (backupExists) {
                returnMessage = "Backup file with .orig extension already exists";
            } else if (snippetExists) {
                returnMessage =
                    "Snippet of similar content already exists. content: " + searchString;
            }
        }

        return returnMessage;
    }


    /**
     * Converts the passed json string to an EUM object
     * 
     * @param jsonStr
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */

    public static EUM convertToEUM(String jsonStr) throws JsonParseException, JsonMappingException,
        IOException {
        EUM endUserMonitoringData = null;
        if (jsonStr != null) {
            JACKSON_OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
            endUserMonitoringData = (EUM) JACKSON_OBJECT_MAPPER.readValue(jsonStr, EUM.class);
        }
        return endUserMonitoringData;
    }

    /**
     * Converts the passed EUM object into a pretty json string.
     * 
     * @param endUserMonitoringData
     * @return
     * @throws JsonProcessingException
     */

    public static String convertToJsonString(EUM endUserMonitoringData)
        throws JsonProcessingException {
        String eumJson = null;
        if (endUserMonitoringData != null) {
            JACKSON_OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
            eumJson = JACKSON_OBJECT_MAPPER.writeValueAsString(endUserMonitoringData);
        }
        return eumJson;
    }

    /**
     * Extracts a file from the jar and writes to local file system
     * 
     * @param resource the path and resource name inside the jar
     * @param localFileSystemDest the full path and file name for the local filesystem
     * @throws Exception
     */

    public static void extractJarResourceTo(String resource, String localFileSystemDest)
        throws Exception {
        InputStream in = BATestCollector.class.getResourceAsStream(resource);
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        FileOutputStream fos = new FileOutputStream(localFileSystemDest);
        int ch = input.read();

        while (ch >= 0) {
            fos.write(ch);
            ch = input.read();
        }
        fos.flush();
        fos.close();
    }

    /**
     * This reverts the extension file back to the original
     * 
     * @param localFileSystemDest
     * @param useMin
     * @throws Exception
     */

    public static void revertExtensionToOriginal(String localFileSystemDest, boolean useMin)
        throws Exception {
        String extFile = useMin ? "/js/" + BIG_FILE_EXT : "/js-clear/" + BIG_FILE_EXT;
        extractJarResourceTo(extFile, localFileSystemDest);
    }


    /**
     * Helper to get a system temp directory for the collector
     * 
     * @return String a system temp directory
     * @throws IOException
     */

    private static String getTempDirForCollector() throws IOException {
        // creates a folder like: BATestCollectorTestsTempDir0103423942394823
        // located in something (windows): C:\Users\brido02\AppData\Local\Temp

        // DONT CHANGE from temp directory, see where dirPath is used below in the shut downhook
        Path dirPath = Files.createTempDirectory(COLLECTOR_TEMP_DIRECTORY);

        final File dirFile = dirPath.toFile();
        // dirFile.deleteOnExit(); // this doesnt work because it has items in it
        System.out.println("getBATestCollector temp dir " + dirFile);
        String collectorWorkingDir = dirFile.getCanonicalPath();

        // Register a hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                // System.out.println("Shutdown hook for " + testName + " called");
                BATestCollectorUtils.this.deleteDirectoryContents(dirFile);
                // System.out.println("Shutdown hook for " + testName + " completed");
            }
        });

        return collectorWorkingDir;
    }

    /**
     * This is a helper for getBATestCollector shutdown hook to remove temp files
     * 
     * @param rootDirectoryOrFile - BE VERY CAREFUL WHAT YOU PASS IN HERE!!!
     *        Should only be COLLECTOR_TEMP_DIRECTORY or one of its children
     */

    private static void deleteDirectoryContents(File rootDirectoryOrFile) {
        try {
            if (rootDirectoryOrFile.isDirectory()) {
                for (File f : rootDirectoryOrFile.listFiles()) {
                    deleteDirectoryContents(f);
                }
            }

            // Its either a directory or file, but contents should be gone ok to delete
            boolean result = false;

            // Provide some sanity checking that we are only deleting paths/files of the temp
            // director
            if (rootDirectoryOrFile.getCanonicalPath().contains(COLLECTOR_TEMP_DIRECTORY)) {
                rootDirectoryOrFile.delete();
                // System.out.println("Delete for " + rootDirectoryOrFile + " ,result is: " +
                // result);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
