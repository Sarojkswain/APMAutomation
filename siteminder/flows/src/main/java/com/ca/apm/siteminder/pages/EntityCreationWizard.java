package com.ca.apm.siteminder.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityCreationWizard {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityCreationWizard.class);

    private WebDriver driver;
    private WebDriverWait wait;
    private String pageUrl;

    @FindBy(id = "main:id1next_bottom")
    private WebElement nextButton;

    @FindBy(id = "main:entitytxt1")
    private WebElement entityIDField;

    @FindBy(id = "main:entitytxt2")
    private WebElement entityName;

    @FindBy(id = "main:entitytxt4")
    private WebElement entityBaseURL;

    @FindBy(id = "main:nameIDTransientCB")
    private WebElement transientCheckbox;

    @FindBy(id = "main:id1finish_bottom")
    private WebElement finishButton;

    @FindBy(id = "main:attributetable:addAttributeBut")
    private WebElement addAttributeButton;

    @FindBy(id = "main:attributetable:0:AttributeTable_NameInput")
    private WebElement name0;

    @FindBy(id = "main:attributetable:1:AttributeTable_NameInput")
    private WebElement name1;

    @FindBy(id = "main:attributetable:0:AttributeTable_FormatDropdown")
    private WebElement dropDown1;

    @FindBy(id = "main:attributetable:1:AttributeTable_FormatDropdown")
    private WebElement dropDown2;

    public EntityCreationWizard(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
        pageUrl = driver.getCurrentUrl();
        wait = new WebDriverWait(driver, 20);
    }

    public EntityConfigPage createEntity(String entityID, String entityNameS, String baseURL, String attName1, String attName2) throws InterruptedException {
        nextButton.click();
        PageFactory.initElements(driver, this);
        wait.until(ExpectedConditions.visibilityOf(entityIDField));
        entityIDField.sendKeys(entityID);
        LOGGER.info("Set entity ID as " + entityID);
        entityName.sendKeys(entityNameS);
        LOGGER.info("Set entity name as " + entityNameS);
        entityBaseURL.sendKeys(baseURL);
        LOGGER.info("Set base URL as " + baseURL);
        transientCheckbox.click();
        addAttributeButton.click();
        wait.until(ExpectedConditions.visibilityOf(addAttributeButton));
        addAttributeButton.click();
        PageFactory.initElements(driver, this);
        wait.until(ExpectedConditions.visibilityOf(name0));
        name0.sendKeys(attName1);
        name1.sendKeys(attName2);
        dropDown1.sendKeys("b");
        dropDown2.sendKeys("b");
        nextButton.click();
        LOGGER.info("Finished step 2");
        PageFactory.initElements(driver, this);
        wait.until(ExpectedConditions.visibilityOf(finishButton));
        finishButton.click();
        LOGGER.info("Finished step 3");
        return new EntityConfigPage(driver);
    }
}
