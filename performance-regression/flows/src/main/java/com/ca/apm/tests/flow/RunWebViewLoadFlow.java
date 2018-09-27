/**
 * 
 */
package com.ca.apm.tests.flow;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.tests.util.selenium.WebViewLoadSeleniumRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map.Entry;

/**
 * @author KEYJA01
 *
 */
public class RunWebViewLoadFlow implements IAutomationFlow {
    private static final Logger logger = LoggerFactory.getLogger(RunWebViewLoadFlow.class);
    
    @FlowContext
    private RunWebViewLoadFlowContext ctx;


    /* (non-Javadoc)
     * @see com.ca.apm.automation.action.core.IAutomationFlow#run()
     */
    @Override
    public void run() throws Exception {
        WebViewLoadSeleniumRunner runner = new WebViewLoadSeleniumRunner(ctx.webviewServerHost, ctx.webviewServerPort, ctx.webViewUser, ctx.webViewPassword);
        for (Entry<String, Collection<String>> entry: ctx.urlBrowserMap.entrySet()) {
            String browser = entry.getKey();
            for (String url: entry.getValue()) {
                logger.info("Adding " + url + ", will open with " + browser);
                runner.addUrl(browser, url);
            }
        }
        
        runner.run();
        runner.waitForShutdown(ctx.shutdownTimeout);
    }

}
