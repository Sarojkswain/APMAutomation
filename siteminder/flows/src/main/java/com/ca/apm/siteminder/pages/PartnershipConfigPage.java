/*
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 *
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.siteminder.pages;

import junit.framework.Assert;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.LoggerFactory;

/**
 * @author Sundeep (bhusu01)
 */
public class PartnershipConfigPage {

    private static final org.slf4j.Logger LOGGER =
        LoggerFactory.getLogger(PartnershipConfigPage.class);

    WebDriver driver;
    WebDriverWait wait;

    @FindBy(id = "main:fedPartnerSubView:partnershipsTable:fedCreatePartnership0_centerCell")
    private WebElement createPartnershipButton;

    @FindBy(xpath = "/html/body/div[3]/div/div/div/div/table/tbody/tr/td/table/tbody/tr/td/form/div/table[2]/tbody/tr[3]/td/div/table/tbody/tr/td/table[1]/tbody/tr/td[3]/div/table/tbody/tr/td/div[2]/table/tbody/tr[1]/td[2]/a")
    private WebElement idpToSPPartnershipButton;

    @FindBy(id = "main:NameInput")
    private WebElement partnershipNameInput;

    @FindBy(id = "main:IdPSelect")
    private WebElement idpSelectDropDown;

    @FindBy(id = "main:SPSelect")
    private WebElement spSelectDropDown;

    @FindBy(id = "horizontal_leftToRight_shuttlControlIDmain:UDShuttle")
    private WebElement selectDefaultDirectory;

    @FindBy(id = "main:i2swizidnext_bottom")
    private WebElement nextButton;

    @FindBy(id = "main:NameIDFormatSelect")
    private WebElement nameIDFormatDropDown;

    @FindBy(id = "main:NameIDValueInput")
    private WebElement nameIDValueInput;

    @FindBy(id = "main:AssnAttrTable:0:AssnAttrTypeSelect")
    private WebElement attributeTypeDropDown;

    @FindBy(id = "main:AssnAttrTable:0:AssnAttrValueInput")
    private WebElement attr0ValueInput;

    @FindBy(id = "main:AssnAttrTable:1:AssnAttrValueInput")
    private WebElement attr1ValueInput;

    @FindBy(id = "main:AuthURLInput")
    private WebElement authURLInput;

    @FindBy(id = "main:AuthnRequestBindingPostCheckbox")
    private WebElement requestBindingPOSTCheckBox;

    @FindBy(id = "main:SSOBindingPostCheckbox")
    private WebElement responseBindingPOSTCheckBox;

    @FindBy(id = "main:SigningAliasLinkGenerate_anchor")
    private WebElement generateKeyButton;

    @FindBy(id = "main:certrequest:AliasInpt")
    private WebElement aliasNameValue;

    @FindBy(id = "main:certrequest:NameInpt")
    private WebElement requesterNameInput;

    @FindBy(id = "main:certrequest:OrgUnitInpt")
    private WebElement orgUnitValue;

    @FindBy(id = "main:certrequest:OrgInpt")
    private WebElement orgValue;

    @FindBy(id = "main:certrequest:SizeMenu")
    private WebElement keySizeDropDown;

    @FindBy(id = "main:certrequest:PgHdr_SaveBtn")
    private WebElement saveButton;

    @FindBy(id = "main:certrequest:_idJsp3_anchor")
    private WebElement backToStep5Button;

    @FindBy(id = "main:SigningAliasSelect")
    private WebElement signingAliasDropDown;

    @FindBy(id = "main:ReqSignedAuthnReqsCheckbox")
    private WebElement requireSignedRequestsCheckbox;

    @FindBy(id = "main:DisableSigningCheckbox")
    private WebElement disableSignatureProcessingCheckBox;

    @FindBy(id = "main:i2swizidfinish")
    private WebElement finishButton;

    @FindBy(id = "main:fedPartnerSubView:partnershipsTable:0:fedListdd_centerCell")
    private WebElement actionButton;

    @FindBy(xpath = "/html/body/div[3]/div/div/div/div/table/tbody/tr/td/table/tbody/tr/td/form/div/table[2]/tbody/tr[3]/td/div/table/tbody/tr/td/table[3]/tbody/tr/td/div/table/tbody/tr[1]/td[1]/table/tbody/tr/td/div[2]/table/tbody/tr[5]/td[2]/a")
    private WebElement activateActionButton;

    @FindBy(id = "main:fedPartnerSubView:activateConfirmMsgBoxOk")
    private WebElement confirmActivateButton;

    @FindBy(id = "main:fedPartnerSubView:partnershipsTable:0:_idJsp29")
    private WebElement partnershipStatus;
    
    @FindBy(id = "main:fedPartnerSubView:searchValInput")
    private WebElement searchInput;
    
    @FindBy(id = "main:fedPartnerSubView:fedSearchbut")
    private WebElement searchButton;
    
    @FindBy(id = "main:fedPartnerSubView:PartnershipSearchResetButton")
    private WebElement searchResetButton;

