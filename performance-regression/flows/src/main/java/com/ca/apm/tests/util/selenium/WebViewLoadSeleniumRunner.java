package com.ca.apm.tests.util.selenium;

import com.ca.apm.tests.util.selenium.SeleniumHelper.SelectionBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;

import static com.ca.apm.tests.util.selenium.SeleniumHelperResolver.getSeleniumHelper;

public class WebViewLoadSeleniumRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebViewLoadSeleniumRunner.class);

//    private static final String CSV_SEPERATOR = ",";

//    private static final int DEFAULT_NODEID_COLUMN_INDEX = 0;
//    private static final int DEFAULT_BROWSER_COLUMN_INDEX = 1;
//    private static final int DEFAULT_URL_COLUMN_INDEX = 2;
//
//    private static ArgumentParser parser;
//
//    private final String webviewServerHost;
//    private final Integer webviewServerPort;
    private final String webViewUser;
    private final String webViewPassword;

    private List<String> browsers = new ArrayList<>();
//    private List<String> urls = new ArrayList<>();

//    private List<SeleniumSession> sessions = new ArrayList<>();

    private final String webviewLoginUrl;
    @SuppressWarnings("unused")
    private final String logoutUrl;
    private final Map<String, Collection<String>> urlBrowserMap;

    public WebViewLoadSeleniumRunner(String webviewServerHost, Integer webviewServerPort,
        String webViewUser, String webViewPassword) {
//        this.webviewServerHost = webviewServerHost;
//        this.webviewServerPort = webviewServerPort;
        this.webViewUser = webViewUser == null ? "" : webViewUser;
        this.webViewPassword = webViewPassword == null ? "" : webViewPassword;
        this.webviewLoginUrl =
            "http://" + webviewServerHost + ":" + webviewServerPort + "/jsp/login.jsf";
        this.logoutUrl = "http://" + webviewServerHost + ":" + webviewServerPort + "/logout";
        this.urlBrowserMap = new HashMap<String, Collection<String>>();
    }
    
    
    public void waitForShutdown(long maxWait) {
        synchronized (this) {
            try {
                wait(maxWait);
            } catch (InterruptedException e) {
                // don't care what woke us up
            }
        }
    }
    
    public void addUrl(String browser, String url) {
        Collection<String> coll = urlBrowserMap.get(browser);
        if (coll == null) {
            coll = new ArrayList<>();
            urlBrowserMap.put(browser, coll);
        }
        coll.add(url);
    }
    
    
    public void run() {
        LOGGER.info("About to download Selenium webdrivers");
        browsers.addAll(urlBrowserMap.keySet());
        downloadWebDrivers();
        LOGGER.info("Downloaded");
        
        for (Entry<String, Collection<String>> entry: urlBrowserMap.entrySet()) {
            String browser = entry.getKey();
            for (String url: entry.getValue()) {
                SeleniumSession session = webviewLogin(browser, url);
                sleep(15000L);
                openWebViewUrl(session);
            }
        }
    }
    
    

//    public static void main(String[] args) throws IOException {
//        LOGGER.info("WebViewLoadSeleniumRunner.main():: entry");
//        try {
//            // parse arguments
//            LOGGER.info("WebViewLoadSeleniumRunner.main():: parsing arguments");
//            Namespace namespace = parseArgs(args);
//
//            WebViewLoadSeleniumRunner runner =
//                new WebViewLoadSeleniumRunner(namespace.getString("webviewServerHost"),
//                    namespace.getInt("webviewServerPort"), namespace.getString("webViewUser"),
//                    namespace.getString("webViewPassword"));
//
//            // get data
//            LOGGER.info("WebViewLoadSeleniumRunner.main():: loading input data");
//            runner.loadInputData(namespace);
//
//            // download Selenium webdrivers
//            LOGGER.info("WebViewLoadSeleniumRunner.main():: downloading webdrivers");
//            runner.downloadWebDrivers();
//
//            // start WebView sessions
//            LOGGER.info("WebViewLoadSeleniumRunner.main():: starting WebView sessions");
//            runner.startWebViewSessions();
//
//            // coffee break
//            LOGGER.info("WebViewLoadSeleniumRunner.main():: waiting");
//            sleep(15000L);
//
//            // open WebView urls
//            LOGGER.info("WebViewLoadSeleniumRunner.main():: opening WebView URLs");
//            runner.openWebViewUrls();
//        } catch (Exception e) {
//            throw ErrorUtils.logExceptionAndWrap(LOGGER, e,
//                "WebViewLoadSeleniumRunner.main() - exception occurred");
//        } finally {
//            LOGGER.info("WebViewLoadSeleniumRunner.main():: exit");
//        }
//    }

