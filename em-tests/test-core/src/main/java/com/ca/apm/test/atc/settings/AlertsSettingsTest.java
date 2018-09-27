package com.ca.apm.test.atc.settings;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.AlertSettingsPage;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.UI.Role;
import com.wily.introscope.appmap.rest.entities.Alert;
import com.wily.introscope.appmap.rest.entities.AlertMetricGrouping;
import com.wily.introscope.appmap.rest.entities.AlertThreshold;

public class AlertsSettingsTest extends UITest {

    private UI ui;
    private AlertSettingsPage alertSettings;

    private Alert alert = getFrontentErrorsAlert();
    private String alertName = alert.getName();


    public void init(Role role) throws Exception {
        ui = getUI();
        alertSettings = ui.getAlertsSettingsPage();

        ui.login(role);
        ui.getLeftNavigationPanel().goToAlerts();

        if (role.equals(Role.ADMIN)) {
            cleanup();
        }
    }

    private void cleanup() throws Exception {
        if (alertSettings.alertExists(alertName)) {
            alertSettings.deleteAlert(alertName);
        }
    }

    private Alert getFrontentErrorsAlert() {
        Alert alert = new Alert();
        alert.setName("Frontend Errors");
        alert.setDescription("Alert triggered when the threshold is exceeded for frontend url group errors.");
        alert.setErrorThreshold(new AlertThreshold(8L, 8, 10));
        alert.setWarningThreshold(new AlertThreshold(1L, 8, 10));

        List<AlertMetricGrouping> metricGroupings = new ArrayList<AlertMetricGrouping>();
        metricGroupings.add(new AlertMetricGrouping(
                "(.*)", "(.*)\\|(.*)\\|(.*)",
                "Frontends\\|Apps\\|[^|]+\\|URLs\\|[^|]+:Errors Per Interval"));
        metricGroupings.add(new AlertMetricGrouping(
                "(.*)", "(.*)\\|(.*)\\|(.*)", "Servlets\\|[^|]*:Errors Per Interval"));
        alert.setMetricGroupings(metricGroupings);

        return alert;
    }

    @Test
    public void testCrud() throws Exception {
        init(Role.ADMIN);

        // create
        alertSettings.createAlert(alert);
        ui.pageRefresh();
        Assert.assertTrue(alertSettings.alertExists(alertName));

        // update
        Assert.assertEquals(alertSettings.getRowDescription(alertName), alert.getDescription());
        String newDesc = "Chuck Norris can take a screenshot of his blue screen";
        alert.setDescription(newDesc);
        alertSettings.updateAlert(alert);
        Assert.assertTrue(alertSettings.alertExists(alertName));
        Assert.assertEquals(alertSettings.getRowDescription(alertName), newDesc);

        // delete
        alertSettings.deleteAlert(alertName);
        Assert.assertFalse(alertSettings.alertExists(alertName));
    }

