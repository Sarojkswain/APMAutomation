package com.ca.apm.test.atc.filtersnavigation;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.*;
import com.ca.apm.test.atc.common.AttributeRulesTable.Operator;
import com.ca.apm.test.atc.common.UI.Role;
import com.ca.apm.test.atc.common.UI.View;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test that the UI can process the URL with pre-APM 10.2 filters (also referred to as "Simple filters") 
 * as documented on https://cawiki.ca.com/display/APM/AGC+Filter+URL+encoding
 * 
 * This backward compatibility is required because of integration with other applications.
 * 
 * Up to 10.1 we could filter only by a subset of attributes:
 * <ul>
 * <li>'bsf' for 'serviceId' displayed as 'Business Service'</li>
 * <li>'apf' for 'applicationName' displayed as 'Application'</li>
 * <li>'trf' for 'transactionId' displayed as 'Business Transaction'</li>
 * <li>'loc' for 'location' displayed as 'Location'</li>
 * <li>'owner' displayed as 'Owner'</li>
 * <li>'tier' displayed as 'Tier'</li>
 * <li>'type' displayed as 'Type'</li>
 * </ul>
 * 
 * @author strma15
 */
public class SimpleFilterUrlTest extends UITest {

    private static final String LOCATION_VALUE = "e2e_filter_url_loc";
    private static final String TIER_VALUE = "e2e_filter_url_tier";
    
    private static final String BS_1 = "ApplicationService";
    private static final String BS_3 = "Trading Service";
    
    private UI ui;
    private FilterBy f;

    private String commonMapUrl;

    private class NameAndValues {
        private String name;
        private String[] values;

        public NameAndValues(String name, String[] values) {
            super();
            this.name = name;
            this.values = values;
        }

        public String getName() {
            return name;
        }

        public String[] getValues() {
            return values;
        }
    }

