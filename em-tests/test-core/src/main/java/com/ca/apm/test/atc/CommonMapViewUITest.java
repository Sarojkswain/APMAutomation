package com.ca.apm.test.atc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;

import com.ca.apm.test.atc.common.Canvas;
import com.ca.apm.test.atc.common.UI;

public abstract class CommonMapViewUITest extends UITest {
    
    protected static final String PERSP1 = "Type";
    protected static final String PERSP2 = "Application";
    
    protected static final String PERSP1_GROUP1_NAME = "SERVLET";
    protected static final String[] PERSP1_GROUP1_POSSIBLE_SUBNODES = {"TradeOptions|service",
            "JspServlet|service", "ServletA6|service", "AxisServlet|service", "PlaceOrder|service",
            "DefaultServlet|service", "ViewOrder|service"};
    
    protected static final String PERSP1_GROUP1_COMMON_BASIC_ATTR_NAME = "agentDomain";
    protected static final String PERSP1_GROUP1_COMMON_BASIC_ATTR_VALUE = "SuperDomain/TomcatDomain";

    protected static final String PERSP1_GROUP2_NAME = "GENERICFRONTEND";
    protected static final String[] PERSP1_GROUP2_POSSIBLE_SUBNODES = {"Apps|TradeService|URLs|Default",
            "Apps|TradeService|URLs|/TradeService/", "Apps|Welcome to Tomcat|URLs|Default",
            "Apps|Welcome to Tomcat|URLs|/robots.txt",
            "Apps|TradeService|URLs|/TradeService/TradeOptions",
            "Apps|AuthenticationService|URLs|/AuthenticationService/",
            "Apps|AuthenticationEngine|URLs|/AuthenticationEngine/services/",
            "Apps|OrderEngine|URLs|/OrderEngine/services/"};
    
    protected static final String PERSP1_GROUP3_NAME = "INFERRED_DATABASE";
    protected static final String[] PERSP1_GROUP3_POSSIBLE_SUBNODES = {"file%order-records (Hypersonic)",
            "DATABASE : file%customer-records (Hypersonic)"};
    
    protected static final String PERSP2_GROUP1_NAME = "TradeService - applicationName";
    protected static final String[] PERSP2_GROUP1_POSSIBLE_SUBNODES = {"Place Order",
    "Options Trading", "ViewOrders|service", "TradeOptions|service", "PlaceOrder|service"};    
    
    protected final List<String> persp1group1existingSubnodes = new ArrayList<String>();
    protected final List<String> persp1group2existingSubnodes = new ArrayList<String>();
    protected final List<String> persp1group3existingSubnodes = new ArrayList<String>();
    protected final List<String> persp2group1existingSubnodes = new ArrayList<String>();
    
    protected static String PERSP1_GROUP1_TEST_SUBNODE_NAME;
    protected static String PERSP1_GROUP2_TEST_SUBNODE_NAME;
    protected static String PERSP1_GROUP2_TEST_SUBNODE2_NAME;
    protected static String PERSP1_GROUP3_TEST_SUBNODE_NAME;
    protected static String PERSP2_GROUP1_TEST_SUBNODE_NAME;
    protected static boolean testSubnodesAssigned = false;
    
    protected UI ui = null;
    
    protected void init() throws Exception {
        if (!testSubnodesAssigned) {
            initTestNodes();
        }
    }
    
