package com.ca.apm.test.atc.landing;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.UI.View;
import com.ca.apm.test.atc.common.Utils;
import com.ca.apm.test.atc.common.landing.ATPanel;
import com.ca.apm.test.atc.common.landing.LandingPage;
import com.ca.apm.test.atc.common.landing.Notebook;
import com.ca.apm.test.atc.common.landing.Story;
import com.ca.apm.test.atc.common.landing.SummaryPanel.TimeRange;

public class ATPanelTest extends UITest {

    private final static Logger logger = Logger.getLogger(ATPanelTest.class);
    
    private LandingPage landingPage;
    private Notebook notebook;
    
    public void init() throws Exception {
        UI ui = getUI();
        ui.login();
        notebook = ui.getNotebook();
        landingPage = ui.getLandingPage();
        landingPage.getSummaryPanel().setTimeRange(TimeRange.CUSTOM_RANGE);
    }
    
    @Test
    public void testExpandedStory() throws Exception {
        init();
        
        logger.info("Checking AT panel presence in Global view.");
        assertTrue(landingPage.isGlobalView());
        assertFalse(landingPage.isATPanelPresent());
        
        
        logger.info("Go to Business view.");
        landingPage.getNthTile(0).drillDown();
        assertTrue(landingPage.isATPanelPresent());
        
        testProblemAndAnomalyStory(landingPage.getATPanel(), View.HOMEPAGE);
        
        // TODO check number of stories if expanded
        
        
        logger.info("Go Notebook.");
        Story problemStory = landingPage.getATPanel().getNthProblemGroup(0).getNthStory(0);
        problemStory.expand();
        problemStory.openNotebook();
        assertTrue(landingPage.isATPanelPresent());
        
        testProblemAndAnomalyStory(notebook.getATPanel(), View.NOTEBOOK);
        
        // TODO check number of stories if expanded
    }
    
    private void testProblemAndAnomalyStory(ATPanel atPanel, View view) {
        atPanel.expand();
        
        Story problemStory = atPanel.getNthProblemGroup(0).getNthStory(0);
        testStory(problemStory, view);
        
        Story anomalyStory = atPanel.getNthAnomalyGroup(0).getNthStory(0);
        testStory(anomalyStory, view);
    }
    
    /**
     * 
     * @param story
     * @param isNotebook - <b>true</b> - notebook, <b>false</b> - business view
     */
    private void testStory(Story story, View view) {
        logger.info("Checking story collapsing/expanding.");
        
        story.expand();
        assertTrue(story.isExpanded());
        
        story.collapse();
        assertFalse(story.isExpanded());
        
        story.expand();
        landingPage.cancelViewFilter();
        Utils.sleep(100);  // wait for story to collapse
        assertFalse(story.isExpanded());
        
        
        logger.info("Checking expanded story content.");
        story.expand();
        
        String firstAppeared = story.getFirstAppeared(); 
        assertNotNull(firstAppeared);
        assertFalse(firstAppeared.isEmpty());
        
        String lastAppeared = story.getFirstAppeared(); 
        assertNotNull(lastAppeared);
        assertFalse(lastAppeared.isEmpty());
        
        if (story.areOwnersPresent()) {
            assertTrue(story.getOwners().size() > 0, "Owners list cannot be empty.");
        }
        
        String acStr = story.getAffectedComponentsString();
        assertNotNull(acStr);
        assertFalse(acStr.isEmpty());
        assertTrue(acStr.startsWith(story.getAffectedComponentsNumber()),
            "The number in Affected components string is not equal to number in description.");
        
        assertTrue(story.openShareUrlBox().isPresent());
        
        if (view.equals(View.NOTEBOOK)) {
            // TODO
            // evidences present?
            // check evidences number
        }
        
        story.collapse();
    }
}
