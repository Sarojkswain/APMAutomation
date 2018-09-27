package com.ca.apm.nextgen.tests.common;

/**
 * Enum to identify type of the new element on Management tab of WebView.
 * New elements can be creating using respective combo box.
 *  
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public enum ManagementElementType {
    //New Action
    NEW_CONSOLE_NOTIFICATION_ACTION(WvManagementTabConstants.ELEMENTS_MENU_NEW_ACTION_ID, 
        WvManagementTabConstants.ELEMENTS_MENU_NEW_ACTION_SUBMENU_ID,
        WvManagementTabConstants.NEW_CONSOLE_NOTIFICATION_ACTION_ID),
    
    NEW_SEND_SMTP_MAIL_ACTION(WvManagementTabConstants.ELEMENTS_MENU_NEW_ACTION_ID, 
        WvManagementTabConstants.ELEMENTS_MENU_NEW_ACTION_SUBMENU_ID,
        WvManagementTabConstants.NEW_SEND_SMTP_MAIL_ACTION_ID),
    
    NEW_TRANSACTION_TRACE_ACTION(WvManagementTabConstants.ELEMENTS_MENU_NEW_ACTION_ID, 
        WvManagementTabConstants.ELEMENTS_MENU_NEW_ACTION_SUBMENU_ID,
        WvManagementTabConstants.NEW_TRANSACTION_TRACE_ACTION_ID),
    
    NEW_UIM_ALERT_ACTION(WvManagementTabConstants.ELEMENTS_MENU_NEW_ACTION_ID, 
        WvManagementTabConstants.ELEMENTS_MENU_NEW_ACTION_SUBMENU_ID,
        WvManagementTabConstants.NEW_UIM_ALERT_ACTION_ID),
    
    //New Alert
    NEW_SIMPLE_ALERT(WvManagementTabConstants.ELEMENTS_MENU_NEW_ALERT_ID, 
        WvManagementTabConstants.ELEMENTS_MENU_NEW_ALERT_SUMBEMNU_ID,
        WvManagementTabConstants.NEW_SIMPLE_ALERT_ID), 
    
    NEW_SUMMARY_ALERT(WvManagementTabConstants.ELEMENTS_MENU_NEW_ALERT_ID, 
        WvManagementTabConstants.ELEMENTS_MENU_NEW_ALERT_SUMBEMNU_ID,
        WvManagementTabConstants.NEW_SUMMARY_ALERT_ID),
    
    //New Metric Grouping
    NEW_METRIC_GROUPING(WvManagementTabConstants.ELEMENTS_MENU_NEW_METRIC_GROUPING_ID, 
        null, null),
    //New Differential Control
    NEW_DIFFERENTIAL_CONTROL(WvManagementTabConstants.ELEMENTS_MENU_NEW_DIFFERENTIAL_CONTROL_ID, 
        null, null);
    
    private String menuItemId;
    private String subMenuId;
    private String subMenuItemId;
    
    private ManagementElementType(String menuItemId, String subMenuId, String subMenuItemId) {
        this.menuItemId = menuItemId;
        this.subMenuId = subMenuId;
        this.subMenuItemId = subMenuItemId;
    }

    public String getMenuItemId() {
        return menuItemId;
    }

    public String getSubMenuItemId() {
        return subMenuItemId;
    }
    
    public String getSubMenuId() {
        return subMenuId;
    }
    
}
