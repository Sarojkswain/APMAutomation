package com.ca.apm.test.atc.filtersnavigation;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.*;
import com.ca.apm.test.atc.common.AttributeRulesTable.Operator;
import com.ca.apm.test.atc.common.UI.Role;
import com.ca.apm.test.atc.common.UI.View;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test that the UI can process the URL with post-APM 10.2 filters (also referred to as
 * "Advanced filters")
 * as documented on https://cawiki.ca.com/display/APM/AGC+Filter+URL+encoding
 * 
 * @author strma15
 */
public class AdvancedFilterUrlTest extends UITest {

    private static final String LOCATION_VALUE = "e2e_filter_url_loc";
    private static final String TIER_VALUE = "e2e_filter_url_tier";

    private static final String CUSTOM_ATTR_NAME = "e2e_filter_custom_name";
    private static final String CUSTOM_ATTR_VALUE = "e2e_filter_custom_value";
    
    private static final String BS_1 = "ApplicationService";
    private static final String BS_2 = "Default BS";
    private static final String BS_3 = "Trading Service";

    private UI ui;
    private WebDriver driver;
    private FilterBy f;

    private String commonMapUrl;

    private class NameBtAndValues {
        private String name;
        private String[] values;
        private int btCoverageGroupIndex;

        public NameBtAndValues(String name, String[] values) {
            this(name, values, 0);
        }

        public NameBtAndValues(String name, String[] values, int btCoverageGroupIndex) {
            this.name = name;
            this.values = values;
            this.btCoverageGroupIndex = btCoverageGroupIndex;
        }

        public String getName() {
            return name;
        }

        public String[] getValues() {
            return values;
        }

        public int getBtCoverageGroupIndex() {
            return btCoverageGroupIndex;
        }
    }

    private void init() throws Exception {
        ui = getUI();
        driver = ui.getDriver();
        f = ui.getFilterBy();
    }

    /**
     * Run this just once as the first test case to obtain a common application map URL.
     * This URL can then be modified in consequent test cases.
     * Also creates rules for tier and location attributes.
     * 
     * @throws Exception
     */
    @Test(groups = "dependency")
    public void preparationGetCurrentUrlAndCreateAttrRules() throws Exception {
        init();

        ui.login();
        
        ui.getLeftNavigationPanel().goToMapViewPage();
        
        
        ui.getTimeline().turnOnLiveMode();
        
        commonMapUrl = driver.getCurrentUrl();
        logger.info("Found out the common map URL: " + commonMapUrl);

        logger.info("switching to attribute rules");
        ui.getLeftNavigationPanel().goToDecorationPolicies();

        AttributeRulesTable table = ui.getAttributeRulesTable();
        table.createRow("Location", LOCATION_VALUE, "type", Operator.IS_NOT_EMPTY);
        table.createRow("Tier", TIER_VALUE, "type", Operator.IS_NOT_EMPTY);
        table.createRow(CUSTOM_ATTR_NAME, CUSTOM_ATTR_VALUE, "type", Operator.IS_NOT_EMPTY);

        // Give the EM some time for applying the rules and creating the attributes
        Utils.sleep(5000);

        ui.logout();

        ui.cleanup();
    }

