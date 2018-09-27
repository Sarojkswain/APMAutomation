package com.ca.apm.systemtest.fld.scripts;

import java.util.Map;

import com.ca.apm.systemtest.fld.plugin.selenium.SeleniumPlugin;
import com.ca.apm.systemtest.fld.plugin.selenium.SeleniumPlugin.SelectionBy;
import com.ca.apm.systemtest.fld.plugin.selenium.SeleniumPluginException;
import com.ca.apm.systemtest.fld.plugin.selenium.SeleniumTest;

public class CEMSetWebServerFilters implements SeleniumTest {

    @Override
    public void executeSeleniumScript(SeleniumPlugin plugin, Map<String, String> params) throws SeleniumPluginException {
        String sessionId = plugin.startSession();
        String windowId = plugin.openUrl(sessionId, "http://" + params.get("tesshost") + ":8081/wily/cem/tess/app/admin/webServerList.html");
        plugin.fillTextField(sessionId, windowId, SelectionBy.ID, "loginForm:loginId_userName", params.get("userName"));
        String pass = "";
        if (params.containsKey("userPass")) {
            pass = params.get("userPass");
        }
        plugin.fillTextField(sessionId, windowId, SelectionBy.ID, "loginForm:loginId_passWord", pass);
        plugin.click(sessionId, windowId, SelectionBy.ID, "loginForm:loginId_loginButton");
    }

    @Override
    public void shouldStop() {
        // this is a short script, should stop pretty fast anyway
    }

}