//    private void loadInputData(Namespace namespace) throws IOException {
//        String data = namespace.getString("data");
//        String file = namespace.getString("file");
//
//        List<String> webviewServerHostPlaceholders =
//            namespace.getList("webviewServerHostPlaceholders");
//        List<String> webviewServerPortPlaceholders =
//            namespace.getList("webviewServerPortPlaceholders");
//        List<String> webviewServerHostPortPlaceholders =
//            namespace.getList("webviewServerHostPortPlaceholders");
//
//        SortedMap<String, String> urlReplacements =
//            getUrlReplacements(webviewServerHost, webviewServerPort, webviewServerHostPlaceholders,
//                webviewServerPortPlaceholders, webviewServerHostPortPlaceholders);
//
//        if (StringUtils.isBlank(file)) {
//            if (StringUtils.isBlank(data)) {
//                // no input data provided
//                parser.printUsage();
//                System.exit(1);
//            } else {
//                // get input data from command line argument
//                getInputData(data, urlReplacements);
//            }
//        } else {
//            if (StringUtils.isNotBlank(data)) {
//                LOGGER.warn("Reading browser+url data from file {}, skipping argument -d/-data",
//                    file);
//            }
//            int nodeIdColumnIndex = namespace.getInt("nodeIdColumnIndex");
//            int browserColumnIndex = namespace.getInt("browserColumnIndex");
//            int urlColumnIndex = namespace.getInt("urlColumnIndex");
//            String nodeId = namespace.getString("nodeId");
//            // get input data from a file
//            getInputDataFromFile(file, nodeId, urlReplacements, nodeIdColumnIndex,
//                browserColumnIndex, urlColumnIndex);
//        }
//    }

//    private void getInputData(String data, SortedMap<String, String> urlReplacements) {
//        String[] items = data.split(CSV_SEPERATOR);
//        if (items.length % 2 != 0) {
//            throw new IllegalArgumentException("Wrong number of line elements: " + items.length
//                + ". Even-numbered count is expected");
//        }
//        for (int i = 0; (i + 1) < items.length;) {
//            String browser = items[i++];
//            String url = items[i++];
//            processInputDataLine(browser, url, urlReplacements);
//        }
//    }
//
//    private void getInputDataFromFile(String fileName, String nodeId,
//        SortedMap<String, String> urlReplacements, int nodeIdColumnIndex, int browserColumnIndex,
//        int urlColumnIndex) throws IOException {
//        List<String> lines =
//            Files.readAllLines((new File(fileName)).toPath(), Charset.defaultCharset());
//        for (String line : lines) {
//            String[] items = line.split(CSV_SEPERATOR);
//            if (nodeId != null && nodeId.equals(items[nodeIdColumnIndex])) {
//                String browser = items[browserColumnIndex];
//                String url = items[urlColumnIndex];
//                processInputDataLine(browser, url, urlReplacements);
//            }
//        }
//    }

//    private void processInputDataLine(String browser, String url,
//        SortedMap<String, String> urlReplacements) {
//        browsers.add(browser);
//        for (String placeholder : urlReplacements.keySet()) {
//            url = url.replace(placeholder, urlReplacements.get(placeholder));
//        }
//        urls.add(url);
//    }

    private void downloadWebDrivers() {
        WebDriverProcurementUtils.downloadWebDrivers(browsers);
    }

