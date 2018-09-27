package com.ca.apm.test.atc.common.landing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.Select;

import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.Utils;
import com.ca.apm.test.atc.common.element.ElementConditionWrapper;
import com.ca.apm.test.atc.common.element.PageElement;
import com.ca.apm.test.atc.common.landing.Tile.ChartType;

public class ConfigurationPage extends ElementConditionWrapper {

    public ConfigurationPage(UI ui) {
        super(ui, By.className("experience-settings-container"));
    }

    private PageElement getNameInputFieldElement() {
        return findElement(By.cssSelector(".experience-settings-card-name input"));
    }

    public String getExperienceName() {
        return getNameInputFieldElement().getAttribute("value");
    }

    private PageElement getExperiencesCountNotice() {
        return findElement(By.className("experience-settings-entrypoints-count"));
    }

    public int getExperiencesCount() {
        String notice = getExperiencesCountNotice().getText();

        Matcher matcher = Pattern.compile("\\d+").matcher(notice);

        if (!matcher.find())
            throw new NumberFormatException("The sentence \"" + notice + "\" does not contain any number.");

        return Integer.parseInt(matcher.group());
    }

    public void setExperienceName(String experienceName) {
        getNameInputFieldElement().click();
        getNameInputFieldElement().sendKeys(Keys.chord(Keys.CONTROL, "a"), experienceName);
    }

    private Select getUniverseDropdown() {
        return new Select(ui.getElementProxy(By.className("t-config-universe-dropdown")));
    }

    private Select getChartTypeDropdown() {
        return new Select(ui.getElementProxy(By.className("t-config-charttype-dropdown")));
    }

    public ChartType getSelectedChartType() {
        return ChartType.getByName(getChartTypeDropdown().getFirstSelectedOption().getText());
    }

    public void selectChartType(ChartType chartType) {
        getChartTypeDropdown().selectByVisibleText(chartType.getChartName());
    }

    public void selectUniverseByName(String universeName) {
        getUniverseDropdown().selectByVisibleText(universeName);
        Utils.sleep(2000);
    }

    private List<PageElement> getGroupingAttributesDropdowns() {
        return findPageElements(By.cssSelector("div[ng-repeat].grouping-dialog-level-item select"));
    }

    private int getGroupingAttributesDropdownCount() {
        return getGroupingAttributesDropdowns().size();
    }

    private PageElement getGroupingAttributesDropdown(int level) {
        int index = level - 1;
        return getGroupingAttributesDropdowns().get(index);
    }

    private PageElement getAddLevelBtn() {
        return findElement(By.cssSelector(".groupingLevelsContainer .icon-plus"));
    }

    public void setGroupingAttributes(String[] attributeNames) {
        int num = 1;
        for (String attributeName : attributeNames) {
            if (num > getGroupingAttributesDropdownCount()) {
                getAddLevelBtn().click();
            }
            
            Utils.sleep(300);
            
            Select dropdownSelect = new Select(getGroupingAttributesDropdown(num));
            dropdownSelect.selectByVisibleText(attributeName);
                        
            num++;
        }
        
        if (num <= getGroupingAttributesDropdownCount()) {
            Select dropdownSelect = new Select(getGroupingAttributesDropdown(num));
            dropdownSelect.selectByIndex(0); // -- no option --
        }
    }

    public String[] getGroupingAttributes() {
        List<PageElement> dropdowns = getGroupingAttributesDropdowns();
        String[] attributes = new String[dropdowns.size()];
        for (int i = 0; i < dropdowns.size(); i++) {
            String attr = dropdowns.get(i).findElement(By.cssSelector("option[selected='selected']")).getAttribute("label");
            if (attr == null || attr.startsWith("--")) {
                return Arrays.copyOf(attributes, i);
            } else {
                attributes[i] = attr;
            }
        }
        return attributes;
    }

    public PageElement getSaveButton() {
        return findElement(By.cssSelector(".experience-settings-buttons .common-button-primary"));
    }

    public PageElement getDeleteButton() {
        return findElement(By.cssSelector(".experience-settings-buttons .common-button-delete"));
    }

    public PageElement getCancelButton() {
        return findElement(By.cssSelector(".experience-settings-buttons .common-button:not(.common-button-primary):not(.common-button-delete)"));
    }

    public List<Tile> getPreviewTiles() {
        List<Tile> tiles = new ArrayList<Tile>();
        List<PageElement> tileElements =
            findPageElements(
                By.cssSelector(".preview-experience-card-container div.experience-card-base.experience-card"));

        for (PageElement el : tileElements) {
            tiles.add(new Tile(ui, el));
        }
       
        return tiles;
    }

    public void waitForPreviewWorkIndicator() {
        ui.waitForWorkIndicator(By.className("experience-settings-loading-card"));
    }

    private PageElement getPublicCheckbox() throws Exception {
        return findElement(By.id("isPublicCheckbox"));
    }
    
    private PageElement getPublicCheckboxLabel() throws Exception {
        return findElement(By.cssSelector(".experience-settings-public-checkbox label"));
    }
    
    public void checkPublicCheckbox() throws Exception {
        PageElement el = getPublicCheckbox();
        if (!el.isSelected()) {
            getPublicCheckboxLabel().click();
        }
    }
    
    public void uncheckPublicCheckbox() throws Exception {
        PageElement el = getPublicCheckbox();
        if (el.isSelected()) {
            getPublicCheckboxLabel().click();
        }
    }
    
    public boolean isPublicCheckboxChecked() throws Exception {
        return getPublicCheckbox().isSelected();
    }
    
    public boolean isPublicCheckboxEnabled() throws Exception {
        return getPublicCheckbox().isEnabled();
    }
}