    @Test
    public void testFormValidation() throws Exception {
        init(Role.ADMIN);

        // check 'Create Alert' button
        alertSettings.addNewAlert();
        Assert.assertFalse(alertSettings.isCreateButtonEnabled(""),
                "'Create Alert' button shouldn't be enabled after new Alert was added.");

        // validate name
        Assert.assertFalse(alertSettings.isFormNameValid(""),
                "Empty Alert Name shouldn't be valid.");
        alertSettings.setFormName("", alertName);
        Assert.assertTrue(alertSettings.isFormNameValid(alertName));

        // validate description
        String longStr = StringUtils.repeat("*", 4096);
        alertSettings.setFormDescription(alertName, longStr + "*");
        Assert.assertFalse(alertSettings.isFormDescriptionValid(alertName),
                "Maximum length of the Alert Description is 4096 characters.");

        alertSettings.setFormDescription(alertName, longStr);
        Assert.assertTrue(alertSettings.isFormDescriptionValid(alertName));

        // validate agent specifier
        String validRegex = "[a-zA-Z0-9,.' ]+";
        String invalidRegex = validRegex + "\\";

        Assert.assertEquals(alertSettings.getAgentSpecifier(alertName, 0), "");
        Assert.assertFalse(alertSettings.isAgentSpecifierValid(alertName, 0),
                "Empty Agent Specifier shouldn't be valid.");

        alertSettings.setAgentSpecifier(alertName, 0, invalidRegex);
        Assert.assertFalse(alertSettings.isAgentSpecifierValid(alertName, 0));
        alertSettings.setAgentSpecifier(alertName, 0, validRegex);
        Assert.assertTrue(alertSettings.isAgentSpecifierValid(alertName, 0));

        // validate metric specifier
        Assert.assertEquals(alertSettings.getMetricSpecifier(alertName, 0), "");
        Assert.assertFalse(alertSettings.isMetricSpecifierValid(alertName, 0),
                "Empty metric specifier shouldn't be valid.");

        alertSettings.setMetricSpecifier(alertName, 0, invalidRegex);
        Assert.assertFalse(alertSettings.isMetricSpecifierValid(alertName, 0));
        alertSettings.setMetricSpecifier(alertName, 0, validRegex);
        Assert.assertTrue(alertSettings.isMetricSpecifierValid(alertName, 0));

        // validate comparison operator
        alertSettings.showAdvancedOptions(alertName);
        Assert.assertEquals(alertSettings.getComparisonOperator(alertName), "Greater Than",
                "Default Comparison Operator value should be 'Greater Than'");

        // validate danger and caution thresholds
        Assert.assertEquals(alertSettings.getDangerThreshold(alertName), "0",
                "Default Danger Threshold value should be zero.");
        Assert.assertEquals(alertSettings.getCautionThreshold(alertName), "0",
                "Default Caution Threshold value should be zero.");

        alertSettings.setDangerThreshold(alertName, "");
        Assert.assertFalse(alertSettings.isDangerThresholdValid(alertName));
        alertSettings.setCautionThreshold(alertName, "");
        Assert.assertFalse(alertSettings.isCautionThresholdValid(alertName));

        alertSettings.setDangerThreshold(alertName, "-");
        Assert.assertFalse(alertSettings.isDangerThresholdValid(alertName));
        alertSettings.setCautionThreshold(alertName, "-");
        Assert.assertFalse(alertSettings.isCautionThresholdValid(alertName));

        alertSettings.setDangerThreshold(alertName, "a");
        Assert.assertFalse(alertSettings.isDangerThresholdValid(alertName));
        alertSettings.setCautionThreshold(alertName, "a");
        Assert.assertFalse(alertSettings.isCautionThresholdValid(alertName));

        alertSettings.setDangerThreshold(alertName, "2");
        alertSettings.setCautionThreshold(alertName, "2");
        Assert.assertTrue(alertSettings.isDangerThresholdValid(alertName));
        Assert.assertTrue(alertSettings.isCautionThresholdValid(alertName));

        alertSettings.setCautionThreshold(alertName, "3");
        Assert.assertFalse(alertSettings.isDangerThresholdValid(alertName),
                "Should be invalid because the Danger threshold must be heigher or equal to the Caution threshold.");
        Assert.assertFalse(alertSettings.isCautionThresholdValid(alertName),
                "Should be invalid because the Danger threshold must be heigher or equal to the Caution threshold.");

        alertSettings.selectComparisonOperator(alertName, "Less Than");
        Assert.assertTrue(alertSettings.isDangerThresholdValid(alertName));
        Assert.assertTrue(alertSettings.isCautionThresholdValid(alertName));

        alertSettings.setDangerThreshold(alertName, "5");
        Assert.assertFalse(alertSettings.isDangerThresholdValid(alertName),
                "The Danger threshold must be lower or equal to the Caution threshold.");
        Assert.assertFalse(alertSettings.isCautionThresholdValid(alertName),
                "The Danger threshold must be lower or equal to the Caution threshold.");

        alertSettings.selectComparisonOperator(alertName, "Greater Than");
        Assert.assertTrue(alertSettings.isCreateButtonEnabled(alertName),
                "All the fields should be valid at this point.");

        // validate periods over threshold and observed periods
        Assert.assertEquals(alertSettings.getDangerPeriodsOverThreshold(alertName), "1",
                "Default value for Danger Periods Over Threshold should be 1.");
        Assert.assertEquals(alertSettings.getDangerObservedPeriods(alertName), "1",
                "Default value for Danger Observed Periods should be 1.");
        Assert.assertEquals(alertSettings.getCautionPeriodsOverThreshold(alertName), "1",
                "Default value for Caution Periods Over Threshold should be 1.");
        Assert.assertEquals(alertSettings.getCautionObservedPeriods(alertName), "1",
                "Default value for Caution Obsereved Periods should be 1.");

        alertSettings.setDangerPeriodsOverThreshold(alertName, "");
        Assert.assertFalse(alertSettings.isDangerPeriodsOverThresholdValid(alertName));
        alertSettings.setDangerObservedPeriods(alertName, "");
        Assert.assertFalse(alertSettings.isDangerObservedPeriodsValid(alertName));
        alertSettings.setCautionPeriodsOverThreshold(alertName, "");
        Assert.assertFalse(alertSettings.isCautionPeriodsOverThresholdValid(alertName));
        alertSettings.setCautionObservedPeriods(alertName, "");
        Assert.assertFalse(alertSettings.isCautionObservedPeriodsValid(alertName));

        alertSettings.setDangerPeriodsOverThreshold(alertName, "-");
        Assert.assertFalse(alertSettings.isDangerPeriodsOverThresholdValid(alertName));
        alertSettings.setDangerObservedPeriods(alertName, "-");
        Assert.assertFalse(alertSettings.isDangerObservedPeriodsValid(alertName));
        alertSettings.setCautionPeriodsOverThreshold(alertName, "-");
        Assert.assertFalse(alertSettings.isCautionPeriodsOverThresholdValid(alertName));
        alertSettings.setCautionObservedPeriods(alertName, "-");
        Assert.assertFalse(alertSettings.isCautionObservedPeriodsValid(alertName));

        alertSettings.setDangerPeriodsOverThreshold(alertName, "b");
        Assert.assertFalse(alertSettings.isDangerPeriodsOverThresholdValid(alertName));
        alertSettings.setDangerObservedPeriods(alertName, "b");
        Assert.assertFalse(alertSettings.isDangerObservedPeriodsValid(alertName));
        alertSettings.setCautionPeriodsOverThreshold(alertName, "b");
        Assert.assertFalse(alertSettings.isCautionPeriodsOverThresholdValid(alertName));
        alertSettings.setCautionObservedPeriods(alertName, "b");
        Assert.assertFalse(alertSettings.isCautionObservedPeriodsValid(alertName));

        alertSettings.setDangerPeriodsOverThreshold(alertName, "0");
        Assert.assertTrue(alertSettings.isDangerPeriodsOverThresholdValid(alertName));
        alertSettings.setDangerObservedPeriods(alertName, "0");
        Assert.assertTrue(alertSettings.isDangerObservedPeriodsValid(alertName));
        alertSettings.setCautionPeriodsOverThreshold(alertName, "0");
        Assert.assertTrue(alertSettings.isCautionPeriodsOverThresholdValid(alertName));
        alertSettings.setCautionObservedPeriods(alertName, "0");
        Assert.assertTrue(alertSettings.isCautionObservedPeriodsValid(alertName));

        alertSettings.setDangerPeriodsOverThreshold(alertName, "1");
        Assert.assertFalse(
                alertSettings.isDangerPeriodsOverThresholdValid(alertName),
                "Should be invalid because the Period Over Threshold value must be less or equal to the Observed Periods value.");
        Assert.assertFalse(
                alertSettings.isDangerObservedPeriodsValid(alertName),
                "Should be invalid because the Period Over Threshold value must be less or equal to the Observed Periods value.");

        alertSettings.setCautionPeriodsOverThreshold(alertName, "1");
        Assert.assertFalse(
                alertSettings.isCautionPeriodsOverThresholdValid(alertName),
                "Should be invalid because the Period Over Threshold value must be less or equal to the Observed Periods value.");
        Assert.assertFalse(
                alertSettings.isCautionObservedPeriodsValid(alertName),
                "Should be invalid because the Period Over Threshold value must be less or equal to the Observed Periods value.");

        alertSettings.setDangerObservedPeriods(alertName, "2");
        alertSettings.setCautionObservedPeriods(alertName, "2");
        Assert.assertTrue(alertSettings.isCreateButtonEnabled(alertName),
                "All the fields should be valid at this point.");

        // create Alert
        alertSettings.clickCreateAlertButton(alertName);

        // validate name uniqueness
        alertSettings.addNewAlert();
        alertSettings.setFormName("", alertName);
        Assert.assertFalse(alertSettings.isFormNameValid(alertName),
                "Alert Name should be unique.");

        String newUniqName = alertName + "UNIQUE";
        alertSettings.setFormName(alertName, newUniqName);
        Assert.assertTrue(alertSettings.isFormNameValid(newUniqName));
        alertSettings.deleteAlert(newUniqName);
        
        cleanup();
    }

