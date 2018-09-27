package com.ca.apm.saas.test.utils;

import org.testng.annotations.DataProvider;

public class TestDataProviders {
 
    
    @DataProvider(name = "agentBundleProps") 
    public static Object[][] agentBundleProps() {
        return new Object[][] {
                //Agent         Bundle      App             App Server      <deploy>/  path after       TestResult file name suffix
                { "GlassFish",  "Java",     "StressApp",    "StandAlone",   "agents/GlassFishJava"},    
                { "GlassFish",  "Spring",   "StressApp",    "StandAlone",   "agents/GlassFishSpring"},
                { "JBoss",      "Java",     "StressApp",    "StandAlone",   "agents/JBossJava"},
                { "JBoss",      "Spring",   "StressApp",    "StandAlone",   "agents/JBossSpring"},
                { "Tomcat",     "Java",     "StressApp",    "StandAlone",   "agents/TomcatJava"},
                { "Tomcat",     "Spring",   "StressApp",    "StandAlone",   "agents/TomcatSpring"},
                { "WebLogic",   "Java",     "StressApp",    "StandAlone",   "agents/WebLogicJava"},
                { "WebLogic",   "Spring",   "StressApp",    "StandAlone",   "agents/WebLogicSpring"},
                { "WebSphere",  "Java",     "StressApp",    "StandAlone",   "agents/WebSphereJava"},
                { "WebSphere",  "Spring",   "StressApp",    "StandAlone",   "agents/WebSphereSpring"},   
                { "Java",       "",         "StressApp",    "StandAlone",   "agents/Java"}
        };
    }
    
    @DataProvider(name = "agentDisconnectProps") 
    public static Object[][] agentDisconnectedProps() {
        return new Object[][] {
                //Agent         Bundle      App             App Server      <deploy>/  path after       TestResult file name suffix
                { "GlassFish",  "Java",     "StressApp",    "StandAlone",   "agents/GlassFishJava"}
        };
    }

    @DataProvider(name = "agentIsolationViewProps") 
    public static Object[][] agentIsolationViewProps() {
        return new Object[][] {
               //Card name, Agent name,   Host name,          App Server,				app Name
               { "All Agents", "CA APM Demo Agent", "CA APM Demo Host", "Tomcat", "CA APM Demo Host|Tomcat|CA APM Demo Agent - Tomcat"},
               { "All Agents", "Logstash-APM-Plugin", "Experience Collector Host", "DxC Agent", "Experience Collector Host|DxC Agent|Logstash-APM-Plugin"}
           };
    }
    
    @DataProvider(name = "demoAppParams")
    public static Object[][] demoAppParams() {
        return new Object[][] {
            //Agent name,   Host,          App Server
            { "CA APM Demo Agent", "CA APM Demo Host", "Tomcat"},
            { "Logstash-APM-Plugin", "Experience Collector Host", "DxC Agent"}
        };
    }

    @DataProvider(name = "helpLinkTestParams")
    public static Object[][] helpLinkTestParams() {
        return new Object[][] {
            //WebElement(linkName), article-name(content)
/* DE304693 Agent View Help link doesn't point to View Agent Status and Manage Agent Cards 
 * DE304955  Attributes View Help link doesn't point to Attributes setting content
 * DE306492: Help doc required login inside CA subnet after 7/17 build.
 * NOTE: Add header request in each call as a solution/workaround 
 */
            { "experienceView", "Monitor Performance Using Experience View"},
//            { "agentsView", "View Agent Status and Manage Agent Cards"},
            { "mapView", "View Component Relationships in the Map"},
            { "dashboardView", "Dashboard"},
            { "perspectiveLink", "Create Personal Perspectives"},
            { "universesLink", "Configure Universes"},
//          { "attributesLink", "Attributes"},
            { "agentsLink", "Download an Agent and Start a Transaction Trace"},
            { "securityLink", "APM REST API"},
            { "alertsLink", "Tune Your Monitoring With Alerts"},
            { "notificationsLink", "Notifications"}
        };
    }
    
    @DataProvider(name = "helpIconTestParams")
    public static Object[][] helpIconTestParams() {
        return new Object[][] {
            //WebElement(linkName), article-name(content)
            { "experienceView", "Monitor Performance Using Experience View"},
            { "agentsView", "View Agent Status and Manage Agent Cards"},
            { "mapView", "View Component Relationships in the Map"},
            { "dashboardView", "Dashboard"}
        };
    }
    
    @DataProvider(name = "atcLinkTestParams")
    public static Object[][] atcLinkTestParams() {
        return new Object[][] {
            //linkName, buttonName
            { "experienceView", "Experience View"},
            { "agentsView", "Agents View"},
            { "mapView", "Map"},
            { "dashboardView", "Dashboard"}
        };
    }

    @DataProvider(name = "atcSettingTestParams")
    public static Object[][] atcSettingTestParams() {
        return new Object[][] {
            //linkName, buttonName
            { "perspectiveLink", "Create a Perspective"},
            { "universesLink", "Create universe"},
            { "attributesLink", "Universe: ENTERPRISE"},
            { "attributesLink", "Attribute RuleSet"},
            { "attributesLink", "Upload Attribute Rule File"},
            { "agentsLink", "Download Agent"},
            { "agentsLink", "Trace All Agents"},
            { "securityLink", "Generate New Token"},
            { "alertsLink", "Alert Name"},
            { "notificationsLink", "Create a PagerDuty Notification"}
        };
    }

    @DataProvider(name = "agentInstructionParams") 
    public static Object[][] agentInstructionParams() {
        return new Object[][] {
                //Agent         Bundle      Instruction
                { "GlassFish",  "Java",     "Configure GlassFish to Use the Java Agent"}, 
                { "GlassFish",  "Spring",   "Configure GlassFish to Use the Java Agent"},
                { "JBoss",      "Java",     "Configure JBoss To Use The Java Agent"},
                { "JBoss",      "Spring",   "Configure JBoss To Use The Java Agent"},
                { "Tomcat",     "Java",     "Configure Agent"},
                { "Tomcat",     "Spring",   "Configure Agent"},
                { "WebLogic",   "Java",     "Configure WebLogic To Use The Java Agent"},
                { "WebLogic",   "Spring",   "Configure WebLogic To Use The Java Agent"},
                { "WebSphere",  "Java",     "Configure WebSphere To Use The Java Agent"},
                { "WebSphere",  "Spring",   "Configure WebSphere To Use The Java Agent"},
                { "Java",       "",         "Instrument any Java Application"}
        };
    }

}