    private void init() throws Exception {
        ui = getUI();
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
        
        commonMapUrl = ui.getCurrentUrl();
        logger.info("Found out the common map URL: {}", commonMapUrl);

        logger.info("switching to attribute rules");
        ui.getLeftNavigationPanel().goToDecorationPolicies();
        
        AttributeRulesTable table = ui.getAttributeRulesTable();
        table.createRow("Location", LOCATION_VALUE, "type", Operator.IS_NOT_EMPTY);
        table.createRow("Tier", TIER_VALUE, "type", Operator.IS_NOT_EMPTY);
        
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
    private void doTestProcessFilterInUrlAs(Role role, List<NameAndValues> attributesAndValues,
        String[] attributeNamesLocalized, boolean checkAlsoValues) throws Exception {
        init();

        StringBuilder fUrlPart = new StringBuilder();
        fUrlPart.append("{");

        boolean firstAttribute = true;
        for (NameAndValues e : attributesAndValues) {
            if (firstAttribute) {
                firstAttribute = false;
            } else {
                fUrlPart.append(",");
            }

            fUrlPart.append("\"").append(e.getName()).append("\":");

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
        }

        fUrlPart.append("}");

        String modURL = UrlUtils.getAtcMapUrlWithParamValue(commonMapUrl, "f", fUrlPart.toString());

        // Remove the 'u' parameter for universe 
        modURL = UrlUtils.getAtcMapUrlWithParamRemoved(modURL, "u");

        logger.info("Log in as {} with the URL: {}", role.getUser(), modURL);
        ui.login(role, modURL, View.MAPVIEW);

        ui.getCanvas().waitForUpdate();
        ui.getRibbon().expandFilterToolbar();

        // Pre-10.2 filters always have just 1 clause - no ORs
        Assert.assertEquals(f.getFilterClausesCount(), 1);

        // Number of filters should be equal to the number of filters passed in the URL
        Assert.assertEquals(f.getFilterItemCount(0), attributesAndValues.size());

        // Verify the 'Include request entry point' check box is not checked
        Assert.assertFalse(f.isCheckedShowEntryElement(), 
            "'Include request entry point' check box should not be checked when a legacy filter is used in the URL");
        
        // Verify the filters created in the filter toolbar match those passed in the URL
        List<String> filterAttributeNames = f.getFilterItemNames(0);
        for (int i = 0; i < attributesAndValues.size(); i++) {
            Assert.assertEquals(filterAttributeNames.get(i).toLowerCase(), attributeNamesLocalized[i].toLowerCase());
        }

        if (checkAlsoValues) {
            // Verify the values are properly selected in the combos
            for (int i = 0; i < attributesAndValues.size(); i++) {
                String attrName = attributesAndValues.get(i).getName();
                String attrNameLoc = filterAttributeNames.get(i);

                FilterMenu fMenu = new FilterMenu(attrNameLoc, f, ui);
                fMenu.expandDropDownMenu();

                String[] valuesToBeSelectedAsArray = attributesAndValues.get(i).getValues();
                if (valuesToBeSelectedAsArray == null) {
                    int allItemsCnt = fMenu.getListOfMenuItems().size();
                    int selectedItemsCnt = fMenu.getListOfSelectedItems().size();
                    Assert.assertEquals(selectedItemsCnt, allItemsCnt,
                        "Selected items do not match for the attribute name '" + attrName + "'");
                } else {
                    List<String> valuesSelectedInFilter = fMenu.getListOfSelectedMenuItemsNames();
                    List<String> valuesToBeSelected = Arrays.asList(valuesToBeSelectedAsArray);

                    Assert.assertEquals(valuesSelectedInFilter.size(), valuesToBeSelected.size(),
                        "Selected items count does not match for the filter '" + attrNameLoc + "'");

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
            }
        }
    }

    @Test(dependsOnMethods = "preparationGetCurrentUrlAndCreateAttrRules")
    public void testApplicationFilter() throws Exception {
        List<NameAndValues> attrs = new ArrayList<NameAndValues>();
        attrs.add(new NameAndValues("apf", new String[] {"TradeService"}));

        String[] attrsLocalized = {"Application"};

        doTestProcessFilterInUrlAs(Role.ADMIN, attrs, attrsLocalized, true);

        ui.cleanup();
    }

    @Test(dependsOnMethods = "preparationGetCurrentUrlAndCreateAttrRules")
    public void testBusinessServiceFilterAsAdmin() throws Exception {
        List<NameAndValues> attrs = new ArrayList<NameAndValues>();
        attrs.add(new NameAndValues("bsf", new String[] {BS_3}));

        String[] attrsLocalized = {"Business Service"};

        // Do not check values as there are no BTs defined on TeamCenterRegressionTestBed
        doTestProcessFilterInUrlAs(Role.ADMIN, attrs, attrsLocalized, false);

        ui.cleanup();
    }

    @Test(dependsOnMethods = "preparationGetCurrentUrlAndCreateAttrRules")
    public void testBusinessServiceFilterAsGuest() throws Exception {
        List<NameAndValues> attrs = new ArrayList<NameAndValues>();
        attrs.add(new NameAndValues("bsf", new String[] {BS_1}));

        String[] attrsLocalized = {"Business Service"};

        /* Do not test values for GUEST as we are not sure what universe is the user assigned to */
        doTestProcessFilterInUrlAs(Role.GUEST, attrs, attrsLocalized, false);

        ui.cleanup();
    }
    
    @Test(dependsOnMethods = "preparationGetCurrentUrlAndCreateAttrRules")
    public void testBusinessTransactionFilter() throws Exception {
        List<NameAndValues> attrs = new ArrayList<NameAndValues>();
        attrs.add(new NameAndValues("trf", null)); // i.e. all values should be selected

        String[] attrsLocalized = {"Business Transaction"};

        doTestProcessFilterInUrlAs(Role.ADMIN, attrs, attrsLocalized, true);

        ui.cleanup();
    }

    @Test(dependsOnMethods = "preparationGetCurrentUrlAndCreateAttrRules")
    public void testLocationFilter() throws Exception {
        List<NameAndValues> attrs = new ArrayList<NameAndValues>();
        // all values should be selected
        attrs.add(new NameAndValues("loc", null)); 
                                                                              
        String[] attrsLocalized = {"location"};

        doTestProcessFilterInUrlAs(Role.GUEST, attrs, attrsLocalized, true);

        ui.cleanup();
    }
    
    @Test(dependsOnMethods = "preparationGetCurrentUrlAndCreateAttrRules")
    public void testOwnerFilter() throws Exception {
        List<NameAndValues> attrs = new ArrayList<NameAndValues>();
        // owner not set
        attrs.add(new NameAndValues("owner", new String[] {"CA_INTERNAL_NULL"})); 
        String[] attrsLocalized = {"owner"};

        doTestProcessFilterInUrlAs(Role.ADMIN, attrs, attrsLocalized, true);

        ui.cleanup();
    }
    
    @Test(dependsOnMethods = "preparationGetCurrentUrlAndCreateAttrRules")
    public void testTierFilter() throws Exception {
        List<NameAndValues> attrs = new ArrayList<NameAndValues>();
        // all values should be selected
        attrs.add(new NameAndValues("tier", null)); 
                                                                          
        String[] attrsLocalized = {"tier"};

        doTestProcessFilterInUrlAs(Role.GUEST, attrs, attrsLocalized, true);

        ui.cleanup();
    }

    @Test(dependsOnMethods = "preparationGetCurrentUrlAndCreateAttrRules")
    public void testTypeFilter() throws Exception {
        List<NameAndValues> attrs = new ArrayList<NameAndValues>();
        attrs.add(new NameAndValues("type", new String[] {"GENERICFRONTEND", "SERVLET"})); 

        String[] attrsLocalized = {"Type"};

        doTestProcessFilterInUrlAs(Role.ADMIN, attrs, attrsLocalized, true);

        ui.cleanup();
    }
    
    @Test(dependsOnMethods = "preparationGetCurrentUrlAndCreateAttrRules")
    public void testMultipleFilters() throws Exception {
        List<NameAndValues> attrs = new ArrayList<NameAndValues>();
        attrs.add(new NameAndValues("apf", new String[] {"TradeService"}));
        attrs.add(new NameAndValues("bsf", new String[] {"Trading Service"}));
        attrs.add(new NameAndValues("loc", null)); 
        attrs.add(new NameAndValues("owner", new String[] {"CA_INTERNAL_NULL"}));
        attrs.add(new NameAndValues("tier", null));
        attrs.add(new NameAndValues("trf", null));
        attrs.add(new NameAndValues("type", new String[] {"DATABASE","SOCKET","SERVLET"})); 

        String[] attrsLocalized = {"Application", "Business Service", "location", "owner", "tier", "Business Transaction", "Type"};

        doTestProcessFilterInUrlAs(Role.ADMIN, attrs, attrsLocalized, false);

        ui.cleanup();
    }
        
    @Test(dependsOnMethods = {"testLocationFilter", "testTierFilter", "testMultipleFilters"}, alwaysRun = true)
    public void cleanUpAttributeRules() throws Exception {
        init();

        ui.login();
        ui.getLeftNavigationPanel().goToDecorationPolicies();
        
        AttributeRulesTable table = ui.getAttributeRulesTable();
        table.removeRowsIfExist("Location", LOCATION_VALUE, "type", Operator.IS_NOT_EMPTY, null);
        table.removeRowsIfExist("Tier", TIER_VALUE, "type", Operator.IS_NOT_EMPTY, null);
        
        ui.getLeftNavigationPanel().goToMapViewPage();
        ui.cleanup();
    }
}
