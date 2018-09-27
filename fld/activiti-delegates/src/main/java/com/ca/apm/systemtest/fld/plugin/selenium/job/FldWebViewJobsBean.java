/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.selenium.job;

import java.util.HashMap;
import java.util.HashSet;

import org.activiti.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.selenium.ChromeSeleniumPlugin;
import com.ca.apm.systemtest.fld.plugin.selenium.FirefoxSeleniumPlugin;
import com.ca.apm.systemtest.fld.plugin.selenium.IESeleniumPlugin;
import com.ca.apm.systemtest.fld.plugin.selenium.SeleniumPlugin;
import com.ca.apm.systemtest.fld.plugin.selenium.SeleniumPlugin.SelectionBy;
import com.ca.apm.systemtest.fld.proxy.AgentProxy;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * @author keyja01
 *
 */
@Component("fldWebViewJobs")
public class FldWebViewJobsBean implements InitializingBean {
    public static final String SELENIUM_SESSION_KEYS = "seleniumSessionKeys";
    public static final String SELENIUM_SESSIONS = "seleniumSessions";
    private static final String SESSION_KEY = "sessionKey";
    private String[] nodes = new String[] {"wv01", "wv02", "wv03", "wv04", "wv05", "wv06"};
    
    @Autowired
    private AgentProxyFactory agentProxyFactory;
    
    @Autowired
    private NodeManager nodeManager;
    
