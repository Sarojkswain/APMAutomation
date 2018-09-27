/**
 * 
 */
package com.ca.apm.systemtest.fld.testbed.smoke;

import java.util.Arrays;
import java.util.Collection;

import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.WebViewLoadFldTestbedProvider;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @author KEYJA01
 *
 */
@TestBedDefinition
public class WebViewSmokeTestbed implements ITestbedFactory {
    private static final Collection<String> WV_LAXNL_JAVA_OPTION =
        Arrays
            .asList(
                "-Djava.awt.headless=true",
                "-Dorg.owasp.esapi.resources=./config/esapi",
                "-Dsun.java2d.noddraw=true",
                "-Dorg.osgi.framework.bootdelegation=org.apache.xpath",
                "-javaagent:./product/webview/agent/wily/Agent.jar",
                "-Dcom.wily.introscope.agentProfile=./product/webview/agent/wily/core/config/IntroscopeAgent.profile",
                "-Dcom.wily.introscope.wilyForWilyPrefix=com.wily", "-Xms1024m", "-Xmx1024m"
                );

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed tb = new Testbed("WebViewSmokeTestbed");
        
        TestbedMachine emMachine = new TestbedMachine.Builder("emMachine")
            .templateId(ITestbedMachine.TEMPLATE_W64)
            .build();
        
        EmRole emRole = new EmRole.Builder(FLDConstants.EM_WEBVIEW_ROLE_ID, tasResolver)
            .silentInstallChosenFeatures(Arrays.asList("Enterprise Manager", "Database", "WebView"))
            .wvPort(8080)
            .wvLaxNlClearJavaOption(WV_LAXNL_JAVA_OPTION)
            .build();
        
        emMachine.addRole(emRole);
        tb.addMachine(emMachine);
        
        WebViewLoadFldTestbedProvider provider = new WebViewLoadFldTestbedProvider();
        tb.addMachines(provider.initMachines());
        provider.initTestbed(tb, tasResolver);
        
        
        return tb;
    }

}
