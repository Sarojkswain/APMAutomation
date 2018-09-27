package com.ca.apm.test.atc;


/**
 * Constants for help doc links Selenium tests.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class HelpLinksConstants {
    public static final String HID_DASHBOARD_KEY = "dashboard";
    public static final String HID_MAP_KEY = "map";
    public static final String HID_PERSPECTIVES_KEY = "perspectives";
    public static final String HID_ENTERPRISE_TEAM_CENTER_KEY = "enterpriseTeamCenter";
    public static final String HID_CONFIGURE_TEAM_CENTER_KEY = "configureTeamCenter";
    public static final String HID_CONFIGURE_ENTERPRISE_TEAM_CENTER_KEY = "configureEnterpriseTeamCenter";
    public static final String HID_CONFIGURE_UNIVERSES_KEY = "configureUniverses";
    public static final String HID_TEAM_CENTER_HOME_PAGE_KEY = "teamCenterHomePage";
    public static final String HID_NOTEBOOK_KEY = "notebook";

    public static final String HID_TEAM_CENTER = "HID_APM_Team_Center";
    public static final String HID_DASHBOARD = "HID_Dashboard";
    public static final String HID_MAP = "HID_Map";
    public static final String HID_PERSPECTIVES = "HID_Perspectives";
    public static final String HID_ENTERPRISE_TEAM_CENTER = "HID_Enterprise_Team_Center";
    public static final String HID_CONFIGURE_TEAM_CENTER = "HID_Configure_Team_Center";
    public static final String HID_ATTRIBUTE_RULES = "HID_Attributes";
    public static final String HID_CONFIGURE_ENTERPRISE_TEAM_CENTER = "HID_Configure_Enterprise_Team_Center";
    public static final String HID_CONFIGURE_SECURITY = "HID_EM_API";
    public static final String HID_CONFIGURE_UNIVERSES = "HID_Configure_Universes";
    public static final String HID_TEAM_CENTER_HOME_PAGE = "HID_Team_Center_Experience_View";
    public static final String HID_NOTEBOOK = "HID_Analysis_Notebook";
    public static final String HID_BLUE_BOX_PAGE = "HID_BLUE_BOX_PAGE";
    public static final String HID_CREATE_AND_USE_MANAGEMENT_MODULES = "HID_Create_and_Use_Management_Modules";
    public static final String HID_CREATE_AND_EDIT_DASHBOARDS = "HID_Create_and_edit_dashboards";

    public static final String HELP_LINK_XPATH = "//a[@id='help-link']";
    public static final String HOME_QUICK_LINKS_HELP_LINK_XPATH = "//div[@id='home-quick-links']//help-link//a";
    public static final String HID_KEY_XPATH_PATTERN = "//help-link[@help-id-key=\"%s\"]//a";
    public static final String TEAM_CENTER_HOME_PAGE_HELP_LINK_XPATH = String.format(HID_KEY_XPATH_PATTERN, HID_TEAM_CENTER_HOME_PAGE_KEY);
    public static final String NOTEBOOK_HELP_LINK_XPATH = String.format(HID_KEY_XPATH_PATTERN, HID_NOTEBOOK_KEY);
    public static final String DASHBOARD_HELP_LINK_XPATH = String.format(HID_KEY_XPATH_PATTERN, HID_DASHBOARD_KEY);
    public static final String MAP_HELP_LINK_XPATH = String.format(HID_KEY_XPATH_PATTERN, HID_MAP_KEY);
    public static final String PERSPECTIVES_HELP_LINK_XPATH = String.format(HID_KEY_XPATH_PATTERN, HID_PERSPECTIVES_KEY);
    public static final String ENTERPRISE_TEAM_CENTER_HELP_LINK_XPATH = String.format(HID_KEY_XPATH_PATTERN, HID_ENTERPRISE_TEAM_CENTER_KEY);
    public static final String CONFIGURE_TEAM_CENTER_HELP_LINK_XPATH = String.format(HID_KEY_XPATH_PATTERN, HID_CONFIGURE_TEAM_CENTER);
    public static final String CONFIGURE_ENTERPRISE_TEAM_CENTER_HELP_LINK_XPATH = String.format(HID_KEY_XPATH_PATTERN, HID_CONFIGURE_ENTERPRISE_TEAM_CENTER_KEY);
    public static final String CONFIGURE_UNIVERSES_HELP_LINK_XPATH = String.format(HID_KEY_XPATH_PATTERN, HID_CONFIGURE_UNIVERSES_KEY);
    
    public static final String HELP_LINK_PATTERN         = "^https:\\/\\/docops\\.ca\\.com\\/rest\\/ca\\/product\\/latest\\/topic\\?hid=(.*)&space=(APMDEVOPS.*)&format=rendered&language=(.*)$";
    public static final String WEBVIEW_HELP_LINK_PATTERN = "^https:\\/\\/docops\\.ca\\.com\\/rest\\/ca\\/product\\/latest\\/topic\\?hid=(.*)&space=(APMDEVOPS.*)&language=(.*)&format=rendered$";
    
    //
    public static final String DEV_SPACE_SUFFIX = "DEV";
    public static final String FEATURE_BRANCH_RELEASE_VERSION = "99.99.some_feature_branc_hname";
    public static final String UNKNOWN_RELEASE_VERSION = "unknown";
    public static final String VARIABLE_PLACEHOLDER_VERSION = "${apm.release.version}";
    public static final String TEST_RELEASE_VERSION = "10.4.0.21";
    public static final String EXPECTED_SPACE = "APMDEVOPS104";
    public static final String DEV_SPACE = "APMDEVOPSDEV";
    public static final String DEFAULT_LANG = "";
    public static final String HY_LANG = "hy";
    public static final String CZ_LANG = "cz";
    public static final String EN_LANG = "en";
    public static final String KO_LANG = "ko";
    public static final String JA_LANG = "ja";
    public static final String ZH_LANG = "zh";
    public static final String ZH_TW1_LANG = "zh-tw";
    public static final String ZH_TW2_LANG = "zh_tw";
    public static final String ZH_TW3_LANG = "zh-TW";
    public static final String EXPECTED_JP_LANG = "jp";
    public static final String EXPECTED_KR_LANG = "kr";
    public static final String EXPECTED_CN_LANG = "cn";
    public static final String EXPECTED_TW_LANG = "tw";
    		
    public static final String RELEASE_STRING = "RELEASE_STRING";
    public static final String DOCOPS_RELEASE_STRING = "DOCOPS_RELEASE_STRING";
}
