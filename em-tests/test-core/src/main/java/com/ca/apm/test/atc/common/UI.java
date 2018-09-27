/*
 * Copyright (c) 2015 CA. All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */
package com.ca.apm.test.atc.common;

import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.test.atc.common.ModalDialog.DialogButton;
import com.ca.apm.test.atc.common.element.ElementConditionWrapper;
import com.ca.apm.test.atc.common.element.PageElement;
import com.ca.apm.test.atc.common.element.WebElementWrapper;
import com.ca.apm.test.atc.common.landing.Breadcrumb;
import com.ca.apm.test.atc.common.landing.LandingPage;
import com.ca.apm.test.atc.common.landing.Notebook;

import static com.ca.apm.test.atc.common.Utils.clearAndSetInputField;
import static com.ca.apm.test.atc.common.element.WebElementWrapper.wrapElement;

public class UI {

    public enum View {

        NOTEBOOK("Notebook", By.cssSelector(".home-container.home-detail"), "/home/detail"), 
        HOMEPAGE("Homepage", By.cssSelector(".home-container.t-tile-view"), "/home"), 
        MAPVIEW("Mapview", By.className("map-map-view"), "/map"), 
        DASHBOARD("Dashboard", By.className("trend-card-panel"), "/dashboard"),
        UNIVERSES("Universes", By.className("universe-grid-container"), "/universes");

        private final String viewMenuLabel;
        private final By characteristicSelector;
        private final String characteristicUriPart;

        View(String viewMenuLabel, By characteristicSelector, String characteristicUriPart) {
            this.viewMenuLabel = viewMenuLabel;
            this.characteristicSelector = characteristicSelector;
            this.characteristicUriPart = characteristicUriPart;
        }

        public String getViewMenuLabel() {
            return viewMenuLabel;
        }

        public By getCharacteristicSelector() {
            return characteristicSelector;
        }

        public String getCharacteristicUriPart() {
            return characteristicUriPart;
        }

        public static View of(String value) {
            for (View v : View.values()) {
                if (v.getViewMenuLabel().equals(value)) {
                    return v;
                }
            }

            return null;
        }
    }

    public enum Role {
        ADMIN("admin", ""), GUEST("guest", "Guest"), READER("reader", ""), EDITOR("editor", ""), 
        MANAGER("manager", ""), TOGGLER("toggler", ""), NEVERLOGIN("neverlogin", "wrongpass"), 
        GHOST("ghost", "");

        private String user;
        private String password;

        Role(String user, String password) {
            this.user = user;
            this.password = password;
        }

        public String getUser() {
            return user;
        }

        public String getPassword() {
            return password;
        }
    }

    public enum Group {
        readers, editors, managers, togglers
    }

    public enum Permission {
        read, edit, manage
    }

    private static final Logger logger = LoggerFactory.getLogger(UI.class);

    private static final String APP_GLOBAL_STATE = "AppGlobalState";

    private static final long LOGIN_TIMEOUT = 20;

    public static long IMPLICITLY_WAIT = 3;

    private RemoteWebDriver driver;

    private Ribbon ribbon = null;

    private FilterBy filterBy = null;

    private PerspectiveSettings perspectiveSettings = null;

    private PerspectivesControl perspectivesControl = null;

    private Canvas canvas = null;

    private TrendCards trendCards = null;

    private DetailView detailView = null;

    private ModalDialog modalDialog = null;

    private DetailsPanel detailsPanel = null;

    private DataBrushing dataBrushing = null;

    private Timeline timeline = null;

    private AttributeRulesTable attributeRules = null;

    private Search search = null;

    private Zoom zoom = null;

    private WebView webView = null;

    private String startUrl = null;

    private EnterprisePage enterprisePage = null;

    private SecurityPage securityPage = null;

    private FollowersPage followersPage = null;

    private MultiStepDialog multiStepDialog = null;

    private ProgressDialog progressDialog = null;

    private UniverseSettings universeSettings = null;

    private BottomBar bottomBar = null;

    private LandingPage landingPage = null;

    private MapviewPage mapTab = null;

    private AlertSettingsPage alertSettingsPage = null;

    private DashboardPage cardTab = null;

    private Notebook notebook = null;

    private Breadcrumb breadcrumb = null;

    private TopNavigationPanel topNavigationPanel = null;

    private LeftNavigationPanel leftNavigationPanel = null;

    private boolean disableWelcome = true;

    private View expectedView;

    public UI() {}

