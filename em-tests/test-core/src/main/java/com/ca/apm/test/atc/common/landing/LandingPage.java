package com.ca.apm.test.atc.common.landing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openqa.selenium.By;

import com.ca.apm.test.atc.common.JQueryDragAndDropSupport;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.Utils;
import com.ca.apm.test.atc.common.element.ElementConditionWrapper;
import com.ca.apm.test.atc.common.element.PageElement;

public class LandingPage extends ElementConditionWrapper {
    
    private BreadcrumbExperiences breadcrumbExperiences = null;
    private SummaryPanel summaryPanel = null;
    private ConfigurationPage configurationPage = null;
    private ATPanel atPanel = null;

    public LandingPage(UI ui) {
        super(ui, By.className("home-container"));
    }
    
    public BreadcrumbExperiences getBreadcrumbExperiences() {
        if (breadcrumbExperiences == null) {
            breadcrumbExperiences = new BreadcrumbExperiences(ui, this);
        }
        return breadcrumbExperiences;
    }

    public void waitForTilesToLoad(boolean waitForAnyTilePresent) {
        By locator = By.cssSelector(".home-main-section .home-main .work-indicator");
        ui.waitForWorkIndicator(locator);
        
        if (waitForAnyTilePresent) {
            ui.waitUntilVisible(By.className("experience-card"));
        }
    }
    
    public void waitForTilesToLoad() {
        waitForTilesToLoad(false);
    }
    
    public boolean isGlobalView() {
        return getAttribute("class").contains("t-global-view");
    }
    
    public List<String> getTileNames() {
        List<String> tileNames = new ArrayList<String>();
        for (Tile tile : getTiles()) {
            tileNames.add(tile.getName());
        }
        return tileNames;
    }

    public void drillDownToNotebook(String tileName) {
        getTileByName(tileName).openNotebook();
    }

    public void drillDownTheTile(String tileName) {
        getTileByNameSubstring(tileName).drillDown();
        waitForTilesToLoad(true);
    }
    
    public void drillDownToBusinessView(String tileName) {
        if (tileName == null) {
            getTiles().get(0).drillDown();
        } else {
            drillDownTheTile(tileName);
        }
    }
    
    public Tile selectTile(String tileName) {
        Tile selectedTile = getTileByName(tileName);
        selectedTile.select();
        Utils.sleep(800); // wait for animation to finish
        return selectedTile;
    }
    
    public List<Tile> getSelectedTiles() {
        List<Tile> tiles = getTiles();

        Iterator<Tile> iterator = tiles.iterator();
        while (iterator.hasNext()) {
            Tile tile = iterator.next();
            if (!tile.isSelected()) {
                iterator.remove();
            }
        }

        return tiles;
    }

    public boolean isFilteredViewOn() {
        boolean result = false;
        for (PageElement closeIcon : getCloseFilterIcons()) {
            result = result || closeIcon.isDisplayed();
        }
        return result;
    }

    public void cancelViewFilter() {
        for (PageElement closeIcon : getCloseFilterIcons()) {
            if (closeIcon.isDisplayed()) {
                closeIcon.click();
            }
        }
    }
    
    public List<Tile> getTiles() {
        return getTiles(false);
    }
    
    public Tile getRandomProblemTile() {
        for (Tile tile : getTiles()) {
            if (tile.getProblemsCount() > 0) {
                return tile;
            }
        }
        return null;
    }
    
    public List<Tile> getVisibleTiles() {
        return getTiles(true);
    }

    public List<Tile> getTiles(boolean visibleOnly) {
        waitForTilesToLoad();
        
        List<Tile> tiles = new ArrayList<Tile>();
        List<PageElement> tileElements =
            getHomeMainSectionDiv().findPageElements(
                By.cssSelector("div.experience-card-base.experience-card"));

        for (PageElement el : tileElements) {
            Tile t = new Tile(ui, el);
            if (visibleOnly) {
                if (t.isDisplayed()) {
                    tiles.add(t);
                }
            } else {
                tiles.add(t);
            }
        }
       
        return tiles;
    }
    
    /**
     * Get tiles that have data displayed
     */
    public List<Tile> getActiveTiles() {
        List<Tile> activeTiles = new ArrayList<>();
        
        for (Tile tile : getTiles(true)) {
            if (tile.hasNotebookLink()) {
                activeTiles.add(tile);
            }
        }
        
        return activeTiles;
    }
    
    public boolean isATPanelPresent() {
        return getATPanel().isPresent();
    }

    public ATPanel getATPanel() {
        if (atPanel == null) {
            atPanel = new ATPanel(ui, this);
        }
        return atPanel;
    }
    
    public SummaryPanel getSummaryPanel() {
        if (summaryPanel == null) {
            summaryPanel = new SummaryPanel(ui, this);
        }
        
        return summaryPanel;
    }

    public ConfigurationPage getConfigurationPage() {
        if (configurationPage == null) {
            configurationPage = new ConfigurationPage(ui);
        }
        return configurationPage;
    }

    private PageElement getHomeMainSectionDiv() {
        return findElement(By
            .cssSelector(".home-main-section .home-main"));
    }

    public Tile getTileByName(String tileName) {
        for (Tile tile : getTiles()) {
            if (tile.getName().equals(tileName.trim())) {
                return tile;
            }
        }
        
        return null;
    }
    
    public Tile getTileByNameSubstring(String tileName) {
        for (Tile tile : getTiles()) {
            if (tile.getName().contains(tileName.trim())) {
                return tile;
            }
        }
        
        return null;
    }
    
    public Tile getNthTile(int index) {
        return getTiles().get(index);
    }
    
    private List<PageElement> getCloseFilterIcons() {
        return findPageElements(By.cssSelector(".filter-enabled .icon-close"));
    }

    public PageElement getElement(By locator) {
        return findElement(locator);
    }
    
    public PageElement getHomeButton() {
        return findElement(By.className("home-button"));
    }
    
    public void clickHomeButton() {
        getHomeButton().click();
    }
    
    private String getCssSelectorOfNthTile(int index) {
        return "div.home-main div.experience-card[t-tile-ndx~=\"" + index + "\"]";
    }
    
    public void moveTile(int fromIndex, int toIndex) throws Exception {
        List<Tile> tiles = getTiles();
        Tile sourceTile = tiles.get(fromIndex);
        sourceTile.openToolsOverlay();
        
        String sourceTileCssSelector = getCssSelectorOfNthTile(fromIndex);
        String dndHandleCssSelector = ".experience-card-drag";
        String targetTileCssSelector = getCssSelectorOfNthTile(toIndex);

        JQueryDragAndDropSupport.performDragAndDrop(ui.getDriver(), sourceTileCssSelector, dndHandleCssSelector, targetTileCssSelector);
        
        // Wait while the drag-n-drop tile placeholder is visible
        ui.waitWhileVisible(By.cssSelector("experience-card-placeholder"));
    }

    public ConfigurationPage editTile(String tileName) {
        Tile tile = getTileByName(tileName);
        tile.openToolsOverlay();
        tile.getEditIcon().click();
        ConfigurationPage configPage = getConfigurationPage();
        configPage.waitForPreviewWorkIndicator();
        return configPage;
    }

    public PageElement getAddNewExperienceIcon() {
        return findElement(By.className("add-new-experience"));
    }

    public ConfigurationPage clickAddNewExperienceIcon() {
        getAddNewExperienceIcon().click();
        return getConfigurationPage();
    }
}
