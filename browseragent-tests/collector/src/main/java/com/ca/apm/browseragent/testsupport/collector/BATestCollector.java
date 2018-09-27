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

package com.ca.apm.browseragent.testsupport.collector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.ca.apm.browseragent.testsupport.collector.handler.AbstractHttpHandler;
import com.ca.apm.browseragent.testsupport.collector.handler.GetFileContextHandler;
import com.ca.apm.browseragent.testsupport.collector.handler.MetricCollectionContextHandler;
import com.ca.apm.browseragent.testsupport.collector.handler.MetricDisplayContextHandler;
import com.ca.apm.browseragent.testsupport.collector.handler.ProfileConfigContextHandler;
import com.ca.apm.browseragent.testsupport.collector.handler.RootContextHandler;
import com.ca.apm.browseragent.testsupport.collector.pojo.Attributes;
import com.ca.apm.browseragent.testsupport.collector.pojo.Configuration;
import com.ca.apm.browseragent.testsupport.collector.util.AbstractPayloadType;
import com.ca.apm.browseragent.testsupport.collector.util.BATestCollectorUtils;
import com.ca.apm.browseragent.testsupport.collector.util.MetricPostListener;
import com.ca.apm.browseragent.testsupport.collector.util.MetricPostRecord;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;


/**
 * This class defines a test collector for the Browser Agent
 *
 * @author Doug Briere
 */

public class BATestCollector {
    public final String DEFAULT_TENANT_ID;
    public final String DEFAULT_APP_ID;
    public final String WORKING_DIRECTORY;
    public final String COLLECTOR_NAME;
    public final String HOSTED_AT_ADDRESS;

    private MetricCollectionContextHandler metricCollectionHandler = null;
    private ProfileConfigContextHandler profileConfigContextHandler = null;

    private HttpServer httpServer = null;

    private final Logger LOGGER;

    private static final int MAX_VALID_PORT = 65535;

    /**
     * This ID will be used as something to fill into the snippet. The returned snippet from this
     * collector instance will contain this value. Tas tests will automatically insert the snippet
     * value into the test page that will use this value as well.
     */

    public static final String APP_KEY = "BATestCollectorAppID";


    public volatile static String lastSnippet = null;


    /**
     * Handler helper to return response code
     * 
     */

    public class ResponseCodes extends AbstractHttpHandler {
        public static final String RESPONSE_CODE_CONTEXT = "/code";

        public void handle(HttpExchange httpExchange) throws IOException {

            try {

                String requestMethod = httpExchange.getRequestMethod();
                System.out.println("requestMethod " + requestMethod);
                switch (requestMethod) {
                    case BATestCollectorUtils.METHOD_OPTIONS:
                        handleOPTIONS(httpExchange);
                        break;
                    case BATestCollectorUtils.METHOD_GET:
                        handleGET(httpExchange);
                        break;
                    default:
                        LOGGER.warn("Unknown request: " + requestMethod);
                        BATestCollectorUtils.sendResponse(httpExchange, requestMethod
                            + " method not supported for this end point",
                            BATestCollectorUtils.HTTP_SERVER_ERROR);
                }
            }
            // DO NOT REMOVE !!! The server that calls HttpHandler tries to log excetpion to a
            // configured logger
            // but when the logger isnt configured the exception doesnt go to standard output and
            // not
            // seen. horrible!
            catch (Exception e) {
                LOGGER.error("Server error: ", e);
                BATestCollectorUtils.sendResponse(httpExchange, "Server error: " + e.getMessage(),
                    BATestCollectorUtils.HTTP_SERVER_ERROR);
                throw e;
            }

        }

        private void handleGET(HttpExchange httpExchange) throws IOException {
            setResponseHeaders(httpExchange);

            String path = httpExchange.getRequestURI().getPath();
            int lastIndex = path.lastIndexOf("/");
            String code = path.substring(lastIndex + 1, path.length());
            int statusCode = Integer.parseInt(code);

            BATestCollectorUtils.sendResponse(httpExchange, "", statusCode);
        }

        protected void registerAllEndPoints() {
            addEndPointRecord(getClass().getName(), new EndPointRecord("GET", RESPONSE_CODE_CONTEXT
                + "/yourCode", "Returns yourCode as the response code"));

        }
    }