    private StartWebViewSession loginDelegate;
    private OpenWebViewUrlDelegate openPageDelegate;
    private LogoutAndCloseDelegate logoutAndCloseDelegate;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        loginDelegate = new StartWebViewSession(nodeManager, agentProxyFactory);
        openPageDelegate = new OpenWebViewUrlDelegate(nodeManager, agentProxyFactory);
        logoutAndCloseDelegate = new LogoutAndCloseDelegate(nodeManager, agentProxyFactory);
    }
    
    
    public class LogoutAndCloseDelegate extends AbstractJavaDelegate {

        public LogoutAndCloseDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            String sessionKey = getStringExecutionVariable(execution, SESSION_KEY);
            
            @SuppressWarnings("unchecked")
            HashMap<String, SeleniumSession> sessions = (HashMap<String, SeleniumSession>) getExecutionVariableObject(execution, "seleniumSessions");
            SeleniumSession session = sessions.get(sessionKey);
            if (session == null) {
                return;
            }
            
            boolean available = nodeManager.checkNodeAvailable(session.node);
            if (!available) {
                return;
            }
            
            AgentProxy proxy = agentProxyFactory.createProxy(session.node);
            
            SeleniumPlugin plugin = proxy.getPlugin("seleniumPluginFirefox", FirefoxSeleniumPlugin.class);
            if (session.browser.equals("chrome")) {
                plugin = proxy.getPlugin("seleniumPluginChrome", ChromeSeleniumPlugin.class);
            } else if (session.browser.equals("ie")) {
                plugin = proxy.getPlugin("seleniumPluginIE", IESeleniumPlugin.class);
            }
            
            String webViewNode = getStringExecutionVariable(execution, "webViewNode");
            String logoutUrl = "http://" + webViewNode + ":8080/logout";
            
            try {
                plugin.openUrl(session.sessionId, logoutUrl);
            } catch (Exception e) {
                // don't care, we're just trying to leave quietly
            }
            try {
                plugin.closeSession(session.sessionId);
            } catch (Exception e) {
                // still don't care
            }
        }
        
    }
    
    
    /**
     * Opens the URL in a selenium session
     * @author keyja01
     *
     */
    public class OpenWebViewUrlDelegate extends AbstractJavaDelegate {

        public OpenWebViewUrlDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }
        
        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            String sessionKey = getStringExecutionVariable(execution, SESSION_KEY);
            
            @SuppressWarnings("unchecked")
            HashMap<String, SeleniumSession> sessions = (HashMap<String, SeleniumSession>) getExecutionVariableObject(execution, "seleniumSessions");
            SeleniumSession session = sessions.get(sessionKey);
            
            AgentProxy proxy = agentProxyFactory.createProxy(session.node);
            
            SeleniumPlugin plugin = proxy.getPlugin("seleniumPluginFirefox", FirefoxSeleniumPlugin.class);
            if (session.browser.equals("chrome")) {
                plugin = proxy.getPlugin("seleniumPluginChrome", ChromeSeleniumPlugin.class);
            } else if (session.browser.equals("ie")) {
                plugin = proxy.getPlugin("seleniumPluginIE", IESeleniumPlugin.class);
            }
            
            plugin.openUrl(session.sessionId, session.url);
        }
    }
    
    /**
     * Delegate which creates a new selenium session and logs into the WebView server.
     * @author keyja01
     *
     */
    public class StartWebViewSession extends AbstractJavaDelegate {

        public StartWebViewSession(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            String tmpNodeDefinitionString = getStringExecutionVariable(execution, "tmpNodeDefinitionStr");
            String[] arr = tmpNodeDefinitionString.split(",");
            
            SeleniumSession session = new SeleniumSession();
            session.browser = arr[1];
            
            String targetNode = arr[0];
            if (targetNode == null) {
                return;
            }
            targetNode = targetNode.toLowerCase().trim();
            for (String node: nodes) {
                if (node.equals(targetNode)) {
                    session.node = getStringExecutionVariable(execution, node + "Node");
                    break;
                }
            }
            if (session.node == null) {
                return;
            }
            String webViewNode = getStringExecutionVariable(execution, "webViewNode");
            String targetUrl = arr[2].replace("WEBVIEW_HOST_NAME", webViewNode);
            session.url = targetUrl;
            
            boolean available = nodeManager.checkNodeAvailable(session.node);
            if (!available) {
                return;
            }
            
            AgentProxy proxy = agentProxyFactory.createProxy(session.node);
            
            SeleniumPlugin plugin = proxy.getPlugin("seleniumPluginFirefox", FirefoxSeleniumPlugin.class);
            if (session.browser.equals("chrome")) {
                plugin = proxy.getPlugin("seleniumPluginChrome", ChromeSeleniumPlugin.class);
            } else if (session.browser.equals("ie")) {
                plugin = proxy.getPlugin("seleniumPluginIE", IESeleniumPlugin.class);
            }
            
            session.sessionId = plugin.startSession();
            execution.setVariable(SESSION_KEY, session.node + ":" + session.sessionId);
            
            @SuppressWarnings("unchecked")
            HashMap<String, SeleniumSession> sessions = (HashMap<String, SeleniumSession>) getExecutionVariableObject(execution, SELENIUM_SESSIONS);
            sessions.put(session.node + ":" + session.sessionId, session);
            execution.setVariable(SELENIUM_SESSIONS, sessions);
            HashSet<String> keys = new HashSet<>(sessions.keySet());
            execution.setVariable(SELENIUM_SESSION_KEYS, keys);
            
            String windowId = plugin.openUrl(session.sessionId, "http://" + webViewNode + ":8080/jsp/login.jsf");
            
            String webViewUser = getStringExecutionVariable(execution, "webViewUser");
            String webViewPassword = getStringExecutionVariable(execution, "webViewPassword");
            
            plugin.waitForElement(session.sessionId, windowId, SelectionBy.ID, "username", 5);
            plugin.fillTextField(session.sessionId, windowId, SelectionBy.ID, "username", webViewUser);
            
            
            if (webViewPassword != null && webViewPassword != "# no password #") {
                plugin.fillTextField(session.sessionId, windowId, SelectionBy.ID, "j_passWord", webViewPassword);
            }
            plugin.click(session.sessionId, windowId, SelectionBy.ID, "webview-loginPage-login-button");
        }
    }

    public StartWebViewSession getLoginDelegate() {
        return loginDelegate;
    }

    public void setLoginDelegate(StartWebViewSession loginDelegate) {
        this.loginDelegate = loginDelegate;
    }

    public OpenWebViewUrlDelegate getOpenPageDelegate() {
        return openPageDelegate;
    }

    public void setOpenPageDelegate(OpenWebViewUrlDelegate openPageDelegate) {
        this.openPageDelegate = openPageDelegate;
    }

    public LogoutAndCloseDelegate getLogoutAndCloseDelegate() {
        return logoutAndCloseDelegate;
    }

    public void setLogoutAndCloseDelegate(LogoutAndCloseDelegate logoutAndCloseDelegate) {
        this.logoutAndCloseDelegate = logoutAndCloseDelegate;
    }
}