//    private void startWebViewSessions() {
//        for (int i = 0; i < urls.size(); i++) {
//            String browser = browsers.get(i);
//            String url = urls.get(i);
//            try {
//                SeleniumSession session = webviewLogin(browser, url);
//                sessions.add(session);
//            } catch (Exception e) {
//                ErrorUtils.logExceptionFmt(LOGGER, e, "Unable to make WebView login " + browser
//                    + ":" + url);
//            }
//        }
//        LOGGER.info("WebViewLoadSeleniumRunner.startWebViewSessions():: sessions.size = {}",
//            sessions.size());
//        LOGGER.debug("WebViewLoadSeleniumRunner.startWebViewSessions():: sessions      = {}",
//            sessions);
//    }

    private SeleniumSession webviewLogin(String browser, String url) {
        LOGGER.info("Using browser " + browser + " for URL " + url);
        SeleniumSession session = new SeleniumSession(browser, url);
        LOGGER.info("WebViewLoadSeleniumRunner.webviewLogin():: going to open " + session);
        SeleniumHelperBase seleniumHelper = getSeleniumHelper(browser);
        session.sessionId = seleniumHelper.startSession();
        String windowId = seleniumHelper.openUrl(session.sessionId, webviewLoginUrl);
        seleniumHelper.waitForElement(session.sessionId, windowId, SelectionBy.ID, "username", 5);
        seleniumHelper.fillTextField(session.sessionId, windowId, SelectionBy.ID, "username",
            webViewUser);
        if (webViewPassword != null && webViewPassword != "# no password #") {
            seleniumHelper.fillTextField(session.sessionId, windowId, SelectionBy.ID, "j_passWord",
                webViewPassword);
        }
        seleniumHelper.click(session.sessionId, windowId, SelectionBy.ID,
            "webview-loginPage-login-button");
        return session;
    }

//    private void openWebViewUrls() {
//        for (SeleniumSession session : sessions) {
//            try {
//                openWebViewUrl(session);
//            } catch (Exception e) {
//                ErrorUtils.logExceptionFmt(LOGGER, e, "Unable to open WebView session " + session);
//            }
//        }
//    }

    private static String openWebViewUrl(SeleniumSession session) {
        LOGGER.info("WebViewLoadSeleniumRunner.openWebViewUrl():: going to open " + session);
        SeleniumHelperBase seleniumHelper = getSeleniumHelper(session.browser);
        return seleniumHelper.openUrl(session.sessionId, session.url);
    }