    public UI(RemoteWebDriver driver, String url) {
        this.driver = driver;
        this.startUrl = url;
    }

    public UI(RemoteWebDriver driver, String url, View expectedView) {
        this.driver = driver;
        this.startUrl = url;
        this.expectedView = expectedView;
    }

    public void login() throws Exception {
        login(Role.ADMIN);
    }

    public void login(View expectedView) throws Exception {
        login(Role.ADMIN, expectedView);
    }

    /**
     * 
     * @param role
     * @throws Exception
     */
    public void login(Role role) throws Exception {
        login(role.getUser(), role.getPassword());
    }

    public void login(Role role, View expectedView) throws Exception {
        login(role.getUser(), role.getPassword(), startUrl, expectedView);
    }

    /**
     * 
     * @param role
     * @param url
     * @throws Exception
     */
    public void login(Role role, String url, View expectedView) throws Exception {
        login(role.getUser(), role.getPassword(), url, expectedView);
    }

    /**
     * 
     * @param user
     * @param pass
     * @throws Exception
     */
    public void login(String user, String pass) throws Exception {
        login(user, pass, startUrl, expectedView);
    }

    /**
     * 
     * @param user
     * @param pass
     * @param url
     * @throws Exception
     */
    public void login(String user, String pass, String url, View expectedView) throws Exception {
        if (url != null) {
            driver.get(url);
        } else {
            driver.get(getStartUrl());
        }

        doLogin(user, pass);

        if (expectedView != null) {
            waitUntilVisible(expectedView.getCharacteristicSelector(), LOGIN_TIMEOUT);
        } else {
            View currentView = getCurrentViewByUrl();
            if (currentView != null) {
                waitUntilVisible(currentView.getCharacteristicSelector(), LOGIN_TIMEOUT);
            }
        }
    }

    private void doLogin() throws Exception {
        doLogin(Role.ADMIN.getUser(), Role.ADMIN.getPassword());
    }

    private void doLogin(String user, String pass) throws Exception {
        disableWelcome();

        if (user == null) {
            user = "Admin";
        }

        if (driver.findElements(By.id("LoginFrame")).size() > 0) {
            // we are on login page
            driver.switchTo().frame("LoginFrame");

            WebElement username = driver.findElement(By.id("username"));
            WebElement password = driver.findElement(By.id("j_passWord"));

            clearAndSetInputField(getDriver(), username, user);
            clearAndSetInputField(getDriver(), password, pass);

            username.submit();

            driver.switchTo().defaultContent();
        } else {
            // If there was no login screen, we got here having been launched from grunt probably
            try {
                doWait(10).until(new ExpectedCondition<Boolean>() {
                    @Override
                    public Boolean apply(WebDriver d) {
                        return d.findElements(By.id("welcome-dialog")).size() > 0;
                    }
                });

                ModalDialog md = getModalDialog();
                if (md != null) {
                    md.clickButton(DialogButton.CLOSE);
                }
            } catch (TimeoutException e) {
                logger.warn("Timeout when waited for the Welcome dialog.");
            }
        }
    }

    /**
     * Log out
     */
    public void logout() {
        getTopNavigationPanel().getLogoutLinkElement().click();
        getDriver().switchTo().window(getDriver().getWindowHandle());
        waitUntilVisible(By.id("LoginFrame"));
    }

    public RemoteWebDriver getDriver() {
        return driver;
    }

    public Ribbon getRibbon() {
        if (ribbon == null) {
            ribbon = new Ribbon(getLeftNavigationPanel(), this);
        }
        return ribbon;
    }

    public FilterBy getFilterBy() throws Exception {
        if (filterBy == null) {
            filterBy = new FilterBy(this);
        }
        return filterBy;
    }

    public EnterprisePage getEnterprisePage() {
        if (enterprisePage == null) {
            enterprisePage = new EnterprisePage(this);
        }
        return enterprisePage;
    }

    public SecurityPage getSecurityPage() {
        if (securityPage == null) {
            securityPage = new SecurityPage(this);
        }
        return securityPage;
    }

    public MapviewPage getMapviewPage() {
        if (mapTab == null) {
            mapTab = new MapviewPage(this);
        }
        return mapTab;
    }

    public DashboardPage getDashboardPage() {
        if (cardTab == null) {
            cardTab = new DashboardPage(this);
        }
        return cardTab;
    }

    public AlertSettingsPage getAlertsSettingsPage() {
        if (alertSettingsPage == null) {
            alertSettingsPage = new AlertSettingsPage(this);
        }
        return alertSettingsPage;
    }