    /**
     * This constructor specifically used for test automation. minification is enabled.
     * 
     * @param workingDir
     * @param collectorNameInstance
     */

    public BATestCollector(String workingDir, String collectorNameInstance) {
        this(5000, BATestCollectorUtils.DEFAULT_TENANT, BATestCollectorUtils.DEFAULT_APP,
            workingDir, collectorNameInstance, false, true);
    }

    /**
     * Constructor to create and start a collector instance at specified port. Used
     * for local runs via the batch file where minification is turned off
     *
     * @param port
     * @param tenantId
     * @param appId
     * @param workingDirectory - a directory on the localhost where profiles can be written to, this
     *        directory must exist!
     * @param collectorNameInstance - name of this collector, a directory will be created on the
     *        working directory to hold profiles
     * @throws Exception
     */

    public BATestCollector(int port, String tenantId, String appId, String workingDirectory,
        String collectorNameInstance) {
        this(port, tenantId, appId, workingDirectory, collectorNameInstance, false, false);
    }


    /**
     * Constructor to create and start a collector instance at specified port.
     *
     * @param port
     * @param tenantId
     * @param appId
     * @param workingDirectory - a directory on the localhost where profiles can be written to, this
     *        directory must exist!
     * @param collectorNameInstance - name of this collector, a directory will be created on the
     *        working directory to hold profiles
     * @param useLocalHost true if the collector should run on host IP, false to use a loopback
     *        127.0.0.1
     * @param useMinBigFile true if the collector should use the minified version of the big file
     */