//    @SuppressWarnings("unused")
//    private void logoutAndClose() {
//        for (SeleniumSession session : sessions) {
//            try {
//                logoutAndClose(session);
//            } catch (Exception e) {
//                ErrorUtils.logExceptionFmt(LOGGER, e, "Unable to make WebView logout: " + session);
//            }
//        }
//    }
//
//    private void logoutAndClose(SeleniumSession session) {
//        LOGGER.info("WebViewLoadSeleniumRunner.logoutAndClose():: going to open " + session);
//        SeleniumHelperBase seleniumHelper = getSeleniumHelper(session.browser);
//        try {
//            seleniumHelper.openUrl(session.sessionId, logoutUrl);
//        } catch (Exception e) {
//            // don't care, we're just trying to leave quietly
//            LOGGER.info("Unable to open logout url: " + session);
//        }
//        try {
//            seleniumHelper.closeSession(session.sessionId);
//        } catch (Exception e) {
//            // still don't care
//            LOGGER.info("Unable to close session: " + session);
//        }
//    }
//
//    private static Namespace parseArgs(String[] args) {
//        parser =
//            ArgumentParsers
//                .newArgumentParser(WebViewLoadSeleniumRunner.class.getName())
//                .description(
//                    "Runs WebViewLoad using Selenium. It starts 1..n WebView sessions from a list that specifies browser and URL.");
//
//        parser.addArgument("-d", "-data").dest("data").type(String.class).action(Arguments.store())
//            .help("input list of browsers + URLs in CSV format");
//
//        parser.addArgument("-n", "-nodeId").dest("nodeId").type(String.class)
//            .action(Arguments.store()).help("Id of this machone/node");
//
//        parser.addArgument("-f", "-file").dest("file").type(String.class).action(Arguments.store())
//            .help("CSV file containing a list of browsers + URLs");
//
//        parser.addArgument("-m", "-nodeIdColumnIndex").dest("nodeIdColumnIndex")
//            .type(Integer.class).action(Arguments.store())
//            .help("Index of column with nodeId id within a file")
//            .setDefault(DEFAULT_NODEID_COLUMN_INDEX);
//
//        parser.addArgument("-b", "-browserColumnIndex").dest("browserColumnIndex")
//            .type(Integer.class).action(Arguments.store())
//            .help("Index of column with browser id within a file")
//            .setDefault(DEFAULT_BROWSER_COLUMN_INDEX);
//
//        parser.addArgument("-a", "-urlColumnIndex").dest("urlColumnIndex").type(Integer.class)
//            .action(Arguments.store()).help("Index of column with URL within a file")
//            .setDefault(DEFAULT_URL_COLUMN_INDEX);
//
//        parser.addArgument("-s", "-host", "-webviewServerHost").dest("webviewServerHost")
//            .type(String.class).action(Arguments.store()).help("WebView server host")
//            .required(true);
//
//        parser.addArgument("-p", "-port", "-webviewServerPort").dest("webviewServerPort")
//            .type(Integer.class).action(Arguments.store()).help("WebView server port")
//            .required(true);
//
//        parser.addArgument("-u", "-user", "-webViewUser").dest("webViewUser").type(String.class)
//            .action(Arguments.store()).help("WebView credentials - user");
//
//        parser.addArgument("-w", "-password", "-webViewPassword").dest("webViewPassword")
//            .type(String.class).action(Arguments.store()).help("WebView credentials - password");
//
//        parser.addArgument("-x", "-webviewServerHostPlaceholders")
//            .dest("webviewServerHostPlaceholders").type(String.class).action(Arguments.store())
//            .help("A placeholder for WebView server host to be replaced within URLs").nargs("*");
//
//        parser.addArgument("-y", "-webviewServerPortPlaceholders")
//            .dest("webviewServerPortPlaceholders").type(String.class).action(Arguments.store())
//            .help("A placeholder for WebView server port to be replaced within URLs").nargs("*");
//
//        parser.addArgument("-z", "-webviewServerHostPortPlaceholders")
//            .dest("webviewServerHostPortPlaceholders").type(String.class).action(Arguments.store())
//            .help("A placeholder for WebView server host:port to be replaced within URLs")
//            .nargs("*");
//
//        Namespace namespace = parser.parseArgsOrFail(args);
//        LOGGER.debug("WebViewLoadSeleniumRunner.parseArgs():: namespace = " + namespace);
//        return namespace;
//    }

//    private static SortedMap<String, String> getUrlReplacements(String webviewServerHost,
//        Integer webviewServerPort, List<String> webviewServerHostPlaceholders,
//        List<String> webviewServerPortPlaceholders, List<String> webviewServerHostPortPlaceholders) {
//        SortedMap<String, String> urlReplacements = new TreeMap<>();
//        if (webviewServerHostPlaceholders != null) {
//            for (String placeholder : webviewServerHostPlaceholders) {
//                urlReplacements.put(placeholder, webviewServerHost);
//            }
//        }
//        if (webviewServerPortPlaceholders != null) {
//            for (String placeholder : webviewServerPortPlaceholders) {
//                urlReplacements.put(placeholder, "" + webviewServerPort);
//            }
//        }
//        if (webviewServerHostPortPlaceholders != null) {
//            for (String placeholder : webviewServerHostPortPlaceholders) {
//                urlReplacements.put(placeholder, webviewServerHost + ":" + webviewServerPort);
//            }
//        }
//        return urlReplacements;
//    }

    private static void sleep(long sleepTime) {
        try {
            LOGGER.info("WebViewLoadSeleniumRunner.sleep():: sleeping for {} [s]",
                (sleepTime / 1000));
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            LOGGER.debug("WebViewLoadSeleniumRunner.sleep():: InterruptedException");
        }
    }

}