    public View getCurrentViewByUrl() {
        final String url = getCurrentUrl();
        for (View v : View.values()) {
            if (url.contains(v.getCharacteristicUriPart())) {
                return v;
            }
        }

        return null;
    }

    /**
     * Create instance of UI based on local driver
     * 
     * @param dc
     * @param localDriverPath
     *        to local driver
     * @return local instance of UI
     */
    public static UI getLocal(DesiredCapabilities dc, String localDriverPath) {

        UI ui = new UI();

        logger.info("creating local web driver connection for browser {}", dc.getBrowserName());
        if (dc.getBrowserName().equals(DesiredCapabilities.chrome().getBrowserName())) {
            System.setProperty("webdriver.chrome.driver", localDriverPath);
            ui.setDriver(new ChromeDriver(dc));
        } else {
            throw new IllegalArgumentException("Invalid browser specified");
        }

        initUI(ui);
        return ui;
    }

    /**
     * 
     * @param ui
     */
    private static void initUI(UI ui) {
        ui.getDriver().manage().window().maximize();
        ui.getDriver().manage().timeouts().implicitlyWait(IMPLICITLY_WAIT, TimeUnit.SECONDS);
    }

    public void cleanup() {
        clearLocalStorage();
    }

    /**
     * Delete localStorage object
     */
    public void clearLocalStorage() {
        try {
            JavascriptExecutor jse = getDriver();
            jse.executeScript("localStorage.clear();");
        } catch (WebDriverException we) {
            logger.warn(we.getMessage());
        }
    }

    /**
     * Get app global state
     */
    public String getSavedState(String username) {
        JavascriptExecutor jse = getDriver();
        return (String) jse.executeScript("return localStorage.getItem('" + APP_GLOBAL_STATE
            + "_" + username + "');");
    }

    /**
     * Create instance of UI based on remote driver
     * 
     * @param dc
     * @param remoteDriverUrl
     *        to remote driver
     * @return remote instance of UI
     * @throws Exception
     */
    public static UI getRemote(DesiredCapabilities dc, String remoteDriverUrl) throws Exception {
        UI ui = new UI();
        logger.info("creating remote web driver connection to {} for browser {}",
            remoteDriverUrl, dc.getBrowserName());
        ui.setDriver(new RemoteWebDriver(new URL(remoteDriverUrl), dc));
        initUI(ui);
        return ui;
    }

    /**
     * Setter
     * 
     * @param driver
     */
    public void setDriver(RemoteWebDriver driver) {
        this.driver = driver;
    }

    /**
     * returns instance of DriverWait
     * 
     * @param sec
     * @return
     */
    public FluentWait<WebDriver> doWait(int sec) {
        return new WebDriverWait(driver, sec);
    }

    public ModalDialog getModalDialog() {
        if (modalDialog == null) {
            modalDialog = new ModalDialog(this);
        }
        return modalDialog;
    }

    /**
     * 
     * @return
     */
    public PerspectiveSettings getPerspectiveSettings() {
        if (perspectiveSettings == null) {
            perspectiveSettings = new PerspectiveSettings(this);
        }
        return perspectiveSettings;
    }

    public PerspectivesControl getPerspectivesControl() {
        if (perspectivesControl == null) {
            perspectivesControl = new PerspectivesControl(this);
        }
        return perspectivesControl;
    }

    /**
     * 
     * @return
     */
    public Canvas getCanvas() {
        if (canvas == null) {
            canvas = new Canvas(this);
        }
        return canvas;
    }

    public TrendCards getTrendCards() {
        if (trendCards == null) {
            trendCards = new TrendCards(this);
        }
        return trendCards;
    }

    public DetailView getDetailView() {
        if (detailView == null) {
            detailView = new DetailView(this);
        }
        return detailView;
    }

    public Timeline getTimeline() {
        if (timeline == null) {
            timeline = new Timeline(this);
        }
        return timeline;
    }

    /**
     * Return AttributeRulesTable object
     * 
     * @return
     */
    public AttributeRulesTable getAttributeRulesTable() {
        if (attributeRules == null) {
            attributeRules = new AttributeRulesTable(this);
        }
        return attributeRules;
    }

    public Zoom getZoom() {
        if (zoom == null) {
            zoom = new Zoom(driver);
        }
        return zoom;
    }

    public DetailsPanel getDetailsPanel() {
        if (detailsPanel == null) {
            detailsPanel = new DetailsPanel(this);
        }
        return detailsPanel;
    }