    public PartnershipConfigPage(WebDriver driver) {
        this.driver = driver;
        
        String currentURL = driver.getCurrentUrl();
        int sep = currentURL.indexOf('?');
        String partnerPageURL = currentURL.substring(0, sep+1) + "task.tag=ConfigurePartnerships";
        driver.get(partnerPageURL);
        LOGGER.info("Navigated to " + partnerPageURL);

        PageFactory.initElements(driver, this);
        LOGGER.info("Initialized partnership config page");
        wait = new WebDriverWait(driver, 20);
    }

    public void createIdPtoSPPartnership(String partnershipName, String spEntityId, String nameIDValue, String principalAttribValue, String groupAttribValue, String authURLValue) {
        // Step 1
        wait.until(ExpectedConditions.visibilityOf(createPartnershipButton));
        createPartnershipButton.click();
        idpToSPPartnershipButton.click();
        PageFactory.initElements(driver, this);
        LOGGER.info("Reached at " + driver.getCurrentUrl());
        wait.until(ExpectedConditions.visibilityOf(partnershipNameInput));
        partnershipNameInput.sendKeys(partnershipName);
        idpSelectDropDown.sendKeys("s");
        spSelectDropDown.sendKeys(spEntityId);
        selectDefaultDirectory.click();
        nextButton.click();
        LOGGER.info("Step 1 finished");

        // Step 2
        LOGGER.info("Reached at " + driver.getCurrentUrl());
        PageFactory.initElements(driver, this);
        wait.until(ExpectedConditions.visibilityOf(nextButton));
        nextButton.click();
        LOGGER.info("Step 2 finished");

        // Step 3
        PageFactory.initElements(driver, this);
        wait.until(ExpectedConditions.visibilityOf(nameIDFormatDropDown));
        nameIDFormatDropDown.sendKeys("t");
        nameIDValueInput.sendKeys(nameIDValue);
        //attributeTypeDropDown.sendKeys("u");
        attr0ValueInput.sendKeys(principalAttribValue);
        attr1ValueInput.sendKeys(groupAttribValue);
        nextButton.click();
        LOGGER.info("Step 3 finished");

        // Step 4
        PageFactory.initElements(driver, this);
        wait.until(ExpectedConditions.visibilityOf(authURLInput));
        authURLInput.sendKeys(authURLValue);
        requestBindingPOSTCheckBox.click();
        responseBindingPOSTCheckBox.click();
        nextButton.click();
        LOGGER.info("Step 4 finished");

        // Step 5
        PageFactory.initElements(driver, this);
        wait.until(ExpectedConditions.visibilityOf(disableSignatureProcessingCheckBox));
        disableSignatureProcessingCheckBox.click();

        /*generateKeyButton.click();

        // Detour to create a new alias
        PageFactory.initElements(driver, this);
        wait.until(ExpectedConditions.visibilityOf(aliasNameValue));
        aliasNameValue.sendKeys("siteminder_key");
        requesterNameInput.sendKeys("apm_for_sm");
        orgUnitValue.sendKeys("APM");
        orgUnitValue.sendKeys("CATech");
        keySizeDropDown.sendKeys("2");
        saveButton.click();
        LOGGER.info("Created new alias");
        backToStep5Button.click();

        LOGGER.info("Returend back to step 5");
        PageFactory.initElements(driver, this);
        wait.until(ExpectedConditions.visibilityOf(signingAliasDropDown));
        signingAliasDropDown.sendKeys("s");
        requireSignedRequestsCheckbox.click();
        */

        nextButton.click();
        LOGGER.info("Step 5 finished");

        // Step 6, last step
        PageFactory.initElements(driver, this);
        wait.until(ExpectedConditions.visibilityOf(finishButton));
        finishButton.click();
        LOGGER.info("Created Partnership");

    }

    /**
     * Activates a partnership by name.
     * Implementation filters by partnership name so only one partnership
     *  is visible and that one is activated.
     * @param partnershipName partnership to activate
     */
    public void activatePartnership(String partnershipName) {
        LOGGER.info("Activating partnership " + partnershipName);
        PageFactory.initElements(driver, this);
        searchResetButton.click();
        // Clear button resets it to string <ANY>, need to delete it
        searchInput.click();
        searchInput.sendKeys(Keys.END);
        searchInput.sendKeys(Keys.BACK_SPACE);
        searchInput.sendKeys(Keys.BACK_SPACE);
        searchInput.sendKeys(Keys.BACK_SPACE);
        searchInput.sendKeys(Keys.BACK_SPACE);
        searchInput.sendKeys(Keys.BACK_SPACE);
        searchInput.sendKeys(partnershipName);
        searchButton.click();
        wait.until(ExpectedConditions.visibilityOf(actionButton));
        actionButton.click();
        activateActionButton.click();
        wait.until(ExpectedConditions.visibilityOf(confirmActivateButton));
        confirmActivateButton.click();
        wait.until(ExpectedConditions.visibilityOf(partnershipStatus));
        Assert.assertEquals("Partnership status is not active", "Active", partnershipStatus.getText());
        LOGGER.info("Partnership " + partnershipName + " successfully activated");
    }
}
