package com.ca.apm.tests.role;

import com.ca.apm.tests.flow.RunWebViewLoadFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import org.apache.http.util.Args;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class WebViewLoadRole extends AbstractRole {

    public final static String ENV_WEBVIEW_LOAD_START = "startWebViewLoad";
    public final static String ENV_WEBVIEW_LOAD_STOP = "stopWebViewLoad";

//    private final String workDir;
//    private final String javaHome;
//
//    private final String webviewServerHost;
//    private final int webviewServerPort;
//
//    private final String webViewUser;
//    private final String webViewPassword;
//
//    private final String inputFileName;
//    private final int browserColumnIndex;
//    private final int urlColumnIndex;
//    private final String nodeId;
//    private final int nodeIdColumnIndex;
//    private Map<String, Collection<String>> urlBrowserMap;

    protected WebViewLoadRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
//        this.workDir = builder.workDir;
//        this.javaHome = builder.javaHome;
//        this.webviewServerHost = builder.webviewServerHost;
//        this.webviewServerPort = builder.webviewServerPort;
//        this.webViewUser = builder.webViewUser;
//        this.webViewPassword = builder.webViewPassword;
//        this.nodeId = builder.nodeId;
//        this.inputFileName = builder.inputFileName;
//        this.nodeIdColumnIndex = builder.nodeIdColumnIndex;
//        this.browserColumnIndex = builder.browserColumnIndex;
//        this.urlColumnIndex = builder.urlColumnIndex;
//        this.urlBrowserMap = builder.urlBrowserMap;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        // we don't have to deploy anything - cool, eh!
    }

    public static class Builder extends BuilderBase<Builder, WebViewLoadRole> {
        private String roleId;
        private static final int DEFAULT_WEBVIEW_SERVER_PORT = 8080;
        private static final String DEFAULT_WEBVIEW_SERVER_USER = "cemadmin";
        private static final String DEFAULT_WEBVIEW_SERVER_PASSWORD = "quality";

//        private static final int DEFAULT_NODEID_COLUMN_INDEX = 0;
//        private static final int DEFAULT_BROWSER_COLUMN_INDEX = 1;
//        private static final int DEFAULT_URL_COLUMN_INDEX = 2;

        private String workDir = getDeployBase() + "webview-load";

        private String webviewServerHost;
        private int webviewServerPort = DEFAULT_WEBVIEW_SERVER_PORT;

        private String webViewUser = DEFAULT_WEBVIEW_SERVER_USER;
        private String webViewPassword = DEFAULT_WEBVIEW_SERVER_PASSWORD;


        private Map<String, Collection<String>> urlBrowserMap = new HashMap<>();
        private Long shutdownTimeout;
//        private List<String> browsers = new ArrayList<>();
//        private List<String> urls = new ArrayList<>();

        public Builder(String roleId, ITasResolver tasResolver) {
            super();
            this.roleId = roleId;
        }

        
        @Override
        public WebViewLoadRole build() {
            Args.notBlank(workDir, "workDir");
            Args.notBlank(webviewServerHost, "webviewServerHost");
            
            RunWebViewLoadFlowContext.Builder b = RunWebViewLoadFlowContext.getBuilder()
                .webviewServerHost(webviewServerHost)
                .webviewServerPort(webviewServerPort)
                .webViewUser(webViewUser)
                .webViewPassword(webViewPassword)
                .urlBrowserMap(urlBrowserMap);
            if (shutdownTimeout != null) {
                b.shutdownTimeout(shutdownTimeout);
            }
                
            getEnvProperties().add(ENV_WEBVIEW_LOAD_START, b.build());
            WebViewLoadRole webViewLoadRole = getInstance();
            
            return webViewLoadRole;
        }
        
        @Override
        protected Builder builder() {
            return this;
        }
        
        public Builder shutdownTimeout(long timeout) {
            this.shutdownTimeout = timeout;
            return this;
        }

        @Override
        protected WebViewLoadRole getInstance() {
            WebViewLoadRole role = new WebViewLoadRole(this);
            return role;
        }

        public Builder workDir(String workDir) {
            Args.notBlank(workDir, "workDir");
            this.workDir = workDir;
            return builder();
        }

        public Builder webViewServerHost(String webviewServerHost) {
            Args.notBlank(webviewServerHost, "WebView server host");
            this.webviewServerHost = webviewServerHost;
            return builder();
        }

        public Builder webViewServerPort(int webviewServerPort) {
            Args.positive(webviewServerPort, "WebView server port");
            this.webviewServerPort = webviewServerPort;
            return builder();
        }

        public Builder webViewCredentials(String webViewUser, String webViewPassword) {
            this.webViewUser = webViewUser;
            this.webViewPassword = webViewPassword;
            return builder();
        }

        public Builder openWebViewUrl(String url) {
            return openWebViewUrl("chrome", url);
        }

        public Builder openWebViewUrl(String browser, String url) {
            Args.notBlank(browser, "Specified browser");
            Args.notBlank(url, "Specified url");
            
            Collection<String> coll = urlBrowserMap.get(browser);
            if (coll == null) {
                coll = new ArrayList<>();
                urlBrowserMap.put(browser, coll);
            }
            coll.add(url);
            
//            inputFileName = null;
//            browserColumnIndex = -1;
//            urlColumnIndex = -1;
            return builder();
        }

//        public Builder readDataFromFile(String inputFileName) {
//            Args.notBlank(inputFileName, "Specified file name");
//            this.inputFileName = inputFileName;
//            browsers.clear();
//            urls.clear();
//            return builder();
//        }
//
//        public Builder readDataFromFile(String inputFileName, int browserColumnIndex,
//            int urlColumnIndex) {
//            Args.notBlank(inputFileName, "Specified file name");
//            Args.notNegative(browserColumnIndex, "browserColumnIndex");
//            Args.notNegative(urlColumnIndex, "urlColumnIndex");
//            this.inputFileName = inputFileName;
//            this.browserColumnIndex = browserColumnIndex;
//            this.urlColumnIndex = urlColumnIndex;
//            browsers.clear();
//            urls.clear();
//            return builder();
//        }

//        public Builder nodeId(String nodeId) {
//            this.nodeId = nodeId;
//            return builder();
//        }
//
//        public Builder nodeId(String nodeId, int nodeIdColumnIndex) {
//            Args.notNegative(nodeIdColumnIndex, "nodeIdColumnIndex");
//            this.nodeId = nodeId;
//            this.nodeIdColumnIndex = nodeIdColumnIndex;
//            return builder();
//        }
    }

}