    public WebView getWebView() {
        if (webView == null) {
            webView = new WebView(this);
        }
        return webView;
    }

    public MultiStepDialog getMultiStepDialog() {
        if (multiStepDialog == null) {
            multiStepDialog = new MultiStepDialog(this);
        }
        return multiStepDialog;
    }

    public LandingPage getLandingPage() {
        if (landingPage == null) {
            landingPage = new LandingPage(this);
        }
        return landingPage;
    }

    public Notebook getNotebook() {
        if (notebook == null) {
            notebook = new Notebook(this);
        }
        return notebook;
    }

    public Breadcrumb getBreadcrumb() {
        if (breadcrumb == null) {
            breadcrumb = new Breadcrumb(this);
        }
        return breadcrumb;
    }

    public TopNavigationPanel getTopNavigationPanel() {
        if (topNavigationPanel == null) {
            topNavigationPanel = new TopNavigationPanel(this);
        }
        return topNavigationPanel;
    }

    public LeftNavigationPanel getLeftNavigationPanel() {
        if (leftNavigationPanel == null) {
            leftNavigationPanel = new LeftNavigationPanel(this);
        }
        return leftNavigationPanel;
    }

    /**
     * 
     * @return
     */
    public String getStartUrl() {
        return startUrl;
    }

    /**
     * 
     * @param startUrl
     */
    public void setStartUrl(String startUrl) {
        this.startUrl = startUrl;
    }

    /**
     * Turn off the Live mode
     * 
     * @deprecated Use method from timeline object
     * @throws Exception
     */
    public void turnOffLiveMode() throws Exception {
        getTimeline().turnOffLiveMode();
    }

    /**
     * Turn on the Live mode
     * 
     * @deprecated Use method from timeline object
     * @throws Exception
     */
    public void turnOnLiveMode() throws Exception {
        getTimeline().turnOnLiveMode();
    }

    public void waitForAutorefresh() throws Exception {
        getTimeline().expand();

        getElementProxy(By.id("end-time-indication")).waitForTextChange(40);
        Utils.sleep(500);

        getTimeline().collapse();
    }

    /*
     * Returns databrushing
     */
    public DataBrushing getDataBrushing() {
        if (dataBrushing == null) {
            dataBrushing = new DataBrushing(this);
        }

        return dataBrushing;
    }

    /**
     * 
     * @return
     */
    public Search getSearch() {
        if (search == null) {
            search = new Search(this);
        }

        return search;
    }

    public BottomBar getBottomBar() {
        if (bottomBar == null) {
            bottomBar = new BottomBar(this);
        }

        return bottomBar;
    }

    public FollowersPage getFollowersPage() {
        if (followersPage == null) {
            followersPage = new FollowersPage(this);
        }

        return followersPage;
    }

