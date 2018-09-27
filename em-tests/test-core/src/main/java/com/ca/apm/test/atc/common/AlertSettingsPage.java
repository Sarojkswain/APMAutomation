package com.ca.apm.test.atc.common;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import com.ca.apm.test.atc.common.element.ATCDropDownOpener;
import com.ca.apm.test.atc.common.element.PageElement;
import com.wily.introscope.appmap.rest.entities.Alert;
import com.wily.introscope.appmap.rest.entities.AlertMetricGrouping;

public class AlertSettingsPage {

    private UI ui;
    
    public AlertSettingsPage(UI ui) {
        this.ui = ui;
    }
    
    private By getModalDeleteBtnLocator() {
        return By.cssSelector(".modal-footer button[type=submit].common-button-primary");
    }
    
    private By getAlertRowLocator(String alertName) {
        String xpath = "//div[contains(@class,'t-alert-row-name') and contains(@class, 'ui-grid-cell-contents')]";
        xpath += "//span[text()='" + alertName + "']/../../../..";
        return By.xpath(xpath);
    }

    private PageElement getGridTableElement() {
        return ui.findElement(By.className("alerts-grid"));
    }
    
    private PageElement getTableRowElement(String alertName) {
        return getGridTableElement().findElement(getAlertRowLocator(alertName));
    }
    
    private PageElement getAlertFormElement(String alertName) {
        return getTableRowElement(alertName).findElement(
            By.cssSelector("form[name=alertForm]"));
    }
    
    public void waitForWorkIndicator() {
        ui.waitForWorkIndicator(By.id("alertsWorkIndicator"));
    }
    
    public boolean alertExists(String alertName) {
        By alertRowLocator = getAlertRowLocator(alertName);
        return getGridTableElement().findElements(alertRowLocator).size() > 0;
    }
    
    private PageElement getRowNameElem(String alertName) {
        return getTableRowElement(alertName).findElement(By.className("t-alert-row-name"));
    }
    
    public boolean isRowExpanded(String alertName) {
        final String EXPANDED_ICON_CLASS = "combo-filter-marker-active";
        return getRowNameElem(alertName).findElements(By.className(EXPANDED_ICON_CLASS)).size() > 0;
    }
    
    public void expandRow(String alertName) {
        if (!isRowExpanded(alertName)) {
            getRowNameElem(alertName).click();
            Utils.sleep(100);  // TODO
        }
    }
    
    public int getExpandedRowsCount() {
        return ui.findElements(By.className("expandableRow")).size();
    }
    
    public void collapseRow(String alertName) {
        if (isRowExpanded(alertName)) {
            getTableRowElement(alertName).click();
            Utils.sleep(100);  // TODO
        }
    }
    
    public boolean areAdvancedOptionsShown(String alertName) {
        return getAlertFormElement(alertName).findElement(
            By.className("t-alert-show-less-options")).isDisplayed();
        
    }
    
    public void showAdvancedOptions(String alertName) {
        if (!areAdvancedOptionsShown(alertName)) {
            PageElement showMoreBtn = getAlertFormElement(alertName).findElement(
                By.className("t-alert-show-more-options"));
            showMoreBtn.click();
            Utils.sleep(100);  // TODO
        }
    }
    
    public void hideAdvancedOptions(String alertName) {
        if (areAdvancedOptionsShown(alertName)) {
            PageElement showLessBtn = getAlertFormElement(alertName).findElement(
                By.className("t-alert-show-less-options"));
            showLessBtn.click();
            Utils.sleep(100);  // TODO
        }
    }
    
    public String getNameOfFirstAlert() {
        PageElement nameCell = getGridTableElement().findElement(
            By.cssSelector(".t-alert-row-name.ui-grid-cell-contents"));
        return nameCell.getText().trim();
    }
    
    public void createAlert(Alert alert) throws Exception {
        String alertName = alert.getName();
        if (alertExists(alertName)) {
            throw new Exception("Alert already exists.");
        }
        
        addNewAlert();
        fillAlertForm("", alert);        
        clickCreateAlertButton(alertName);
    }
    
    public void updateAlert(Alert alert) throws Exception {
        String alertName = alert.getName();
        if (!alertExists(alertName)) {
            throw new Exception("Alert does not exist.");
        }
        
        expandRow(alertName);
        fillAlertForm(alertName, alert);
        clickSaveAlertButton(alertName);
    }
    