    /**
     * Log in as the specified user with an URL bearing a temporary perspective value.
     * 
     * @throws Exception
     */
    private void doTestProcessFilterInUrlAs(Role role, List<List<NameBtAndValues>> clauses, boolean showEntryPoints, 
        String[][] attributeNamesLocalized, boolean checkAlsoValues) throws Exception {
        init();

        StringBuilder fUrlPart = new StringBuilder();
        fUrlPart.append("[");

        boolean firstAttribute = true;
        for (List<NameBtAndValues> clause : clauses) {
            boolean firstInClause = true;
            for (NameBtAndValues e : clause) {
                if (firstAttribute) {
                    firstAttribute = false;
                } else {
                    fUrlPart.append(",");
                }

                fUrlPart.append("{");
                fUrlPart.append("\"n\":\"").append(e.getName()).append("\",");

                if (!firstAttribute && firstInClause) {
                    fUrlPart.append("\"o\":\"OR\",");
                    firstInClause = false;
                } else {
                    fUrlPart.append("\"o\":\"AND\",");
                }

                if (e.getBtCoverageGroupIndex() > 0) {
                    fUrlPart.append("\"b\":").append(e.getBtCoverageGroupIndex()).append(",");
                };

                fUrlPart.append("\"v\":");

                if (e.getValues() == null) {
                    fUrlPart.append("null");
                } else {
                    fUrlPart.append("[");
                    boolean firstValue = true;
                    for (String value : e.getValues()) {
                        if (firstValue) {
                            firstValue = false;
                        } else {
                            fUrlPart.append(",");
                        }

                        fUrlPart.append("\"").append(value).append("\"");
                    }

                    fUrlPart.append("]");
                }

                fUrlPart.append("}");
            }
        }

        fUrlPart.append("]");

        // Add the 'fa' parameter for advanced filters
        String modURL = UrlUtils.getAtcMapUrlWithParamValue(commonMapUrl, "fa", fUrlPart.toString());
        
        // Remove the 'f' parameter for simple filters (which would have precedence if it was passed)
        modURL = UrlUtils.getAtcMapUrlWithParamRemoved(modURL, "f");
        
        // Remove the 'u' parameter for universe 
        modURL = UrlUtils.getAtcMapUrlWithParamRemoved(modURL, "u");
        
        // Add the 'ep' parameter for 'Include request entry point'
        modURL = UrlUtils.getAtcMapUrlWithParamValue(modURL, "ep", showEntryPoints ? "1" : "0");
        
        logger.info("Log in as " + role.getUser() + " with the URL: " + modURL);
        ui.login(role, modURL, View.MAPVIEW);

        ui.getCanvas().waitForUpdate();
        ui.getRibbon().expandFilterToolbar();

        // Number of clauses
        Assert.assertEquals(f.getFilterClausesCount(), clauses.size());

        // Number of filters within individual clauses should correspond to the passed URL
        for (int c = 0; c < clauses.size(); c++) {
            Assert.assertEquals(f.getFilterItemCount(c), clauses.get(c).size());
        }

        // Verify the 'Include request entry point' check box is checked if requested
        Assert.assertEquals(f.isCheckedShowEntryElement(), showEntryPoints);
        
        // Verify the filters created in the filter toolbar match those passed in the URL
        for (int c = 0; c < clauses.size(); c++) {
            List<String> filterAttributeNames = f.getFilterItemNames(c);
            for (int i = 0; i < clauses.get(c).size(); i++) {
                Assert.assertEquals(filterAttributeNames.get(i).toLowerCase(),
                    attributeNamesLocalized[c][i].toLowerCase());

                // Check that the filter item is (not) in a business transaction coverage as passed
                Assert.assertEquals(f.isFilterItemInBtClause(f.getFilterItemElement(c, i)), clauses
                    .get(c).get(i).getBtCoverageGroupIndex() > 0);
            }
        }

        if (checkAlsoValues) {
            // Verify the values are properly selected in the combo boxes
            for (int c = 0; c < clauses.size(); c++) {
                List<String> filterAttributeNames = f.getFilterItemNames(c);
                List<NameBtAndValues> items = clauses.get(c);

                for (int i = 0; i < items.size(); i++) {
                    String attrName = items.get(i).getName();
                    String attrNameLoc = filterAttributeNames.get(i);

                    FilterMenu fMenu = new FilterMenu(attrNameLoc, f, ui);
                    fMenu.expandDropDownMenu();

                    String[] valuesToBeSelectedAsArray = items.get(i).getValues();
                    if (valuesToBeSelectedAsArray == null) {
                        int allItemsCnt = fMenu.getListOfMenuItems().size();
                        int selectedItemsCnt = fMenu.getListOfSelectedItems().size();
                        Assert
                            .assertEquals(selectedItemsCnt, allItemsCnt,
                                "Selected items do not match for the attribute name '" + attrName
                                    + "'");
                    } else {
                        List<String> valuesSelectedInFilter =
                            fMenu.getListOfSelectedMenuItemsNames();
                        List<String> valuesToBeSelected = Arrays.asList(valuesToBeSelectedAsArray);

                        Assert.assertEquals(valuesSelectedInFilter.size(),
                            valuesToBeSelected.size(),
                            "Selected items count does not match for the filter '" + attrNameLoc
                                + "'");

                        for (String v : valuesSelectedInFilter) {
                            if ("Not set".equals(v)) {
                                v = "CA_INTERNAL_NULL";
                            }

                            Assert.assertTrue(valuesToBeSelected.contains(v), "The value '" + v
                                + "' should not be selected in the filter '" + attrNameLoc + "'");
                        };

                        for (String v : valuesToBeSelected) {
                            if ("CA_INTERNAL_NULL".equals(v)) {
                                v = "Not set";
                            }

                            Assert.assertTrue(valuesSelectedInFilter.contains(v), "The value '" + v
                                + "' should be selected in the filter '" + attrNameLoc + "'");
                        };
                    }

                    fMenu.collapseDropDownMenu();
                }
            }
        }
    }