    public ProgressDialog getProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }

        return progressDialog;
    }

    /**
     * @return
     */
    public String getCurrentUrl() {
        return getDriver().getCurrentUrl();
    }

    /**
     * Open new tab with URL and return window handle
     */
    public String openUrlInANewTab(String url) {
        logger.info("Should open a new tab and switch to it.");

        getDriver().executeScript("window.open('about:blank','_blank');");
        
        String newTabHandle = Utils.switchToAnotherTab(driver, 2);
        if (url != null) {
            driver.navigate().to(url);
        }
        return newTabHandle;
    }

    public String closeCurrentTab() {
        return Utils.closeTab(getDriver(), getDriver().getWindowHandle());
    }

    /**
     * Switch tab to ATC. Tab has to be already opened.
     * 
     * Method expects that in browser are opened two tabs and current tab is WebView.
     */
    public void switchToATC() {
        logger.info("Should switch to ATC");
        Utils.switchToAnotherTab(driver, 2);
    }

    /**
     * Switch tab to ATC. Tab has to be already opened. And close WebView tab.
     * 
     * Method expects that in browser are opened two tabs and current tab is ATC and second tab is
     * WebView.
     */
    public void switchToATCandCloseWebViewTab() {
        logger.info("Should switch to ATC and close WebView tab.");

        String webViewHandle = getDriver().getWindowHandle();
        getDriver().switchTo().window(webViewHandle).close();

        Utils.switchToAnotherTab(driver, 1);
    }

    /**
     * Switch tab to WebView. Tab has to be already opened.
     * 
     * Method expects that in browser are opened two tabs and current tab is ATC and second tab is
     * WebView.
     * 
     * @throws Exception
     */
    public void switchToWebView() throws Exception {
        logger.info("Should switch to webview.");
        Utils.switchToAnotherTab(driver, 2);
        doLogin();
        getDriver().switchTo().defaultContent();
    }

    public UniverseSettings getUniverseSettings() {
        if (universeSettings == null) {
            universeSettings = new UniverseSettings(this);
        }
        return universeSettings;
    }

    /**
     * Disable of showing welcome screen
     */
    public void disableWelcome() {
        if (isDisableWelcome()) {
            getDriver().executeScript(
                "window.localStorage.setItem('welcomeObj', JSON.stringify({ skip: true }));");
        }
    }

    public boolean isDisableWelcome() {
        return disableWelcome;
    }

    public void setDisableWelcome(boolean disableWelcome) {
        this.disableWelcome = disableWelcome;
    }

    /**
     * Returns a proxy object that waits for element's visibility every time you try to access it
     * 
     * @param by
     * @return
     */
    public PageElement getElementProxy(By by) {
        return new ElementConditionWrapper(this, ExpectedConditions.visibilityOfElementLocated(by));
    }

    /**
     * Returns a proxy object that waits for element's visibility every time you try to access it
     * 
     * @param by
     * @param waitDuration
     * @return PageElement
     */
    public PageElement getElementProxy(By by, int waitDuration) {
        return new ElementConditionWrapper(this, ExpectedConditions.visibilityOfElementLocated(by),
            waitDuration);
    }

    /**
     * Returns a proxy object that waits for element's presence every time you try to access it
     * 
     * @param by
     * @return
     */
    public PageElement getInvisibleElementProxy(By by) {
        return new ElementConditionWrapper(this, ExpectedConditions.presenceOfElementLocated(by));
    }

    /**
     * Returns a proxy object that waits for element's presence every time you try to access it
     * 
     * @param by
     * @param waitDuration
     * @return PageElement
     */
    public PageElement getInvisibleElementProxy(By by, int waitDuration) {
        return new ElementConditionWrapper(this, ExpectedConditions.presenceOfElementLocated(by),
            waitDuration);
    }

    public List<PageElement> findElements(By by) {
        return WebElementWrapper.wrapElements(getDriver().findElements(by), this);
    }

    public PageElement findElement(By by) {
        return wrapElement(getDriver().findElement(by), this);
    }

    public boolean elementExists(By by) {
        return !getDriver().findElements(by).isEmpty();
    }

    public List<PageElement> waitUntilElementsVisible(By by, int duration) {
        return WebElementWrapper.wrapElements(
            Utils.waitUntilElementsVisible(getDriver(), by, duration), this);
    }

    public List<PageElement> waitUntilElementsVisible(By by) {
        return waitUntilElementsVisible(by, Utils.DEFAULT_WAIT_DURATION);
    }

    public void waitForWorkIndicator(By locator) {
        if (locator == null) {
            return;
        }

        try {
            // Wait 1 sec whether the work indicator pops up
            // (it appears with a delay, not immediately)
            waitUntilVisible(locator, 1);
        } catch (TimeoutException e) {
            // logger.info("Waiting for work indicator timed out.");
        }

        // Wait while the work indicator is displayed
        waitWhileVisible(locator);
    }

    public void waitForWorkIndicator() {
        waitForWorkIndicator(By.className("work-indicator"));
    }

    public void pageRefresh() {
        driver.navigate().refresh();
    }

    public void waitWhileVisible(By locator) {
        Utils.waitWhileVisible(getDriver(), locator);
    }

    public void waitWhileVisible(By locator, long duration) {
        Utils.waitWhileVisible(getDriver(), locator, duration);
    }

    public PageElement waitUntilVisible(By locator) {
        return wrapElement(Utils.waitUntilVisible(getDriver(), locator), this);
    }

    public PageElement waitUntilVisible(By locator, long duration) {
        return wrapElement(Utils.waitUntilVisible(getDriver(), locator, duration), this);
    }

    public <V> V waitForCondition(ExpectedCondition<V> condition, long seconds) {
        FluentWait<WebDriver> wait = Utils.getFluentWait(getDriver(), seconds);
        return wait.until(condition);
    }


    public String getViewName() {
        Pattern pattern = Pattern.compile("/#/(.*?)\\?");
        Matcher matcher = pattern.matcher(getCurrentUrl());

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    public Actions actions() {
        return new Actions(getDriver());
    }
}
