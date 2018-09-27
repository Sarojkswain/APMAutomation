package com.ca.apm.test.atc.common.landing;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.Utils;
import com.ca.apm.test.atc.common.element.PageElement;
import com.ca.apm.test.atc.common.element.WebElementWrapper;

public class Tile extends WebElementWrapper {
      
    private final String name;
    
    private Histogram histogram;
    
    public enum ChartType {
        LINE_CHART("Average Response Time", "line-chart"), VOLUME_CHART("Transaction Volume", "volume-chart"), HISTOGRAM("Histogram", "histogram-directive");

        private final String chartName;
        private final String htmlElementName;
        
        private ChartType(String chartName, String htmlElementName) {
            this.chartName = chartName;
            this.htmlElementName = htmlElementName;
        }

        public String getChartName() {
            return chartName;
        }

        public String getHtmlElementName() {
            return htmlElementName;
        }
        
        public By getLocator() {
            return By.cssSelector(htmlElementName);
        }

        public static ChartType getByName(String name) {
            for (ChartType type : ChartType.values()) {
                if (type.getChartName().equals(name)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown chart type: " + name);
        }
    }
    
    public Tile(UI ui, PageElement el) {
        super(el, ui);
        this.name = el.findElement(getExperienceCardHeadingLinkLocator()).getText(); 
    }

    public String getName() {
        return name;
    }
    
    public void select() {
        findElement(By.className("title-problem-container")).click();
        Utils.sleep(800); // wait for animation to finish
    }

    public void unselect() {
        findElement(By.cssSelector(".experience-card-heading .icon-close")).click();
        Utils.sleep(800);  // wait for animation
    }
    
    /**
     * 
     * @return
     *   <b>true</b> in case there is next drill-down level
     *   <br><b>false</b> in case there is no next drill-down level and link leads to notebook page
     */
    public boolean hasNextDrilldownLevel() {
        PageElement headingLink = findElement(getExperienceCardHeadingLinkLocator());
        return !headingLink.getAttribute("class").contains("disabled");
    }
    
    public void drillDown() {
        PageElement cardHeadingLinkElement = findElement(getExperienceCardHeadingLinkLocator());
        cardHeadingLinkElement.scrollIntoView();
        cardHeadingLinkElement.click();
    }
    
    public boolean hasNotebookLink() {
        return findElement(getOpenNotebookLinkLocator()).isPresent();
    }
    
    public void openNotebook() {
        PageElement openNotebookElem = findElement(getOpenNotebookLinkLocator());
        openNotebookElem.scrollIntoView();
        openNotebookElem.click();
        
        ui.getCanvas().waitForUpdate();
    }
    
    public Histogram getHistogram() {
        if (histogram == null) {
            histogram = new Histogram(ui, this);
        }
        
        changeChartTo(ChartType.HISTOGRAM);
        return histogram;
    }
    
    public int getHistogramFailedVolume() {
        PageElement volumeCountElement = findElement(By.className("t-failed-trx-cnt"));
        
        if (volumeCountElement.isDisplayed()) {
            return LandingUtils.getValueFromFormattedString(volumeCountElement.getText());
        }

        return 0;
    }
    
    @Override
    public boolean isSelected() {
        return findElement(By.className("action-links-container")).isDisplayed();
    }
    
    public ShareUrlBox openShareUrlBox() {
        // click icon
        PageElement toggleBtnEl = findElement(By.cssSelector(".action-links-container .t-copy-url-dialog-toggle-btn"));
        toggleBtnEl.click();
        Utils.sleep(150);
        return new ShareUrlBox(ui, this);
    }
    
    private By getExperienceCardHeadingLinkLocator() {
        return By.className("experience-card-heading-link");
    }
    
    private By getOpenNotebookLinkLocator() {
        return By.className("t-open-notebook");
    }
        
    public PageElement getProblemContainer() {
        return findElement(By.className("title-problem-container"));
    }
    
    public PageElement getProblemsCountElement() {
        return getProblemContainer().findElement(By.className("problems-detected-card"));
    }
    
    public PageElement getAnomaliesCountElement() {
        return getProblemContainer().findElement(By.className("comp-cards-anomalies-number"));
    }

    public int getProblemsCount() {
        return Integer.valueOf(getProblemsCountElement().getText().split(" ")[0].trim());
    }
    
    public int getAnomaliesCount() {
        return Integer.valueOf(getAnomaliesCountElement().getText().trim());
    }
    
    public boolean isTileInDangerState() {
        return findPageElements(By.className("icon-minus-circle")).size() > 0;
    }
    
    public boolean containsBtVolumeContainer() {
        List<PageElement> elements = findPageElements(By.className("bt-volume-container"));
        return !elements.isEmpty();
    }
    
    public boolean hasCharts() {
        return findElement(By.className("experience-card-graphs-container")).isDisplayed();
    }
    
    public PageElement getAppdexElement() {
        return findElement(By.className("card-apdex-number"));
    }
    
    public int getAppdex() {
        return Integer.valueOf(getAppdexElement().getText());
    }
    
    public PageElement getAllTransactionsVolumeElement() {
        return findElement(By.className("t-transactions-volume"));
    }
    
    public int getAllTransactionsVolume() {
        return LandingUtils.getValueFromFormattedString(getAllTransactionsVolumeElement().getText().trim());
    }
    
    public PageElement getPoorTransactionsVolumeElement() {
        return findElement(By.className("t-poor-transactions"));
    }
    
    public int getPoorTransactionsVolume() {
        return Integer.valueOf(getPoorTransactionsVolumeElement().getText());
    }
    
    public boolean isToolsOverlayIcon() {
        return findPageElements(By.className("icon-chevron-circle-up")).size() > 0;
    }
    
    public PageElement getToolsOverlayIcon() {
        return findElement(By.className("icon-gear"));
    }
    
    public PageElement getToolsOverlayCloseIcon() {
        return findElement(By.cssSelector(".reveal-tools-container .icon-close"));
    }
    
    public boolean isMoveCardIcon() {
        return findPageElements(By.className("t-move-card")).size() > 0;
    }
    
    public By getToolsOverlaySelector() {
        return By.className("reveal-tools-container");
    }

    public PageElement getToolsOverlay() {
        return findElement(By.className("reveal-tools-container"));
    }
    
    public void openToolsOverlay() {
        if (!getToolsOverlay().isDisplayed()) {
            getToolsOverlayIcon().click();
            getToolsOverlay().waitUntilVisible();
        }
    }
    
    public void closeToolsOverlay() {
        if (getToolsOverlay().isDisplayed()) {
            getToolsOverlayCloseIcon().click();
            ui.waitWhileVisible(getToolsOverlaySelector());
        }
    }
    
    public PageElement getDragIcon() {
        return findElement(By.className("experience-card-drag"));
    }

    public PageElement getEditIcon() {
        return findElement(By.className("experience-card-edit"));
    }

    public PageElement getDots() {
        return findElement(By.cssSelector(".view-dots-container .dots"));
    }
    
    public void clickDots() {
        getDots().click();
    }

    public ChartType getChartType() {
        for (ChartType chartType : ChartType.values()) {
            try {
                if (getChartType(chartType).isDisplayed()) {
                    return chartType;
                }
            } catch (NoSuchElementException e) {
                // continue
            }
        }
        return null;
    }
    
    public PageElement getChartType(ChartType chartType) {
        return findElement(chartType.getLocator());
    }
    
    public void changeChartTo(ChartType chartType) {
        int attempts = 3; 
        while (!findElement(chartType.getLocator()).isDisplayed()) {
            clickDots();
            Utils.sleep(100);
            attempts--;
            
            if (attempts == 0) {
                throw new IllegalStateException("Could not select the " + chartType + " chart type in tile " + name + ".");
            }
        }
    }
}