    /* ******************************************************************************************* *
     * Regression test cases.
     * The same scenarios are tested using, simple filters, in SimpleFilterUrlTest.
     * In other words, the following stuff tests old stuff in the new notation.
     * *******************************************************************************************
     */

    @Test(dependsOnMethods = "preparationGetCurrentUrlAndCreateAttrRules")
    public void testApplicationFilterWithAbbrev() throws Exception {
        List<List<NameBtAndValues>> clauses = new ArrayList<List<NameBtAndValues>>();
        List<NameBtAndValues> attrs = new ArrayList<NameBtAndValues>();
        attrs.add(new NameBtAndValues("apf", new String[] {"TradeService"}));
        clauses.add(attrs);

        String[][] attrsLocalized = {{"Application"}};

        /* Do not test values for GUEST as we are not sure what universe is the user assigned to */
        doTestProcessFilterInUrlAs(Role.GUEST, clauses, true, attrsLocalized, false);

        ui.cleanup();
    }

    @Test(dependsOnMethods = "preparationGetCurrentUrlAndCreateAttrRules")
    public void testApplicationFilterWithName() throws Exception {
        List<List<NameBtAndValues>> clauses = new ArrayList<List<NameBtAndValues>>();
        List<NameBtAndValues> attrs = new ArrayList<NameBtAndValues>();
        attrs.add(new NameBtAndValues("applicationName", new String[] {"AuthenticationService"}));
        clauses.add(attrs);

        String[][] attrsLocalized = {{"Application"}};

        doTestProcessFilterInUrlAs(Role.ADMIN, clauses, false, attrsLocalized, true);

        ui.cleanup();
    }

    @Test(dependsOnMethods = "preparationGetCurrentUrlAndCreateAttrRules")
    public void testBusinessServiceFilterWithAbbrev() throws Exception {
        List<List<NameBtAndValues>> clauses = new ArrayList<List<NameBtAndValues>>();
        List<NameBtAndValues> attrs = new ArrayList<NameBtAndValues>();
        attrs.add(new NameBtAndValues("bsf", new String[] {BS_3}));
        clauses.add(attrs);

        String[][] attrsLocalized = {{"Business Service"}};

        // Do not check values as there are no BTs defined on TeamCenterRegressionTestBed
        doTestProcessFilterInUrlAs(Role.ADMIN, clauses, true, attrsLocalized, false);

        ui.cleanup();
    }

    @Test(dependsOnMethods = "preparationGetCurrentUrlAndCreateAttrRules")
    public void testBusinessServiceFilterWithName() throws Exception {
        List<List<NameBtAndValues>> clauses = new ArrayList<List<NameBtAndValues>>();
        List<NameBtAndValues> attrs = new ArrayList<NameBtAndValues>();
        attrs.add(new NameBtAndValues("serviceId", new String[] {BS_2}));
        clauses.add(attrs);

        String[][] attrsLocalized = {{"Business Service"}};

        /* Do not test values for GUEST as we are not sure what universe is the user assigned to */
        doTestProcessFilterInUrlAs(Role.GUEST, clauses, false, attrsLocalized, false);

        ui.cleanup();
    }

    @Test(dependsOnMethods = "preparationGetCurrentUrlAndCreateAttrRules")
    public void testBusinessTransactionFilterWithAbbrev() throws Exception {
        List<List<NameBtAndValues>> clauses = new ArrayList<List<NameBtAndValues>>();
        List<NameBtAndValues> attrs = new ArrayList<NameBtAndValues>();
        // all values should be selected
        attrs.add(new NameBtAndValues("trf", null));
        clauses.add(attrs);

        String[][] attrsLocalized = {{"Business Transaction"}};

        doTestProcessFilterInUrlAs(Role.ADMIN, clauses, true, attrsLocalized, true);

        ui.cleanup();
    }

