/*
 * Copyright (c) 2014 CA. All rights reserved.
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

package com.ca.tas.em.rest.api.test;

import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.Ribbon;
import com.ca.apm.test.atc.common.SecurityPage;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.Utils;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.envproperty.EnvironmentPropertyContextFactory;
import com.ca.tas.test.em.appmap.EmRestApiTestbed;
import com.ca.tas.test.em.appmap.EmRestApiTestbedAgc;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

public class PublicRestApiTest extends UITest {

    private EnvironmentPropertyContext envProp;
    private String hostname;
    final CloseableHttpClient httpclient = HttpClients.createDefault();

    private static final Logger LOGGER = LoggerFactory.getLogger(PublicRestApiTest.class);

    @BeforeTest
    public void loadEnvProperties() throws Exception {
        envProp = new EnvironmentPropertyContextFactory().createFromSystemProperty();
        hostname = getTestHostname();
    }

    @Tas(testBeds = {@TestBed(name = EmRestApiTestbedAgc.class, executeOn = EmRestApiTestbedAgc.MACHINE)}, owner = "surma04", size = SizeType.MEDIUM)
    @Test(groups = "publicApi")
    public void testTokenGenerationAndRestCallsOnAgc() throws Exception {
        testTokenGenerationAndRestCalls(true);
    }

    @Tas(testBeds = {@TestBed(name = EmRestApiTestbed.class, executeOn = EmRestApiTestbed.MACHINE)}, owner = "surma04", size = SizeType.MEDIUM)
    @Test(groups = "publicApi")
    public void testTokenGenerationAndRestCalls() throws Exception {
        testTokenGenerationAndRestCalls(false);
    }
        
    private void testTokenGenerationAndRestCalls(final boolean isAgc) throws Exception {
        final UI ui = this.getUI();
        ui.login();

        final String tokenHeader = getAuthnHeaderWithGeneratedToken(ui);

        // assure private API did not get broken
        testPrivateApiCalls(tokenHeader);

        // OK is expected for public api on HTTP
        final String publicVertexUrl = getPublicVertexUrl();
        final String publicGraphVertexUrl = getPublicGraphVertexUrl();

        // public api with query and a token
        try {
            testPublicApiOnSsl(tokenHeader, publicVertexUrl);
            testPublicApiOnSsl(tokenHeader,publicVertexUrl.concat("?q=attributes.location:USA"));
        } catch (AssertionError err) {
            if (!isAgc) {
                // for AGC version this endpoint HAVE TO throw the 404
                LOGGER.info("Public Vertex should not be available for AGC at " + publicVertexUrl);
                throw err;
            } else {
                LOGGER.info("Public Vertex not available for AGC as expected at " + publicVertexUrl);
            }
            
        }

        testPublicApiOnSsl(tokenHeader, publicGraphVertexUrl);
        testPublicApiOnSsl(tokenHeader,
            publicGraphVertexUrl.concat("?q=attributes.location:USA"));

        final String publicGraphUrl = getPublicGraphUrl();
        testPublicApiOnSsl(tokenHeader, publicGraphUrl);
        // test that json filter is accepted
        {
            // @formatter:off
            final String filter = "{\r\n" + 
                "    \"includeStartPoint\": false ,\r\n" + 
                "    \"orItems\":[{\r\n" + 
                "        \"andItems\":[\r\n" + 
                "                    {\r\n" + 
                "                        \"itemType\" : \"attributeFilter\", \r\n" + 
                "                        \"attributeName\": \"type\",\r\n" + 
                "                        \"attributeOperator\": \"IN\",\r\n" + 
                "                        \"values\": [\"GENERICFRONTEND\"]\r\n" + 
                "                    }\r\n" + 
                "                    ]\r\n" + 
                "    }]\r\n" + 
                "}   ";
            // @formatter:on
            final HttpPost request = new HttpPost(publicGraphUrl);
            request.addHeader(HttpHeaders.AUTHORIZATION, tokenHeader);
            request.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
            request.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
            request.setEntity(new StringEntity(filter, ContentType.APPLICATION_JSON));
            final HttpResponse response = httpclient.execute(request);

            assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK, "A call to " + publicGraphUrl
                + " with a valid token and json filter expects an OK response");
            request.releaseConnection();
        }

        LOGGER.info("test GUI - token revocation");
        Thread.sleep(1000);
        InvalidateAllTokens(ui);

        // REST calls with revoked token should return proper error message
        {
            final HttpGet request = new HttpGet(publicGraphVertexUrl);
            request.addHeader(HttpHeaders.AUTHORIZATION, tokenHeader);
            final HttpResponse response = httpclient.execute(request);
            
            assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_UNAUTHORIZED,
                "A call to a public secured URL with an expired token expects an 401 response");
            final String header = response.getFirstHeader(HttpHeaders.WWW_AUTHENTICATE).getValue();
            if (!header.contains("error=token_expired")) {
                Assert
                    .fail("TOKEN_EXPIRED error expected when the token header is not valid anymore");
            }
            request.releaseConnection();

        }

        logger.info("clean up");
        ui.cleanup();
    }

    private void testPrivateApiCalls(String tokenHeader) throws Exception{
        // test private API functionality - 200
        String privateUrl = getPrivateGraphUrl();
        LOGGER.debug("URL tested: {}", privateUrl);

        {
            final HttpGet request = new HttpGet(privateUrl);
            request.addHeader(HttpHeaders.AUTHORIZATION, tokenHeader);
            final HttpResponse response = httpclient.execute(request);

            assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                "A call to a private unsecured URL with a valid token expects an OK response");
            request.releaseConnection();
        }
        

        {
            // private API without a token - 401
            final HttpGet request = new HttpGet(privateUrl);
            final HttpResponse response = httpclient.execute(request);
            
            assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_UNAUTHORIZED,
                "A call to a private unsecured URL without a token expects a 401 response");
            request.releaseConnection();
        }
    }

    private String getAuthnHeaderWithGeneratedToken(final UI ui) throws Exception {

        ui.getLeftNavigationPanel().goToSecurity();

        final SecurityPage security = ui.getSecurityPage();
        security.clickOnGenerateNewToken();
        security.fillLabel("test token");

        final List<WebElement> radio = ui.getDriver().findElementsById("generate-key-public-label");
        if (radio.size() > 0) {
            security.selectPublicToken();
        }

        Utils.sleep(200);
        security.submitForm();
        security.waitForNextStep();
        assertTrue("Generating of security token failed.", security.isGeneratedTokenPresent());

        String agcToken = security.getGeneratedToken();

        security.clickOnClose();

        String tokenHeader = "Bearer " + agcToken;
        LOGGER.debug("Authorization header for testing: {}", tokenHeader);
        return tokenHeader;
    }

    private void testPublicApiOnSsl(String tokenHeader, String uri) throws Exception{

        // call with token returns OK
        {
            final HttpGet request = new HttpGet(uri);
            request.addHeader(HttpHeaders.AUTHORIZATION, tokenHeader);
            final HttpResponse response = httpclient.execute(request);

            assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                "A call to a public secured URL with a valid token expects an OK response at " + uri);
            request.releaseConnection();
        }

        // call without token returns UNAUTHORIZED
        {
            final HttpGet request = new HttpGet(uri);
            final HttpResponse response = httpclient.execute(request);
            assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_UNAUTHORIZED,
                "A call to a public secured URL without a valid token expects an 401 response at " + uri);
            String header = response.getFirstHeader(HttpHeaders.WWW_AUTHENTICATE).getValue();
            if (!header.contains("error=no_token")) {
                Assert.fail("NO_TOKEN error header expected when the token header is not present");
            }
            request.releaseConnection();
        }
    }

    /** confirm the delete of the tokens for the current user */
    private void clickConfirmationYes(final UI ui) {
        final RemoteWebDriver driver = ui.getDriver();
        final int buttonWaitTime = 5000;
        final WebDriverWait wait = new WebDriverWait(driver, buttonWaitTime);
        WebElement button =
            wait.until(ExpectedConditions.elementToBeClickable(By
                .name("confirmationDialogButtonYes")));
        LOGGER.info("clicking button {}", button.getText());
        button.click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By
            .name("confirmationDialogButtonYes")));
    }

    private void InvalidateAllTokens(final UI ui) throws Exception {
        final RemoteWebDriver driver = ui.getDriver();
        final List<WebElement> allLinks = driver.findElementsByClassName("t-invalidate-token-link");
        if (allLinks.size() < 1) {
            throw new RuntimeException("Did not find link with class == t-invalidate-token-link");
        }

        while (true) {
            Thread.sleep(1000);
            final List<WebElement> links =
                driver.findElementsByClassName("t-invalidate-token-link");
            if (links.size() < 1) {
                return;
            }
            final WebElement link = links.get(0);
            LOGGER.debug("link text: [{}]", link.getText());
            WebDriverWait wait = new WebDriverWait(driver, 10);
            wait.until(ExpectedConditions.elementToBeClickable(link));
            LOGGER.info("clicked link {}", link.getText());
            link.click();
            clickConfirmationYes(ui);
        }
    }

    /**
     * @param testHostname
     */
    private String getPrivateGraphUrl() {
        return String.format("http://%s:8081/apm/appmap/private/graph", hostname);
    }

    /**
     * @param testHostname
     */
    @Deprecated // /vertex endpoint is deprecated at the moment, /graph/vertex should replace it
    private String getPublicVertexUrl() {
        return String.format("http://%s:8081/apm/appmap/vertex", hostname);
    }

    private String getPublicGraphUrl() {
        return String.format("http://%s:8081/apm/appmap/graph", hostname);
    }
    
    private String getPublicGraphVertexUrl() {
        return String.format("http://%s:8081/apm/appmap/graph/vertex", hostname);
    }

    private String getTestHostname() throws UnknownHostException {
        return InetAddress.getByName(
            envProp.getRolePropertiesById(EmRestApiTestbed.ROLE).getProperty("em_hostname"))
            .getCanonicalHostName();
    }

    /*
     * ALM testcase id #454339
     */
    @Tas(testBeds = {@TestBed(name = EmRestApiTestbed.class, executeOn = EmRestApiTestbed.MACHINE)}, owner = "gaupr01", size = SizeType.MEDIUM)
    @Test(groups = {"publicApi","full","Securability","bugs"})
    public void RestCallsTestXSSinCreatingGroup() throws Exception {
        final UI ui = this.getUI();
        ui.login();
        final String tokenHeader = getAuthnHeaderWithGeneratedToken(ui);
        // assure private API did not get broken
        testPrivateApiCalls(tokenHeader);
        String publicUrl = String.format("http://%s:8081/apm/appmap/private/grouping", hostname);
        {
            String payload =
                "{\"name\": \"<script>alert('Group')</script>\",\"groupBy\": [{\"attributeName\": \"location\",\"prefix\": \"Loc:\"},{\"attributeName\": \"test123\",\"prefix\": \"App:\"}],\"public\": true}";

            final HttpPost request = new HttpPost(publicUrl);
            request.addHeader(HttpHeaders.AUTHORIZATION, tokenHeader);
            request.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
            request.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
            request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
            final HttpResponse response = httpclient.execute(request);

            LOGGER.info(String.format("POST [%s] to [%s], status code [%s], returned data: "
                + System.lineSeparator() + "%s", payload, publicUrl, response.getStatusLine(),
                EntityUtils.toString(response.getEntity())));
            request.releaseConnection();
        }

        // checking HTML tags are encrypted
        {
            final HttpGet request = new HttpGet(publicUrl);
            request.addHeader(HttpHeaders.AUTHORIZATION, tokenHeader);
            final HttpResponse response = httpclient.execute(request);

            String result = EntityUtils.toString(response.getEntity());
            assertTrue(result.contains("&lt;script>alert(&#39;Group&#39;)&lt;/script>"));
            request.releaseConnection();
        }
        logger.info("clean up");
        ui.cleanup();
    }
    
    
    /*
     * ALM testcase id #454340
     */
    @Tas(testBeds = {@TestBed(name = EmRestApiTestbed.class, executeOn = EmRestApiTestbed.MACHINE)}, owner = "gaupr01", size = SizeType.MEDIUM)
    @Test(groups = {"publicApi","full","Securability","bugs"})
    public void RestCallsTestXSSinGeneratingSecurityToken() throws Exception {
        final UI ui = this.getUI();
        ui.login();
        final String tokenHeader = getAuthnHeaderWithGeneratedToken(ui);
        // assure private API did not get broken
        testPrivateApiCalls(tokenHeader);
        String publicUrl = String.format("http://%s:8081/apm/appmap/private/token", hostname);
        {
            String payload =
                "{\"description\": \"<script>alert('Token')</script>\",\"expirationDate\": null,\"system\": true}";
            final HttpPost request = new HttpPost(publicUrl);
            request.addHeader(HttpHeaders.AUTHORIZATION, tokenHeader);
            request.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
            request.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
            request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
            final HttpResponse response = httpclient.execute(request);
            LOGGER.info(String.format("POST [%s] to [%s], status code [%s], returned data: "
                + System.lineSeparator() + "%s", payload, publicUrl, response.getStatusLine(),
                EntityUtils.toString(response.getEntity())));
            request.releaseConnection();
        }
        
        // checking HTML tags are encrypted
        {
            final HttpGet request = new HttpGet(publicUrl);
            request.addHeader(HttpHeaders.AUTHORIZATION, tokenHeader);
            final HttpResponse response = httpclient.execute(request);

            String result = EntityUtils.toString(response.getEntity());
            assertEquals(result.contains("&lt;script>alert(&#39;Token&#39;)&lt;/script>"), true,
                "The response is : " + result);
            request.releaseConnection();
        }
       
        logger.info("clean up");
        ui.cleanup();
    }
}