    private void initTestNodes() throws Exception {
        logger.info("Enable live mode");
        ui.getTimeline().turnOnLiveMode();

        logger.info("Create the perspective '{}' if it does not exist yet", PERSP2);
        if (!ui.getPerspectivesControl().isPerspectivePresent(PERSP2)) {
            ui.getPerspectivesControl().addPerspective(PERSP2, false);
        } else {
            ui.getPerspectivesControl().selectPerspectiveByName(PERSP2);
        }
        
        logger.info("Filter out nodes that do not exist in this environment");
        filterExistingGroupSubnodes(PERSP1, PERSP1_GROUP1_NAME, PERSP1_GROUP1_POSSIBLE_SUBNODES, persp1group1existingSubnodes);
        filterExistingGroupSubnodes(PERSP1, PERSP1_GROUP2_NAME, PERSP1_GROUP2_POSSIBLE_SUBNODES, persp1group2existingSubnodes);
        filterExistingGroupSubnodes(PERSP1, PERSP1_GROUP3_NAME, PERSP1_GROUP3_POSSIBLE_SUBNODES, persp1group3existingSubnodes);
        filterExistingGroupSubnodes(PERSP2, PERSP2_GROUP1_NAME, PERSP2_GROUP1_POSSIBLE_SUBNODES, persp2group1existingSubnodes);
        
        Assert.assertTrue(persp1group1existingSubnodes.size() >= 1, "There should be at least 1 node in the group '" + PERSP1_GROUP1_NAME + "'");
        Assert.assertTrue(persp1group2existingSubnodes.size() >= 2, "There should be at least 2 nodes in the group '" + PERSP1_GROUP2_NAME + "'");
        Assert.assertTrue(persp1group3existingSubnodes.size() >= 1, "There should be at least 1 node in the group '" + PERSP1_GROUP3_NAME + "'");
        Assert.assertTrue(persp2group1existingSubnodes.size() >= 1, "There should be at least 1 node in the group '" + PERSP2_GROUP1_NAME + "'");
        
        PERSP1_GROUP1_TEST_SUBNODE_NAME = persp1group1existingSubnodes.get(0);
        PERSP1_GROUP2_TEST_SUBNODE_NAME = persp1group2existingSubnodes.get(0);
        PERSP1_GROUP2_TEST_SUBNODE2_NAME = persp1group2existingSubnodes.get(1);
        PERSP1_GROUP3_TEST_SUBNODE_NAME = persp1group3existingSubnodes.get(0);
        PERSP2_GROUP1_TEST_SUBNODE_NAME = persp2group1existingSubnodes.get(0);
        
        logger.info("PERSP1_GROUP1_TEST_SUBNODE_NAME=" + PERSP1_GROUP1_TEST_SUBNODE_NAME);
        logger.info("PERSP1_GROUP1_TEST_SUBNODE_NAME=" + PERSP1_GROUP2_TEST_SUBNODE_NAME);
        logger.info("PERSP1_GROUP2_TEST_SUBNODE2_NAME=" + PERSP1_GROUP2_TEST_SUBNODE2_NAME);
        logger.info("PERSP1_GROUP3_TEST_SUBNODE_NAME=" + PERSP1_GROUP3_TEST_SUBNODE_NAME);
        logger.info("PERSP2_GROUP1_TEST_SUBNODE_NAME=" + PERSP2_GROUP1_TEST_SUBNODE_NAME);
        
        testSubnodesAssigned = true;
    }
    
    /**
     * Filter the sub-nodes of a group that do exist in the tested APM environment.
     * 
     * @param groupName
     * @param potentialSubnodes
     * @param existingSubnodes
     * @throws Exception 
     */
    protected void filterExistingGroupSubnodes(String perspective, String groupName, String[] potentialSubnodes, List<String> existingSubnodes) throws Exception {
        existingSubnodes.clear();
        
        ui.getPerspectivesControl().selectPerspectiveByName(perspective);
        Assert.assertTrue(ui.getPerspectivesControl().isPerspectiveActive(perspective), "The active perspective should be '" + perspective + "'");

        Canvas canvas = ui.getCanvas();
        
        canvas.expandGroup(canvas.getNodeByNameSubstring(groupName));
        canvas.getCtrl().fitAllToView();
        
        List<String> allNodeNames = Arrays.asList(canvas.getArrayOfNodeNames());
        for (String subnode : potentialSubnodes) {
            if (allNodeNames.contains(subnode)) {
                existingSubnodes.add(subnode);
                logger.info("Verified that the group {} contains the subnode {}", groupName, subnode);
            } else {
                logger.warn("In the group {} there is no subnode {}", groupName, subnode);

            }
        }
        
        canvas.collapseGroup(canvas.getNodeByNameSubstring(groupName));
        canvas.getCtrl().fitAllToView();
    }
}
