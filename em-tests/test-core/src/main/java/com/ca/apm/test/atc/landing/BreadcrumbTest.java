package com.ca.apm.test.atc.landing;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.landing.*;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;

import static org.testng.Assert.*;

public class BreadcrumbTest extends UITest {

    private final static Logger logger = Logger.getLogger(BreadcrumbTest.class);
    
    private final static String FIRST_LEVEL_LABEL = "EXPERIENCE VIEW";
    
    private LandingPage landingPage;
    private Breadcrumb breadcrumb;
    private BreadcrumbExperiences tilesToolbar;
    
    private String groupingAttribute;
    private String clickedTileName;
    private List<String> tileNames;
    private List<String> dropdownOptions;
    
    
    private void init() throws Exception {
        UI ui = getUI();
        ui.login();
        landingPage = ui.getLandingPage();
        tilesToolbar = landingPage.getBreadcrumbExperiences();
        breadcrumb = ui.getBreadcrumb();
        landingPage.waitForTilesToLoad(true);
    }
    
    @Test(groups = "failing")
    public void testBreadcrumb() throws Exception {
        init();
        assertTrue(landingPage.isGlobalView());
        
        
        logger.info("Go to 1st experience");
        
        Tile t = landingPage.getNthTile(0);
        clickedTileName = t.getName();        
        tileNames = landingPage.getTileNames();
        
        landingPage.drillDownTheTile(t.getName());
        breadcrumb.waitForLoad();
        breadcrumb.scrollToTop();
        assertTrue(breadcrumb.getLevelsCount() == 1);
        checkLastLevelPathLink(FIRST_LEVEL_LABEL, clickedTileName);
        
        dropdownOptions = breadcrumb.getLastLevel().getDropdownOptions();
        assertEquals(new HashSet<String>(dropdownOptions), new HashSet<String>(tileNames));
        
        
        logger.info("Go to 1st tile");
        
        t = landingPage.getNthTile(0);
        clickedTileName = t.getName();
        groupingAttribute = tilesToolbar.getCustomAttributeName();
        tileNames = landingPage.getTileNames();
        
        landingPage.drillDownTheTile(t.getName());
        breadcrumb.waitForLoad();
        
        assertTrue(breadcrumb.getLevelsCount() == 2);
        checkLastLevelPathLink(groupingAttribute.toUpperCase(), clickedTileName);
        breadcrumb.scrollToTop();
        dropdownOptions = breadcrumb.getLastLevel().getDropdownOptions();
        assertEquals(new HashSet<String>(dropdownOptions), new HashSet<String>(tileNames));
        
        
        logger.info("Go to Notebook");
        
        t = landingPage.getNthTile(0);
        clickedTileName = t.getName();
        groupingAttribute = tilesToolbar.getCustomAttributeName();
        
        t.openNotebook();
        breadcrumb.waitForLoad();
        
        assertTrue(breadcrumb.getLevelsCount() == 3);
        checkLastLevelPathLink(groupingAttribute.toUpperCase(), clickedTileName);
        assertFalse(breadcrumb.getLastLevel().isDropdownPresent(),
            "There should be no dropdown options on Notebook page");
        
        
        logger.info("Checking that all breadcrumb path links except of last one are clickable");
        List<BreadcrumbLevel> bcLevels = breadcrumb.getLevels();
        assertFalse(bcLevels.get(0).isDisabled());
        assertFalse(bcLevels.get(1).isDisabled());
        
        
        logger.info("Go to 1st breadcrumb level (" + FIRST_LEVEL_LABEL + ")");
        
        groupingAttribute = bcLevels.get(1).getLabel();
        tileNames = bcLevels.get(1).getDropdownOptions();
        
        breadcrumb.goTo(0);
        breadcrumb.waitForLoad();
        
        assertTrue(breadcrumb.getLevelsCount() == 1);
        assertEquals(tilesToolbar.getCustomAttributeName().toUpperCase(), groupingAttribute);
        assertEquals(new HashSet<String>(tileNames), new HashSet<String>(dropdownOptions));
        assertTrue(breadcrumb.getLastLevel().isDropdownPresent());
        
        
        logger.info("Go to notebook from the 1st breadcrumb level (" + FIRST_LEVEL_LABEL + ")");
        
        t = landingPage.getNthTile(0);
        clickedTileName = t.getName();
        groupingAttribute = tilesToolbar.getCustomAttributeName();
        
        t.openNotebook();
        breadcrumb.waitForLoad();
        
        assertTrue(breadcrumb.getLevelsCount() == 2);
        checkLastLevelPathLink(groupingAttribute.toUpperCase(), clickedTileName);
        assertFalse(breadcrumb.getLastLevel().isDropdownPresent(),
                "There should be no dropdown options on Notebook page");
        
        
        logger.info("Go home");
        
        breadcrumb.goHome();
        assertFalse(breadcrumb.isBreadcrumbVisible(), "There should be no breadcrumb on top page");
    }
    
    private void checkLastLevelPathLink(String label, String value) {
        BreadcrumbLevel level = breadcrumb.getLastLevel();
        
        assertEquals(level.getLabel(), label);
        assertEquals(level.getValue(), value);
        assertTrue(level.isDisabled());
    }
}