    @Test
    public void testReadOnlyForm() throws Exception {
        init(Role.GUEST);
        String aName = alertSettings.getNameOfFirstAlert();
        alertSettings.expandRow(aName);
        Assert.assertFalse(alertSettings.areFormButtonsPresent(aName));
    }

    @Test
    public void testMapLinksToAlerts() throws Exception {
        ui = getUI();
        alertSettings = ui.getAlertsSettingsPage();
        ui.login(Role.ADMIN);
        ui.getLeftNavigationPanel().goToMapViewPage();

        String[] nodeNames = ui.getCanvas().getArrayOfNodeNames();
        Assert.assertTrue(nodeNames.length > 0, "Map nodes not found.");

        String someNode = nodeNames[0];
        ui.getCanvas().selectNodeByName(someNode);

        List<String> alertNames = ui.getDetailsPanel().getAlertsList();
        logger.info("Alerts in right panel: " + alertNames);
        Assert.assertTrue(alertNames.size() > 0, "'" + someNode + "' has no alerts.");

        String someAlert = alertNames.get(0);
        ui.getDetailsPanel().getAlertByName(someAlert).click();
        alertSettings.waitForWorkIndicator();

        Assert.assertTrue(alertSettings.alertExists(someAlert));
        Assert.assertEquals(alertSettings.getExpandedRowsCount(), 1);
        Assert.assertTrue(alertSettings.isRowExpanded(someAlert));
    }
}
