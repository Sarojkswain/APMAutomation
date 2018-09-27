package com.ca.apm.nextgen.tests.common;

/**
 * UI constants used on the 'Management' tab of WebView UI. 
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class WvManagementTabConstants {
    //Common test constants
    public static final String ALERT_NODE_TEMPLATE = "management-tree_*SuperDomain*|Management Modules|%s|Alerts|%s";
    public static final String ACTION_NODE_TEMPLATE = "management-tree_*SuperDomain*|Management Modules|%s|Actions|%s";
    
    public static final String CONSOLE_NOTIFICATION_ACTION = "Console Notification Action";
    public static final String AQUARIUS_SUMMARY_ALERT_NAME = "Aquarius Summary Alert";
    public static final String AGENT_CONNECTION_STATUS_ALERT_NAME = "Agent Connection Status";
    public static final String TEST_SAMPLE_MANAGEMENT_MODULE_NAME = "TestSample";
    public static final String TEST_SAMPLE_METRIC_GROUPINGS_NODE = "management-tree_*SuperDomain*|Management Modules|TestSample|Metric Groupings";
    public static final String TEST1MM_MANAGEMENT_MODULE_NAME = "Test1MM";
    public static final String BOO1_ACTION_NAME = "Boo1";
    public static final String TEST2MM_MANAGEMENT_MODULE_NAME = "Test2MM";
    public static final String BOGEY1_ACTION_NAME = "Bogey1";
    public static final String TEST_ASTERISK_MM_MODULE_NAME_PATTERN = "Test*MM";
    
    public static final String SUPER_DOMAIN_NODE = "management-tree_*SuperDomain*";
    public static final String SUPER_DOMAIN_NODE_NAME = "*SuperDomain*";
    
    public static final String MM_SEARCH_INPUT_ID = "webview-investigator-mmsearch-textfield-search-input";
    public static final String MM_SEARCH_CONTENT_PANEL_GRID_ID = "webview-investigator-mmsearch-contentpanel-grid";
    public static final String MM_SEARCH_GRID_ID = "webview-investigator-mmsearch-grid";
    
    public static final String MM_SEARCH_GO_BUTTON_ID = "webview-investigator-mmsearch-button-go";
    
    //Elements drop down button
    public static final String ELEMENTS_DROP_DOWN_BUTTON_ID = "webview-MMEditor-elements-splitButton";
    public static final String ELEMENTS_MENU_DROP_DOWN_ID = "webview-MMEditor-elements-menu";
    
    //Elements menu items
    public static final String ELEMENTS_MENU_NEW_ACTION_ID = "webview-MMEditor-newAction-menuItem";
    public static final String ELEMENTS_MENU_NEW_ALERT_ID = "webview-MMEditor-newAlert-menuItem";
    public static final String ELEMENTS_MENU_NEW_METRIC_GROUPING_ID = "webview-MMEditor-newMG-menuItem";
    public static final String ELEMENTS_MENU_NEW_DIFFERENTIAL_CONTROL_ID = "webview-MMEditor-newBaseline-menuItem";
    
    //Elements menu item's submenu
    public static final String ELEMENTS_MENU_NEW_ACTION_SUBMENU_ID = "webview-MMEditor-action-menu";
    public static final String ELEMENTS_MENU_NEW_ALERT_SUMBEMNU_ID = "webview-MMEditor-alert-menu";
    
    //Elements menu item's sumbmenu items
    public static final String NEW_CONSOLE_NOTIFICATION_ACTION_ID = "webview-MMEditor-newConsoleNotificationAction-menuItem";
    public static final String NEW_SEND_SMTP_MAIL_ACTION_ID = "webview-MMEditor-newSendSmtpMailAction-menuItem";
    public static final String NEW_TRANSACTION_TRACE_ACTION_ID = "webview-MMEditor-newTrxTraceAction-menuItem";
    public static final String NEW_UIM_ALERT_ACTION_ID = "webview-MMEditor-newUIMAlertAction-menuItem";
    
    public static final String NEW_SIMPLE_ALERT_ID = "webview-MMEditor-newSimpleAlert-menuItem";
    public static final String NEW_SUMMARY_ALERT_ID = "webview-MMEditor-newSummaryAlert-menuItem";

    //Create new element dialog ids
    public static final String NEW_ELEMENT_CREATION_DIALOG_PANEL_ID = "webview-elementCreationPanel-verticalLayout-layout";
    public static final String NEW_ELEMENT_NAME_FIELD_ID = "webview-mmEditor-Element-Name-Field-input";
    public static final String NEW_ELEMENT_MANAGEMENT_MODULE_COMBO_BOX_ID = "webview-elementCreationPanel-Element-Construct-Combo-input";
    public static final String NEW_ELEMENT_FORCE_UNIQUENESS_CHECK_BOX_ID = "webview-mmEditor-Element-ForceUnique-Check";
    public static final String NEW_ELEMENT_CREATION_DIALOG_OK_BUTTON_ID = "webview-mmEditor-Element-OK-Button";

    //Management module editor form
    public static final String MM_EDITOR_DELETE_BUTTON_ID = "webview-MMEditor-buttonBar-delete-button";
    public static final String MM_EDITOR_APPLY_BUTTON_ID = "webview-MMEditor-buttonBar-apply-button";
    public static final String MM_EDITOR_NAME_FIELD_INPUT_ID = "webview-mmEditor-mmeditorContent-name-field-input";
    public static final String MM_EDITOR_ADD_ACTION_BUTTON_ID = "webview-MMEditor-aEditor-danger-add-button";
    public static final String MM_EDITOR_REMOVE_ACTION_BUTTON_ID = "webview-MMEditor-aEditor-danger-remove-button";
    public static final String MM_EDITOR_ACTIVE_CECK_BOX_NAME = "webview-mmEditor-mmeditorContent-active-check";
    public static final String MM_EDITOR_ACTIONS_LIST_ID = "webview-MMEditor-aEditor-danger-actions-field";
    
    //Choose danger action dialog
    public static final String CHOOSE_ACTION_DIALOG_GRID_ID = "webview-MMEditor-aEditor-cAction-Construct-Grid";
    public static final String CHOOSE_BUTTON_ID = "webview-MMEditor-aEditor-Choose-Button";
    
    //Confirmation dialog
    public static final String CONFIRMATION_DIALOG_ID = "webview-Common-ConfirmMessageBox";
    public static final String CONFIRMATION_DIALOG_NO_BUTTON_ID = "webview-Common-ConfirmMessageBox-button-no";
    public static final String CONFIRMATION_DIALOG_YES_BUTTON_ID = "webview-Common-ConfirmMessageBox-button-yes";
    public static final String CONFIRMATION_DIALOG_MESSAGE_ID = "webview-Common-ConfirmMessageBox-message";
    
    //Alert box
    public static final String ALERT_MESSAGE_BOX_ID = "webview-Common-AlertMessageBox";
    public static final String MM_EDITOR_ALERT_MESSAGE_BOX_ID = "webview-MMEditor-aEditor-alert-mbox";
    public static final String ALERT_MESSAGE_BOX_HEADER_ID = "webview-Common-AlertMessageBox-header";
    public static final String ALERT_MESSAGE_BOX_MESSAGE_ID = "webview-Common-AlertMessageBox-message";
    public static final String ALERT_MESSAGE_BOX_OK_BUTTON_ID = "webview-Common-AlertMessageBox-button-ok";

    public static final String SIMPLE_ALERT_MUST_SPECIFY_NON_EMPTY_NAME_ALERT_MESSAGE = "The Simple Alert must specify a non-empty name.";
    public static final String FIELD_MUST_HAVE_A_VALUE_ALERT_MESSAGE = "Did not apply changes. Cannot proceed because a field must have a value.";
    
    //Threshold filed ids
    public static final String DANGER_THRESHOLD_TEXT_INPUT_ID = "webview-MMEditor-aEditor-danger-threshold-text-input";
    public static final String CAUTION_THRESHOLD_TEXT_INPUT_ID = "webview-MMEditor-aEditor-caution-threshold-text-input";
    
    public static final String DANGER_ACTION_LIST_GRID_ID = "webview-MMEditor-aEditor-danger-actions-Grid";
        
}