    @Test(dependsOnMethods = "preparationGetCurrentUrlAndCreateAttrRules")
    public void testBusinessTransactionFilterWithName() throws Exception {
        List<List<NameBtAndValues>> clauses = new ArrayList<List<NameBtAndValues>>();
        List<NameBtAndValues> attrs = new ArrayList<NameBtAndValues>();
        // all values should be selected
        attrs.add(new NameBtAndValues("transactionId", null));
        clauses.add(attrs);

        String[][] attrsLocalized = {{"Business Transaction"}};

        /* Do not test values for GUEST as we are not sure what universe is the user assigned to */
        doTestProcessFilterInUrlAs(Role.GUEST, clauses, false, attrsLocalized, false);

        ui.cleanup();
    }

    @Test(dependsOnMethods = "preparationGetCurrentUrlAndCreateAttrRules")
    public void testLocationFilterWithAbbrev() throws Exception {
        List<List<NameBtAndValues>> clauses = new ArrayList<List<NameBtAndValues>>();
        List<NameBtAndValues> attrs = new ArrayList<NameBtAndValues>();
        // all values should be selected
        attrs.add(new NameBtAndValues("loc", null));
        clauses.add(attrs);

        String[][] attrsLocalized = {{"location"}};

        doTestProcessFilterInUrlAs(Role.ADMIN, clauses, true, attrsLocalized, true);

        ui.cleanup();
    }

    @Test(dependsOnMethods = "preparationGetCurrentUrlAndCreateAttrRules")
    public void testLocationFilterWithName() throws Exception {
        List<List<NameBtAndValues>> clauses = new ArrayList<List<NameBtAndValues>>();
        List<NameBtAndValues> attrs = new ArrayList<NameBtAndValues>();
        // all values should be selected
        attrs.add(new NameBtAndValues("location", null));
        clauses.add(attrs);

        String[][] attrsLocalized = {{"location"}};

        doTestProcessFilterInUrlAs(Role.ADMIN, clauses, false, attrsLocalized, true);

        ui.cleanup();
    }

    @Test(dependsOnMethods = "preparationGetCurrentUrlAndCreateAttrRules")
    public void testOwnerFilter() throws Exception {
        List<List<NameBtAndValues>> clauses = new ArrayList<List<NameBtAndValues>>();
        List<NameBtAndValues> attrs = new ArrayList<NameBtAndValues>();
        // owner not set
        attrs.add(new NameBtAndValues("owner", new String[] {"CA_INTERNAL_NULL"}));
        clauses.add(attrs);

        String[][] attrsLocalized = {{"owner"}};

        doTestProcessFilterInUrlAs(Role.ADMIN, clauses, true, attrsLocalized, true);

        ui.cleanup();
    }

    @Test(dependsOnMethods = "preparationGetCurrentUrlAndCreateAttrRules")
    public void testTierFilter() throws Exception {
        List<List<NameBtAndValues>> clauses = new ArrayList<List<NameBtAndValues>>();
        List<NameBtAndValues> attrs = new ArrayList<NameBtAndValues>();
        // all values should be selected
        attrs.add(new NameBtAndValues("tier", null));
        clauses.add(attrs);

        String[][] attrsLocalized = {{"tier"}};

        doTestProcessFilterInUrlAs(Role.GUEST, clauses, false, attrsLocalized, true);

        ui.cleanup();
    }

    @Test(dependsOnMethods = "preparationGetCurrentUrlAndCreateAttrRules")
    public void testTypeFilterAsAdmin() throws Exception {
        List<List<NameBtAndValues>> clauses = new ArrayList<List<NameBtAndValues>>();
        List<NameBtAndValues> attrs = new ArrayList<NameBtAndValues>();
        attrs.add(new NameBtAndValues("type", new String[] {"GENERICFRONTEND", "SERVLET"}));
        clauses.add(attrs);

        String[][] attrsLocalized = {{"Type"}};

        doTestProcessFilterInUrlAs(Role.ADMIN, clauses, true, attrsLocalized, true);

        ui.cleanup();
    }
    
    @Test(dependsOnMethods = "preparationGetCurrentUrlAndCreateAttrRules")
    public void testTypeFilterAsGuest() throws Exception {
        List<List<NameBtAndValues>> clauses = new ArrayList<List<NameBtAndValues>>();
        List<NameBtAndValues> attrs = new ArrayList<NameBtAndValues>();
        attrs.add(new NameBtAndValues("type", new String[] {"DATABASE", "SOCKET", "SERVLET"}));
        clauses.add(attrs);

        String[][] attrsLocalized = {{"Type"}};

        /* Do not test values for GUEST as we are not sure what universe is the user assigned to */
        doTestProcessFilterInUrlAs(Role.GUEST, clauses, true, attrsLocalized, false);

        ui.cleanup();
    }

