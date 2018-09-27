package com.ca.apm.tests.cem.common;

import com.ca.tas.artifact.IBuiltArtifact.Version;
import com.ca.tas.builder.TasBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_CO66;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

public class CEMConstants {
    
    protected static final Logger LOGGER = LoggerFactory.getLogger(CEMConstants.class);


    public static final String EM_ROLE_ID = "emRole";
    public static final String CLW_ROLE_ID = "clwRole";
    
    public static final String EM_MACHINE_ID = "emMachine";

    public static final String IE_SELENIUM_DRIVER_ROLE_ID = "seleniumDriverRoleIE";
    public static final String CHROME_SELENIUM_DRIVER_ROLE_ID = "seleniumDriverRoleChrome";

    public static final String MOM_ROLE_ID = "momRole";
    public static final String COLLECTOR1_ROLE_ID = "collector1Role";
    public static final String EM_MACHINE_TEMPLATE_ID = TEMPLATE_W64;


    public static final String TIM_MACHINE_ID = "timMachine";
    public static final String TIM_MACHINE_TEMPLATE_ID = TEMPLATE_CO66;

    public static final String WEBDRIVER_ROLE_ID = "webDriverRole";
    public static final String seleniumDriverHome = TasBuilder.WIN_SOFTWARE_LOC;
    public static final String JAVA7_LINUX_ROLE_ID = "java7LinuxRole";
    public static final String TIM_ROLE_ID = "timRole";
    public static final String TIM_ATTENDEE_LIN_ROLE_ID = "timAttendeeRoleId";

    public static final Version VERSION = Version.SNAPSHOT_SYS_99_99;

    public static String ieSeleniumDriver = "IEDriverServer.exe";
    public static String chromeSeleniumDriver = "chromedriver.exe";

    public static final String JAVA7_LINUX_HOME = "/opt/automation/java1.7";

    public static String MEDREC_HOSTNAME, EM_HOSTNAME, CLIENT_HOSTNAME, TIM_HOSTNAME;
    public static final String BTS_ZIP_ROLE_ID = "btsZipFole";
    public static final String BTS_ZIP_VERSION = "1.0.0";
    public static final String BTS_LOC = "c:/sw/client/testdata/GeneralApplication/";


}
