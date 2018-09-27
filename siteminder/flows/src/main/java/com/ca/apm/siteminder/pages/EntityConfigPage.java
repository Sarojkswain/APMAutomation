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

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityConfigPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityConfigPage.class);


    private String pageUrl;
    private WebDriver driver;


    @FindBy(id = "main:fedEntitySubView2:entitiesTable:fedCreateEntitybut")
    private WebElement createEntity;

    @FindBy(id = "main:fedEntitySubView2:entitiesTable:fedImportMetabut")
    private WebElement importMetadata;

    @FindBy(id = "main:fileupload_internal")
    private WebElement fileUploadField;

    @FindBy(id = "main:EntityDataGrid:0:EntityNameInput")
    private WebElement entityNameInput;

    @FindBy(id = "main:mdImpWizidnext_bottom")
    private WebElement nextButton;

    @FindBy(id = "main:CertsDataGrid:0:AliasInput")
    private WebElement aliasNameInput;

    @FindBy(id = "main:mdImpWizidfinish_bottom")
    private WebElement finishButton;

    @FindBy(id = "main:fedEntitySubView2:entitiesTable:0:_idJsp20")
    private WebElement createdIdPEntityID;

    private WebDriverWait wait;

    public EntityConfigPage(WebDriver driver) {
        this.driver = driver;
        wait = new WebDriverWait(driver, 60);
        PageFactory.initElements(driver, this);
        LOGGER.info("Initialized Entity config page");
        pageUrl = driver.getCurrentUrl();
        LOGGER.info("Page url : " + pageUrl);

    }


    public EntityCreationWizard clickOnCreateEntity() throws InterruptedException {
        wait.until(ExpectedConditions.visibilityOf(createEntity));
        createEntity.click();
        return new EntityCreationWizard(driver);

    }

    public void importMetadata(String filePath, String entityName, String aliasName) throws InterruptedException {
        wait.until(ExpectedConditions.visibilityOf(importMetadata));
        importMetadata.click();
        Thread.sleep(3000);
        LOGGER.info("Attempting to create SP by importing metadata");
        LOGGER.info("Attempting to upload file " + filePath);
        LOGGER.info("At url " + driver.getCurrentUrl());
        PageFactory.initElements(driver, this);
        //wait.until(ExpectedConditions.visibilityOf(fileUploadField));
        fileUploadField.sendKeys(filePath);
        nextButton.click();
        PageFactory.initElements(driver, this);
        wait.until(ExpectedConditions.visibilityOf(entityNameInput));
        entityNameInput.sendKeys(entityName);
        LOGGER.info("Added entityName as " + entityName);
        nextButton.click();
        LOGGER.info("Finished step 2");
        PageFactory.initElements(driver, this);
        try {
            wait.until(ExpectedConditions.visibilityOf(aliasNameInput));
            aliasNameInput.sendKeys(aliasName);
            LOGGER.info("Set alias as " + aliasName);
        }
        catch (TimeoutException e) {
            // Certificate may be already imported, continue
        }
        nextButton.click();
        PageFactory.initElements(driver, this);
        wait.until(ExpectedConditions.visibilityOf(finishButton));
        finishButton.click();
        LOGGER.info("Successfully created SP entity");
    }

    public void verifyCreatedIdP(String idpEntityID) {
        wait.until(ExpectedConditions.visibilityOf(createdIdPEntityID));
        Assert.assertTrue("Error creating entity ID",createdIdPEntityID.getText().equals(idpEntityID));
        LOGGER.info("IdP entity with id " + idpEntityID + " created successfully.");
    }
}