    @Test(dependsOnMethods = "preparationGetCurrentUrlAndCreateAttrRules")
    public void testMultipleFiltersWithAbbrevs() throws Exception {
        List<List<NameBtAndValues>> clauses = new ArrayList<List<NameBtAndValues>>();
        List<NameBtAndValues> attrs = new ArrayList<NameBtAndValues>();
        attrs.add(new NameBtAndValues("apf", new String[] {"TradeService"}));
        attrs.add(new NameBtAndValues("bsf", new String[] {BS_2}));
        attrs.add(new NameBtAndValues("loc", null));
        attrs.add(new NameBtAndValues("owner", new String[] {"CA_INTERNAL_NULL"}));
        attrs.add(new NameBtAndValues("tier", null));
        attrs.add(new NameBtAndValues("trf", null));
        attrs.add(new NameBtAndValues("type", new String[] {"DATABASE", "SOCKET", "SERVLET"}));
        clauses.add(attrs);

        String[][] attrsLocalized =
            {{"Application", "Business Service", "location", "owner", "tier",
                    "Business Transaction", "Type"}};

        doTestProcessFilterInUrlAs(Role.ADMIN, clauses, false, attrsLocalized, false);

        ui.cleanup();
    }

    @Test(dependsOnMethods = "preparationGetCurrentUrlAndCreateAttrRules")
    public void testMultipleFiltersWithNames() throws Exception {
        List<List<NameBtAndValues>> clauses = new ArrayList<List<NameBtAndValues>>();
        List<NameBtAndValues> attrs = new ArrayList<NameBtAndValues>();
        attrs.add(new NameBtAndValues("applicationName", new String[] {"TradeService"}));
        attrs.add(new NameBtAndValues("serviceId", new String[] {BS_1}));
        attrs.add(new NameBtAndValues("location", null));
        attrs.add(new NameBtAndValues("owner", new String[] {"CA_INTERNAL_NULL"}));
        attrs.add(new NameBtAndValues("tier", null));
        attrs.add(new NameBtAndValues("transactionId", null));
        attrs.add(new NameBtAndValues("type", new String[] {"DATABASE", "SOCKET", "SERVLET"}));
        clauses.add(attrs);

        String[][] attrsLocalized =
            {{"Application", "Business Service", "location", "owner", "tier",
                    "Business Transaction", "Type"}};

        doTestProcessFilterInUrlAs(Role.ADMIN, clauses, true, attrsLocalized, false);

        ui.cleanup();
    }

    /* ******************************************************************************************* *
     * Advanced filters test cases.
     * Testing scenarios that were not supported with simple filters.
     * *******************************************************************************************
     */

    /**
     * Test filter groups AKA conjunctive clauses
     * 
     * @throws Exception
     */
    @Test(dependsOnMethods = "preparationGetCurrentUrlAndCreateAttrRules")
    public void testTwoConjunciveClause() throws Exception {
        List<List<NameBtAndValues>> clauses = new ArrayList<List<NameBtAndValues>>();

        List<NameBtAndValues> attrs1 = new ArrayList<NameBtAndValues>();
        attrs1.add(new NameBtAndValues("serviceId", new String[] {BS_3}));
        clauses.add(attrs1);

        List<NameBtAndValues> attrs2 = new ArrayList<NameBtAndValues>();
        attrs2.add(new NameBtAndValues("transactionId", new String[] {"Login"}));
        clauses.add(attrs2);

        String[][] attrsLocalized = { {"Business Service"}, {"Business Transaction"}};

        doTestProcessFilterInUrlAs(Role.ADMIN, clauses, false, attrsLocalized, false);

        ui.cleanup();
    }

    /**
     * Test a custom attribute used in filter
     * 
     * @throws Exception
     */
    @Test(dependsOnMethods = "preparationGetCurrentUrlAndCreateAttrRules")
    public void testCustomAttributeUsedInFilter() throws Exception {
        List<List<NameBtAndValues>> clauses = new ArrayList<List<NameBtAndValues>>();
        List<NameBtAndValues> attrs = new ArrayList<NameBtAndValues>();
        attrs.add(new NameBtAndValues(CUSTOM_ATTR_NAME, new String[] {CUSTOM_ATTR_VALUE}));
        clauses.add(attrs);

        String[][] attrsLocalized = {{CUSTOM_ATTR_NAME}};

        doTestProcessFilterInUrlAs(Role.ADMIN, clauses, true, attrsLocalized, false);

        ui.cleanup();
    }