    public BATestCollector(int port, String tenantId, String appId, String workingDirectory,
        String collectorNameInstance, boolean useLocalHost, boolean useMinBigFile) {

        if (port > MAX_VALID_PORT) {
            throw new IllegalArgumentException("BATestCollector port: " + port + " out of range: "
                + MAX_VALID_PORT);
        }

        if (workingDirectory == null) {
            System.out
                .println("constructor passed workingDirectory was null, going to locate vm passed or system");
            try {
                workingDirectory = BATestCollectorUtils.getCollectorWorkingDir();
                System.out.println("constructor workingDirectory was found to be: "
                    + workingDirectory);
            } catch (IOException e) {
                System.out
                    .println("Exception found, unable to locate/create working directory. Exiting");
                System.exit(1);
            }
        }

        if (collectorNameInstance == null || collectorNameInstance.length() == 0) {
            System.out
                .println("constructor passed collectorNameInstance was null or empty, going to set system time");
            collectorNameInstance = Long.toString(System.currentTimeMillis());
            System.out.println("constructor collectorNameInstance now: " + collectorNameInstance);
        }

        if (tenantId == null || tenantId.length() == 0) {
            System.out.println("constructor passed invalid tenant: " + tenantId + " using default");
            tenantId = BATestCollectorUtils.DEFAULT_TENANT;
        }

        if (appId == null || appId.length() == 0) {
            System.out.println("constructor passed invalid appId: " + appId + " using default");
            appId = BATestCollectorUtils.DEFAULT_APP;
        }


        // Initilize the finals
        this.DEFAULT_TENANT_ID = tenantId;
        this.DEFAULT_APP_ID = appId;
        this.WORKING_DIRECTORY = workingDirectory;
        this.COLLECTOR_NAME = collectorNameInstance;

        // Test, but dont create the working directory
        File workingFile = new File(WORKING_DIRECTORY);
        if (!workingFile.exists()) {
            System.out.println("BATestCollector: Working directory: " + WORKING_DIRECTORY
                + " not found, must exist! Please create, exiting");
            System.exit(1);
        }

        // Create the collector instance directory
        File collectorDir = new File(WORKING_DIRECTORY + File.separator + COLLECTOR_NAME);

        // if the collector instance directory doesnt exist, created i.e.
        // C:/workingdirectory/myinstance
        if (!collectorDir.exists()) {
            System.out.println("BATestCollector: Creating collector instance directory: "
                + COLLECTOR_NAME);
            collectorDir.mkdir();
        }

        // Create log directory: <workingdir>/<instance>/log/
        String logsDirectory =
            WORKING_DIRECTORY + File.separator + COLLECTOR_NAME + File.separator + "log";
        File logsDirFile = new File(logsDirectory);
        if (!logsDirFile.exists()) {
            boolean result = logsDirFile.mkdir();
            System.out.println("BATestCollector: Creating log directory: " + logsDirectory
                + " result: " + result);
        }

        // Create file <workingdir>/<instance>/log/output.log
        // This sets the propery in the log4j.properties
        // log4j.appender.file.File=${logfile.name}
        String logFileName = logsDirectory + File.separator + "output.log";
        System.setProperty("logfile.name", logFileName);

        // Configure the logger
        configureLoggerProps();

        // org.apache.log4j.PropertyConfigurator.configure("C:\\log4j.properties");
        // org.apache.log4j.PropertyConfigurator.configure("log4j.properties");
        // LogManager.resetConfiguration();

        LOGGER = Logger.getLogger(BATestCollector.class);

        LOGGER.info("Logging started to console and file ---->: " + logFileName);

        InetAddress thisHostAddress = null;

        // Use the host IP i.e. 130.200.... etc
        if (!useLocalHost) {
            try {
                thisHostAddress = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                LOGGER.error("InetAddress.getLocalHost exception", e);
            }
        }

        // if null or requested to use the loop back set as such i.e. 127.0.0.1...
        if (thisHostAddress == null || useLocalHost) {
            thisHostAddress = InetAddress.getLoopbackAddress();
            LOGGER.error("Using getLoopbackAddress " + thisHostAddress);
        }

        // This will enter a loop to try another port should the requested one be in use.
        // This is an issue on some systems where the server close is scheduled and
        // collector unit tests that follow it request the same port.
        String hostedAtByName = null;
        InetSocketAddress socketAddress = null;
        while (httpServer == null && port <= MAX_VALID_PORT) {
            socketAddress = new InetSocketAddress(thisHostAddress, port);

            // its better to try to use the host name in the snippet for less
            // possible changes
            try {
                hostedAtByName = socketAddress.getAddress().getHostName();
            } catch (Exception e) {
                LOGGER.error("Could not obtain host name", e);
            }

            // ... but if the name cant be obtained use the address
            if (hostedAtByName == null) {
                hostedAtByName = socketAddress.getAddress().getHostAddress();
            }

            // For testing purposes it appears that quickly starting a new collector the
            // previous address is still bound
            try {
                httpServer = HttpServer.create(socketAddress, 0);
            } catch (Exception e) {
                LOGGER.error("Failed to create HttpServer at port:  " + port + " message: "
                    + e.getMessage());

                // Try the next port number
                port += 1;
                LOGGER.debug("Going to attempt bind at port: " + port);
            }
        }

        HOSTED_AT_ADDRESS = "http://" + hostedAtByName + ":" + socketAddress.getPort();

        String snippetLine = getSnippetCode();
        lastSnippet = snippetLine;

        // Add the root context for end point information
        RootContextHandler rootContext = new RootContextHandler();
        httpServer.createContext(RootContextHandler.HOME_ROOT_CONTEXT, rootContext);


        ResponseCodes responseCodes = new ResponseCodes();
        httpServer.createContext(ResponseCodes.RESPONSE_CODE_CONTEXT, responseCodes);

        // Metric collection what the browser agent will post metrics to
        metricCollectionHandler = new MetricCollectionContextHandler(this);
        httpServer.createContext(MetricCollectionContextHandler.METRIC_COLLECTION_CONTEXT,
            metricCollectionHandler);

        // The collected/posted metrics will be displayed at this context/end point
        MetricDisplayContextHandler metricDisplay = new MetricDisplayContextHandler();

        // The display will listen for metric posts to cache each metric post. Cache list will be
        // dumped on display
        addMetricPostListener(metricDisplay);
        httpServer.createContext(MetricDisplayContextHandler.METRIC_DISPLAY_CONTEXT, metricDisplay);

        // Ability to serve up a file from the working directory, useful for the bigfile (ba.js)
        GetFileContextHandler getFileContext = new GetFileContextHandler(WORKING_DIRECTORY);
        httpServer.createContext(GetFileContextHandler.GET_FILE_CONTEXT, getFileContext);

        String collectionUrl = getCollectorURL();
        // Ability to create and serve up profiles
        profileConfigContextHandler =
            new ProfileConfigContextHandler(WORKING_DIRECTORY, COLLECTOR_NAME, collectionUrl);
        httpServer.createContext(ProfileConfigContextHandler.CONFIGS_CONTEXT,
            profileConfigContextHandler);

        // Be precise... make this call after above, but before we start.
        // Making this call will create the default profile by the passed tenant and app above
        Configuration config = getConfiguration();

        Attributes attrs = config.getBaAttributes();
        if (attrs != null) {
            String storedUrl = attrs.getCollectorUrl();

            if (storedUrl != null && !storedUrl.equals(collectionUrl)) {
                LOGGER.info("Constructor detected different collection urls\n" + "storedUrl: "
                    + storedUrl + "\n" + "currentUrl: " + collectionUrl);
                attrs.setCollectorUrl(collectionUrl);
            }
        }


        profileConfigContextHandler.updateConfiguration(config, DEFAULT_TENANT_ID, DEFAULT_APP_ID,
            null, true);


        try {
            String BIG_FILE = BATestCollectorUtils.BIG_FILE;
            String bigFileLocAsString = WORKING_DIRECTORY + File.separator + BIG_FILE;
            File bigFile = new File(bigFileLocAsString);

            if (!bigFile.exists()) {

                String bigFileLocation =
                    useMinBigFile ? "/js/" + BIG_FILE : "/js-clear/" + BIG_FILE;

                LOGGER.debug("bigFile: " + bigFile + " not found, creating from factory jar file: "
                    + bigFileLocation);

                BATestCollectorUtils.extractJarResourceTo(bigFileLocation, bigFileLocAsString);
            } else {
                LOGGER.debug("Found existing bigfile: " + bigFile);
            }
        } catch (Exception e) {
            LOGGER.error("Failure to create bigfile from jar: ", e);
        }

        // Now everything is configured, start..
        httpServer.start();

        // This might be over kill the API may already do this, but just in case...
        // lets make sure ports are not left in linger state
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                LOGGER.info("JVM Shutdown received stopping server");
                BATestCollector.this.stopServer();
            }
        }));


        LOGGER.info("\n\n BATestCollector has started, snippet information:\n\n " + snippetLine
            + "\n\n");

        LOGGER.info("Collector now ready on: " + getHostAddress());
    }

    public String getCollectorURL() {
        return getHostAddress() + MetricCollectionContextHandler.METRIC_COLLECTION_CONTEXT;
    }

    /**
     * Returns the snippet for this collector instance
     * 
     * @return String the snippet the user would have to insert
     */

    public String getSnippetCode() {
        String snippetLine =
            " <script src=\"" + getHostAddress() + "/getfile/" + BATestCollectorUtils.BIG_FILE_EXT
                + "\"></script>\r\n" +

                " <script id=\"BA_AXA\" src=\"" + getHostAddress() + "/getfile/BA.js\"\r\n"
                + "        data-profileUrl=\"" + getHostAddress() + "/configs/" + DEFAULT_TENANT_ID
                + "/" + DEFAULT_APP_ID + "\"\r\n" + "        data-tenantID=\"" + DEFAULT_TENANT_ID
                + "\"" + "  data-appID=\"" + DEFAULT_APP_ID + "\"\r\n" + "        data-appKey=\""
                + APP_KEY + "\">\n" + "\r\n  </script>\r\n";

        return snippetLine;
    }

    /**
     * There are various uses how the collector jar is used, stand alone manual testing,
     * unit functional testing for the collector itself and thirdly the tas automation.
     * Unfortunately, the log4j.properties file is not found in all uses cases. The search
     * policy to find the resource policy is not clear. To avoid having to write special cases
     * for now make configuration here which should be ok since all 3 use cases above users will
     * have access to the code.
     */

    private void configureLoggerProps() {
        /*
         * # Root logger option
         * #From log4 Priority
         * #public final static int FATAL_INT = 50000;
         * #public final static int ERROR_INT = 40000;
         * #public final static int WARN_INT = 30000;
         * #public final static int INFO_INT = 20000;
         * #public final static int DEBUG_INT = 10000;
         * 
         * log4j.rootLogger=DEBUG, file, stdout
         * 
         * # Direct log messages to a log file
         * log4j.appender.file=org.apache.log4j.RollingFileAppender
         * log4j.appender.file.File=${logfile.name}
         * log4j.appender.file.MaxFileSize=10MB
         * log4j.appender.file.MaxBackupIndex=15
         * log4j.appender.file.layout=org.apache.log4j.PatternLayout
         * log4j.appender.file.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n
         * 
         * # Direct log messages to stdout
         * log4j.appender.stdout=org.apache.log4j.ConsoleAppender
         * log4j.appender.stdout.Target=System.out
         * log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
         * log4j.appender.stdout.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L -
         * %m%n
         */
        Properties props = new Properties();


        props.put("log4j.rootLogger", "DEBUG, file, stdout");

        // Direct log messages to a log file
        props.put("log4j.appender.file", "org.apache.log4j.RollingFileAppender");
        props.put("log4j.appender.file.File", "${logfile.name}");
        props.put("log4j.appender.file.MaxFileSize", "10MB");
        props.put("log4j.appender.file.MaxBackupIndex", "15");
        props.put("log4j.appender.file.layout", "org.apache.log4j.PatternLayout");
        props.put("log4j.appender.file.layout.ConversionPattern",
            "%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n");

        // Direct log messages to stdout
        props.put("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
        props.put("log4j.appender.stdout.Target", "System.out");
        props.put("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
        props.put("log4j.appender.stdout.layout.ConversionPattern",
            "%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n");

        LogManager.resetConfiguration();
        PropertyConfigurator.configure(props);
    }

    public String getHostAddress() {
        return HOSTED_AT_ADDRESS;
    }


    /**
     * Stops the underlying http server, call during test tear down
     */

    public void stopServer() {
        try {

            httpServer.stop(0); // no delay

            // Subsequent callers may start a connection. Lets wait a little ensure its closed.
            Thread.currentThread().sleep(3000);
        } catch (Exception e) {
            LOGGER.error("stopServer exception stopping server", e);
        } finally {
            // Since this is the last thing callers would do, stop here
            // this releases pointers to open files so temp files can be deleted
            LogManager.shutdown();
        }
    }

    /**
     * This method will block the calling thread until waitTimeout or typeList,
     * has been found; whatever comes first.
     * 
     * @param waitTimeout - the maximum time to wait for the typeList
     * @param afterOrOnDate - the responses that have arrived by this time. Should be the date when
     *        the test was started
     * @param typesList the list of types you are looking for
     * @return List<MetricPostRecord> the returned records.
     * @throws MetricCollectionContextHandler.MetricCollectionTimeoutException
     */

    public List<MetricPostRecord> waitForNextNotification(long waitTimeout, Date afterOrOnDate,
        List<AbstractPayloadType> typeList)
        throws MetricCollectionContextHandler.MetricCollectionTimeoutException {
        return metricCollectionHandler
            .waitForNextNotification(waitTimeout, afterOrOnDate, typeList);
    }


    /**
     * NOT intented for functional testing.
     * To register your own listener for metric updates. Should not be required if using
     * waitForNextNotification.
     *
     * @see waitForNextNotification
     *
     * @param l
     */

    public void addMetricPostListener(MetricPostListener l) {
        metricCollectionHandler.addMetricPostListener(l);
    }

    /**
     * To unregister
     *
     * @param l
     */


    public void removeMetricPostListener(MetricPostListener l) {
        metricCollectionHandler.removeMetricPostListener(l);
    }

    /**
     * Subsequent runs of the collector by the same instance, tenant and app will use the last
     * updated profile.
     * For testing re-runs this wont be desired. Calls this explicity at the start of the test or
     * maybe make this default behavior of the collector, TBD by team input.
     * 
     * @return Configuration
     */

    public Configuration revertToDefaultConfiguration() {
        Configuration config = BATestCollectorUtils.getDefaultConfiguration(getCollectorURL());
        updateConfiguration(config, "restoreToDefault");

        return config;
    }


    /**
     * Returns the configuration based on the tenant and app used when the collector was created. If
     * one is not found the default is used and returned.
     *
     * @return Configuration
     */

    public Configuration getConfiguration() {
        return getConfiguration(DEFAULT_TENANT_ID, DEFAULT_APP_ID, true);
    }

    /**
     * Returns the configuration based on the passed tenant and app.
     * If one is not found the default is used and returned if allowDefaultIfNotFound is true.
     * 
     *
     * @param tenantId
     * @param appId
     * @param allowDefaultIfNotFound
     * @return Configuration which maybe null if the file was found, but couldnt be parsed
     */

    public Configuration getConfiguration(String tenantId, String appId,
        boolean allowDefaultIfNotFound) {
        return profileConfigContextHandler
            .getConfiguration(tenantId, appId, allowDefaultIfNotFound);
    }

    /**
     * Pushes updates of the passed configuration to storage.
     *
     * @param config
     * @param versionExtension can be null, a copy of the profile will be made that acts as a
     *        snapshot.
     */

    public void updateConfiguration(Configuration config, String versionExtension) {
        profileConfigContextHandler.updateConfiguration(config, DEFAULT_TENANT_ID, DEFAULT_APP_ID,
            versionExtension);
    }


    /**
     * This update method takes a string and should be used if the user needs to write specific
     * config
     * content to disk. This is to be used for negative testing where a bad/invalid config to be
     * used.
     * 
     * @param configAsString - some bogus string
     * @param versionExtension
     */

    public void updateConfiguration(String configAsString, String versionExtension) {

        profileConfigContextHandler.updateConfigurationString(configAsString, DEFAULT_TENANT_ID,
            DEFAULT_APP_ID, versionExtension);
    }

    /**
     * This update method takes a string and should be used if the user needs to write specific
     * config
     * content to disk. This is to be used for negative testing where a bad/invalid config to be
     * used.
     * 
     * @param configAsString - some bogus string
     * @param tenantId
     * @param appId
     * @param versionExtension
     */

    public void updateConfiguration(String configAsString, String tenantId, String appId,
        String versionExtension) {
        profileConfigContextHandler.updateConfigurationString(configAsString, tenantId, appId,
            versionExtension);
    }

    public Logger getLogger() {
        return LOGGER;
    }


    // Exposed for unit testing
    public static BATestCollector staticMainBATestCollector = null;

    public static void main(String args[]) throws Exception {
        try {
            // Start a collector instance which assumes the snippet looks something like the
            // following:
            // <script id="BA_AxA" src="http://127.0.0.1:5000/getfile/BA.js"
            // data-profileUrl="http://127.0.0.1:5000/configs/defaulttenant/defaultapp"
            // data-tenantID="defaulttenant" data-appID="defaultapp"
            // data-appKey="352095703asjkhfsdhf">
            // </script>
            //

            int port = Integer.parseInt(args[0]);
            String tenantId = args[1];
            String appId = args[2];
            String workingDirectory = args[3];
            String name = args[4];

            // System.out.println("input------>" + port + " " + tenantId + " " + appId + " "
            // + workingDirectory + " " + name);

            staticMainBATestCollector = new BATestCollector(port, // The port this collector will
                                                                  // listen on
                // locahost
                tenantId, // The default tenant used when not specified in other method calls
                appId, // The default app used when not specified in other method calls
                workingDirectory, // A directory where the collector and write files (must exist)
                name // A name for this collector instance
                );


            Logger logger = staticMainBATestCollector.getLogger();
            // Auto insert
            try {
                String testAppDirString = args[5];
                // System.out.println("------->" + testAppDirString);

                File testAppDir = new File(testAppDirString);

                if (testAppDir.exists()) {
                    // System.out.println("------->" + args[6]);
                    String[] files = args[6].split(",");

                    logger.info("Snippet insert starting for " + files.length + " files...");
                    for (String file : files) {
                        String fullPath = testAppDirString + File.separator + file;

                        String insertResult = "Unknown";
                        try {
                            insertResult = BATestCollectorUtils.insertSnippetIntoPage(fullPath);
                        } catch (Exception e) {
                            insertResult = e.getMessage();
                        }
                        logger.info("  File: " + fullPath);
                        logger.info("    Result : -----> " + insertResult);
                    }
                    logger.info("... Snippet insert complete");
                } else {
                    throw new FileNotFoundException("Test app directory: " + testAppDirString
                        + " was not found ");
                }
            } catch (Exception e) {
                logger.info("Attempt to auto insert snippet into test app failed with message: "
                    + e.getMessage());
                logger.info("Please edit BATestCollectorRun.bat");
                logger.info("Change TEST_APP_DIR to point to your local running tomcat app server");
                logger.info("add additional files to FILES_TO_INSERT_SNIPPET");
            }

        } catch (Exception e) {
            System.out
                .println("Usage: java -cp \".;BrowserAgentExt.jar\" BATestCollector <port> <tenantId> <appId> <workingDir> <collectorName>");
            throw e;
        }
    }
}