    public void deleteAlert(String alertName) throws Exception {
        if (!alertExists(alertName)) {
            throw new Exception("Alert does not exist.");
        }
        
        expandRow(alertName);
        clickDeleteAlertButton(alertName);
        if (!alertName.isEmpty() || isResetButtonEnabled(alertName)) {
            ui.findElement(getModalDeleteBtnLocator()).click();
            ui.waitWhileVisible(getModalDeleteBtnLocator());
        }
    }
    
    public void addNewAlert() {
        ui.findElement(By.className("t-add-alert")).click();
        ui.waitUntilVisible(By.cssSelector("input:invalid.t-alert-form-name"));
    }
    
    public boolean areFormButtonsPresent(String alertName) {
        return getAlertFormElement(alertName).findElements(
            By.className("t-alert-form-buttons")).size() > 0;
    }
    
    private PageElement getCreateAlertBtn(String alertName) {
        return getAlertFormElement(alertName).findElement(
            By.className("t-alert-create"));
    }
    
    public boolean isCreateButtonEnabled(String alertName) {
        return getCreateAlertBtn(alertName).isEnabled();
    }
    
    public void clickCreateAlertButton(String alertName) {
        PageElement createBtn = getCreateAlertBtn(alertName);
        if (createBtn.isEnabled()) {
            createBtn.click();
            waitForWorkIndicator();
        }
    }
    
    private PageElement getSaveAlertBtn(String alertName) {
        return getAlertFormElement(alertName).findElement(
            By.className("t-alert-save"));
    }
    
    public boolean isSaveButtonEnabled(String alertName) {
        return getSaveAlertBtn(alertName).isEnabled();
    }
    
    public void clickSaveAlertButton(String alertName) {
        PageElement saveBtn = getSaveAlertBtn(alertName);
        if (saveBtn.isEnabled()) {
            saveBtn.click();
            waitForWorkIndicator();
        }
    }
    
    private PageElement getResetAlertBtn(String alertName) {
        return getAlertFormElement(alertName).findElement(
            By.className("t-alert-reset"));
    }
    
    public boolean isResetButtonEnabled(String alertName) {
        return getResetAlertBtn(alertName).isEnabled();
    }
    
    public void clickDeleteAlertButton(String alertName) {
        PageElement delBtn = getAlertFormElement(alertName).findElement(
            By.className("t-alert-delete"));
        if (delBtn.isEnabled()) {
            delBtn.click();
            if (!alertName.isEmpty() || isResetButtonEnabled(alertName)) { 
                ui.waitUntilVisible(getModalDeleteBtnLocator());
            }
        }
    }
    
    public String getRowDescription(String alertName) {
        PageElement desc = getTableRowElement(alertName).findElement(
            By.className("t-alert-row-desc"));
        return desc.getText();
    }
    
    private PageElement getFormNameElem(String alertName) {
        return getAlertFormElement(alertName).findElement(By.className("t-alert-form-name"));
    }
    
    public boolean isFormNameValid(String alertName) {
        return isElementValid(getFormNameElem(alertName));
    }
    
    public void setFormName(String origAlertName, String newAlertName) {
        // using the clipboard to set Alert name because of the problem with name validation:
        // angular 1.5.7 will screw up validation classes if the text is being typed too quickly
        PageElement nameElem = getFormNameElem(origAlertName);
        pasteValue(nameElem, newAlertName);
    }
    
    private PageElement getFormDescriptionElem(String alertName) {
        PageElement alertForm = getAlertFormElement(alertName);
        return alertForm.findElement(By.className("t-alert-form-desc"));
    }
    
    public boolean isFormDescriptionValid(String alertName) {
        return isElementValid(getFormDescriptionElem(alertName));
    }
    
    public String getFormDescription(String alertName) {
        return getFormDescriptionElem(alertName).getText();
    }
    
    public void setFormDescription(String alertName, String description) {
        PageElement descElem = getFormDescriptionElem(alertName);
        // use system clipboard for setting the description,
        // element.sendKeys is too slow in case of long strings
        pasteValue(descElem, description);
    }
    
    public int getMetricGroupingsCount(String alertName) {
        return getAlertFormElement(alertName).findElements(
            By.cssSelector(".t-alert-mgroupings .t-alert-aspecifier")).size();
    }
    
    public void addMetricGrouping(String alertName) {
        getAlertFormElement(alertName).findElement(By.className("t-alert-plus")).click();
        Utils.sleep(100);  // TODO
    }
    