    /**
     * Test an attribute in a business transaction coverage.
     * 
     * @throws Exception
     */
    @Test(dependsOnMethods = "preparationGetCurrentUrlAndCreateAttrRules")
    public void testFilterItemInsideBtCoverage() throws Exception {
        List<List<NameBtAndValues>> clauses = new ArrayList<List<NameBtAndValues>>();
        List<NameBtAndValues> attrs = new ArrayList<NameBtAndValues>();
        attrs.add(new NameBtAndValues(CUSTOM_ATTR_NAME, new String[] {CUSTOM_ATTR_VALUE}, 1));
        clauses.add(attrs);

        String[][] attrsLocalized = {{CUSTOM_ATTR_NAME}};

        doTestProcessFilterInUrlAs(Role.ADMIN, clauses, false, attrsLocalized, false);

        ui.cleanup();
    }

    /**
     * Test a more complex scenario.
     * 
     * It depends on the simpler scenarios. 
     * If those fail, it makes no sense to run this one that is composed of those simpler pieces.
     * 
     * @throws Exception
     */
    @Test(dependsOnMethods = {"preparationGetCurrentUrlAndCreateAttrRules",
            "testTwoConjunciveClause", "testCustomAttributeUsedInFilter",
            "testFilterItemInsideBtCoverage"})
    public void testMoreComplexFilter() throws Exception {
        List<List<NameBtAndValues>> clauses = new ArrayList<List<NameBtAndValues>>();

        // Clause 1
        List<NameBtAndValues> attrs1 = new ArrayList<NameBtAndValues>();
        attrs1.add(new NameBtAndValues(CUSTOM_ATTR_NAME, new String[] {CUSTOM_ATTR_VALUE}, 1));
        attrs1.add(new NameBtAndValues("loc", null));
        clauses.add(attrs1);

        // Clause 2
        List<NameBtAndValues> attrs2 = new ArrayList<NameBtAndValues>();
        attrs2.add(new NameBtAndValues("bsf", new String[] {BS_1}));
        attrs2.add(new NameBtAndValues(CUSTOM_ATTR_NAME, null, 2));
        clauses.add(attrs2);

        // Clause 3
        List<NameBtAndValues> attrs3 = new ArrayList<NameBtAndValues>();
        attrs3.add(new NameBtAndValues("type", new String[] {"DATABASE", "SOCKET"}, 3));
        attrs3.add(new NameBtAndValues("transactionId", new String[] {"Login"}, 3));
        clauses.add(attrs3);

        String[][] attrsLocalized =
            { {CUSTOM_ATTR_NAME, "location"}, {"Business Service", CUSTOM_ATTR_NAME},
                    {"Type", "Business Transaction"}};

        doTestProcessFilterInUrlAs(Role.ADMIN, clauses, false, attrsLocalized, false);

        ui.cleanup();
    }

    /**
     * Cleans the decoration policy table from the rules created for this test only.
     * 
     * Make this method depending on any method that uses any custom attribute created for this test
     * class.
     */
    @Test(dependsOnMethods = {"testLocationFilterWithAbbrev", "testLocationFilterWithName",
            "testTierFilter", "testMultipleFiltersWithAbbrevs", "testMultipleFiltersWithNames",
            "testCustomAttributeUsedInFilter", "testFilterItemInsideBtCoverage",
            "testMoreComplexFilter"}, alwaysRun = true)
    public void cleanUpAttributeRules() throws Exception {
        init();

        ui.login();
        ui.getLeftNavigationPanel().goToDecorationPolicies();

        AttributeRulesTable table = ui.getAttributeRulesTable();
        table.removeRowsIfExist("Location", LOCATION_VALUE, "Type", Operator.IS_NOT_EMPTY, null);
        table.removeRowsIfExist("Tier", TIER_VALUE, "Type", Operator.IS_NOT_EMPTY, null);
        table.removeRowsIfExist(CUSTOM_ATTR_NAME, CUSTOM_ATTR_VALUE, "Type", Operator.IS_NOT_EMPTY, null);

        ui.getLeftNavigationPanel().goToMapViewPage();
        
        ui.cleanup();
    }
}
