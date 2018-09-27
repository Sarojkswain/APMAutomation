/**
 * 
 */
package com.ca.apm.systemtest.fld.testbed.smoke;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import com.ca.apm.systemtest.fld.flow.ConfigureTessFlowContext.PreferredBrowser;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.TIMRole;
import com.ca.tas.role.seleniumgrid.BrowserType;
import com.ca.tas.role.seleniumgrid.NodeCapability;
import com.ca.tas.role.seleniumgrid.NodeConfiguration;
import com.ca.tas.role.seleniumgrid.NodePlatform;
import com.ca.tas.role.seleniumgrid.SeleniumGridHubRole;
import com.ca.tas.role.seleniumgrid.SeleniumGridNodeRole;
import com.ca.tas.role.tess.ConfigureTessRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Testbed for {@link ConfigureTessRole} smoke tests.
 * 
 * 
 * @author keyja01
 * @author sinal04
 *
 */
@TestBedDefinition
public class ConfigureTessSmokeSmallerTestbed implements ITestbedFactory {
	public static final String EM_MACHINE_ID = "em";
	public static final String TIM_MACHINE_ID = "cem";
	public static final String SELENIUM_GRID_HUB_MACHINE_ID = "seleniumGridHubMachine";
	public static final String SELENIUM_GRID_HUB_ROLE_ID = "seleniumGridHubRole";
	public static final String SELENIUM_GRID_NODE_ROLE_ID = "seleniumGridNodeRole";
	public static final String EM_ROLE_ID = "emRole";
	public static final String CONFIGURE_TESS_ROLE_ID = "configureTess";
	public static final String TIM_ROLE_ID = "tim";

	public static final String TEST_SMTP_HOST = "mail.ca.com";
	public static final String TEST_REPORT_EMAIL = "barmaley@ca.com";
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ca.tas.testbed.ITestbedFactory#create(com.ca.tas.resolver.ITasResolver
	 * )
	 */
	@Override
	public ITestbed create(ITasResolver tasResolver) {
		Testbed testbed = new Testbed(this.getClass().getSimpleName());

		TestbedMachine emMachine = new TestbedMachine.Builder(EM_MACHINE_ID).templateId(
				ITestbedMachine.TEMPLATE_W64).build();
		EmRole emRole = new EmRole.Builder(EM_ROLE_ID, tasResolver)
				.silentInstallChosenFeatures(Arrays.asList("Enterprise Manager", "Database"))
				.nostartWV()
				.build();

		emMachine.addRole(emRole);
		SeleniumGridNodeRole seleniumGridNodeRole = createSeleniumNodeRole(tasResolver);
		emMachine.addRole(seleniumGridNodeRole);
		
		TestbedMachine seleniumGridHubMachine = new TestbedMachine.Builder(SELENIUM_GRID_HUB_MACHINE_ID)
			.templateId(ITestbedMachine.TEMPLATE_W64)
			.build();

		SeleniumGridHubRole seleniumGridHubRole = new SeleniumGridHubRole.Builder(SELENIUM_GRID_HUB_ROLE_ID, 
				tasResolver).addNodeRole(seleniumGridNodeRole).build();
		seleniumGridHubRole.after(seleniumGridNodeRole);
		seleniumGridHubMachine.addRole(seleniumGridHubRole);
		
		String hubHost = tasResolver.getHostnameById(SELENIUM_GRID_HUB_ROLE_ID);


		TestbedMachine timMachine = new TestbedMachine.LinuxBuilder(TIM_MACHINE_ID)
				.templateId("co65_tim").build();
		TIMRole timRole = new TIMRole.Builder(TIM_ROLE_ID, tasResolver).installDir(
				"/opt").build();

		
        String hubUrl = "http://" + hubHost + ":4444/wd/hub";

		ConfigureTessRole configureTessRole = new ConfigureTessRole.Builder(CONFIGURE_TESS_ROLE_ID, tasResolver)
			.autostart()
			.reportEmail(TEST_REPORT_EMAIL)
			.smtpHost(TEST_SMTP_HOST)
			.mom(emRole)
			.tim(timRole)
			.preferredBrowser(PreferredBrowser.Firefox)
			.seleniumGridHubHostAndPort(hubUrl)
			.build();

		configureTessRole.after(emRole, timRole, seleniumGridHubRole);

		emMachine.addRole(configureTessRole);

		timMachine.addRole(timRole);
		testbed.addMachine(timMachine, emMachine, seleniumGridHubMachine);

		return testbed;
	}

	private SeleniumGridNodeRole createSeleniumNodeRole(ITasResolver tasResolver) {
		String hubHost = tasResolver.getHostnameById(SELENIUM_GRID_HUB_ROLE_ID);

		URL hubRegisterUrl = null;
        try {
            hubRegisterUrl = new URL("http://" + hubHost + ":4444/grid/register/");
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }

		NodeCapability firefoxCapability = new NodeCapability.Builder()
			.browserType(BrowserType.FIREFOX)
			.platform(NodePlatform.WINDOWS)
			.maxInstances(10)
			.build();

		NodeCapability internetExplorerCapability = new NodeCapability.Builder()
			.browserType(BrowserType.INTERNET_EXPLORER)
			.platform(NodePlatform.WINDOWS)
			.maxInstances(10)
			.build();

		NodeCapability chromeCapability = new NodeCapability.Builder()
			.browserType(BrowserType.CHROME)
			.platform(NodePlatform.WINDOWS)
			.maxInstances(10)
			.build();

		NodeConfiguration nodeConfiguration = new NodeConfiguration.Builder()
			.addCapability(firefoxCapability)
			.addCapability(internetExplorerCapability)
			.addCapability(chromeCapability)
			.maxSession(100)
			.register(true)
			.hub(hubRegisterUrl)
			.build();

		return new SeleniumGridNodeRole.Builder(SELENIUM_GRID_NODE_ROLE_ID, tasResolver)
			.nodeConfiguration(nodeConfiguration).build();
	}

}