    public void removeMetricGrouping(String alertName, int index) {
        // TODO
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    private PageElement getAgentSpecifierElem(String alertName, int index) {
        int offset = index + 2;  // 1 + 1 (selector indexing from 1 + metric groupings header)
        return getAlertFormElement(alertName).findElement(
            By.cssSelector(".t-alert-mgroupings:nth-child(" + offset + ") .t-alert-aspecifier"));
    }
    
    public boolean isAgentSpecifierValid(String alertName, int index) {
        return isElementValid(getAgentSpecifierElem(alertName, index));
    }
    
    public String getAgentSpecifier(String alertName, int index) {
        return getAgentSpecifierElem(alertName, index).getText();
    }
    
    public void setAgentSpecifier(String alertName, int index, String value) {
        PageElement agentSpecifier = getAgentSpecifierElem(alertName, index);
        agentSpecifier.clear();
        agentSpecifier.sendKeys(value);
    }
    
    private PageElement getMetricSpecifierElem(String alertName, int index) {
        int offset = index + 2;  // 1 + 1 (selector indexing from 1 + metric groupings header)
        return getAlertFormElement(alertName).findElement(
            By.cssSelector(".t-alert-mgroupings:nth-child(" + offset + ") .t-alert-mspecifier"));
    }
    
    public boolean isMetricSpecifierValid(String alertName, int index) {
        return isElementValid(getMetricSpecifierElem(alertName, index));
    }
    
    public String getMetricSpecifier(String alertName, int index) {
        return getMetricSpecifierElem(alertName, index).getText();
    }
    
    public void setMetricSpecifier(String alertName, int index, String value) {
        PageElement metricSpecifier = getMetricSpecifierElem(alertName, index);
        metricSpecifier.clear();
        metricSpecifier.sendKeys(value);
    }
    
    private PageElement getComparisonOperatorElem(String alertName) {
        return getAlertFormElement(alertName).findElement(
            By.className("t-alert-comparison-dropdown"));
    }
    
    public String getComparisonOperator(String alertName) {
        return new ATCDropDownOpener(getComparisonOperatorElem(alertName)).getSelectedOption();
    }
    
    public void selectComparisonOperator(String alertName, String labelToSelect) {
        PageElement coDropdownElem = getComparisonOperatorElem(alertName);
        ATCDropDownOpener coDropdown = new ATCDropDownOpener(coDropdownElem);
        if (!coDropdown.getSelectedOption().equals(labelToSelect)) {
            By menuLocator = By.id("t-alert-comparison-dropdown");
            coDropdown.selectFromDropdown(menuLocator, labelToSelect);
        }
    }
    
    private PageElement getDangerThresholdElem(String alertName) {
        return getAlertFormElement(alertName).findElement(
            By.className("t-alert-error-threshold"));
    }
    
    public boolean isDangerThresholdValid(String alertName) {
        return isElementValid(getDangerThresholdElem(alertName));
    }
    
    public String getDangerThreshold(String alertName) {
        return getDangerThresholdElem(alertName).getAttribute("value");
    }
    
    public void setDangerThreshold(String alertName, String value) {
        PageElement dt = getDangerThresholdElem(alertName);
        dt.clear();
        dt.sendKeys(value);
    }
    
    private PageElement getDangerPeriodsOverThresholdElem(String alertName) {
        return getAlertFormElement(alertName).findElement(
            By.className("t-alert-error-pot"));
    }
    
    public boolean isDangerPeriodsOverThresholdValid(String alertName) {
        return isElementValid(getDangerPeriodsOverThresholdElem(alertName));
    }
    
    public String getDangerPeriodsOverThreshold(String alertName) {
        return getDangerPeriodsOverThresholdElem(alertName).getAttribute("value");
    }
    
    public void setDangerPeriodsOverThreshold(String alertName, String value) {
        PageElement dPOT = getDangerPeriodsOverThresholdElem(alertName);
        dPOT.clear();
        dPOT.sendKeys(value);
    }
    
    private PageElement getDangerObservedPeriodsElem(String alertName) {
        return getAlertFormElement(alertName).findElement(
            By.className("t-alert-error-op"));
    }
    
    public boolean isDangerObservedPeriodsValid(String alertName) {
        return isElementValid(getDangerObservedPeriodsElem(alertName));
    }
    
    public String getDangerObservedPeriods(String alertName) {
        return getDangerObservedPeriodsElem(alertName).getAttribute("value");
    }
    
    public void setDangerObservedPeriods(String alertName, String value) {
        PageElement dOP = getDangerObservedPeriodsElem(alertName);
        dOP.clear();
        dOP.sendKeys(value);
    }
    
    private PageElement getCautionThresholdElem(String alertName) {
        return getAlertFormElement(alertName).findElement(
            By.className("t-alert-warn-threshold"));
    }
    
    public boolean isCautionThresholdValid(String alertName) {
        return isElementValid(getCautionThresholdElem(alertName));
    }
    
    public String getCautionThreshold(String alertName) {
        return getCautionThresholdElem(alertName).getAttribute("value");
    }
    
    public void setCautionThreshold(String alertName, String value) {
        PageElement ct = getCautionThresholdElem(alertName);
        ct.clear();
        ct.sendKeys(value);
    }
    
    private PageElement getCautionPeriodsOverThresholdElem(String alertName) {
        return getAlertFormElement(alertName).findElement(
            By.className("t-alert-warn-pot"));
    }
    
    public boolean isCautionPeriodsOverThresholdValid(String alertName) {
        return isElementValid(getCautionPeriodsOverThresholdElem(alertName));
    }
    
    public String getCautionPeriodsOverThreshold(String alertName) {
        return getCautionPeriodsOverThresholdElem(alertName).getAttribute("value");
    }
    
    public void setCautionPeriodsOverThreshold(String alertName, String value) {
        PageElement cPOT = getCautionPeriodsOverThresholdElem(alertName);
        cPOT.clear();
        cPOT.sendKeys(value);
    }
    
    private PageElement getCautionObservedPeriodsElem(String alertName) {
        return getAlertFormElement(alertName).findElement(
            By.className("t-alert-warn-op"));
    }
    
    public boolean isCautionObservedPeriodsValid(String alertName) {
        return isElementValid(getCautionObservedPeriodsElem(alertName));
    }
    
    public String getCautionObservedPeriods(String alertName) {
        return getCautionObservedPeriodsElem(alertName).getAttribute("value");
    }
    
    public void setCautionObservedPeriods(String alertName, String value) {
        PageElement cOP = getCautionObservedPeriodsElem(alertName);
        cOP.clear();
        cOP.sendKeys(value);
    }
    
    public String getUrlAlertName() {
        Pattern pattern = Pattern.compile("\\?alert=(.*?)&");
        Matcher matcher = pattern.matcher(ui.getCurrentUrl());

        if (matcher.find()) {
            try {
                return URLDecoder.decode(matcher.group(1), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return matcher.group(1);
            }
        }
        
        return null;
    }
    
    private void fillAlertForm(String origAlertName, Alert alert) {
        String alertName = alert.getName();
        
        setFormName(origAlertName, alertName);
        setFormDescription(alertName, alert.getDescription());
        
        List<AlertMetricGrouping> mg = alert.getMetricGroupings();
        int mgRowsLen = getMetricGroupingsCount(alertName);
        for (int i = 0; i < mg.size(); i++) {
            if (i >= mgRowsLen) {                
                addMetricGrouping(alertName);
            }
            setAgentSpecifier(alertName, i, mg.get(i).getAgentExpression());
            setMetricSpecifier(alertName, i, mg.get(i).getMetricExpression());
        }
        
        showAdvancedOptions(alertName);
        setDangerThreshold(alertName,
            Long.toString(alert.getErrorThreshold().getThreshold()));
        setDangerPeriodsOverThreshold(alertName,
            Integer.toString(alert.getErrorThreshold().getPeriodsOverThreshold()));
        setDangerObservedPeriods(alertName,
            Integer.toString(alert.getErrorThreshold().getObservedPeriods()));
        
        setCautionThreshold(alertName,
            Long.toString(alert.getWarningThreshold().getThreshold()));
        setCautionPeriodsOverThreshold(alertName,
            Integer.toString(alert.getWarningThreshold().getPeriodsOverThreshold()));
        setCautionObservedPeriods(alertName,
            Integer.toString(alert.getWarningThreshold().getObservedPeriods()));
    }
    
    private void pasteValue(PageElement element, String value) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(value), null);
        
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        element.sendKeys(Keys.chord(Keys.CONTROL, "v"));
    }
    
    private boolean isElementValid(PageElement element) {
        return element.getAttribute("class").matches("(^|.*\\s)ng-valid(\\s.*|$)");
    }
}
