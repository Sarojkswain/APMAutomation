package com.ca.apm.transactiontrace.appmap.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

/**
 *
 * link to Private API that fetches the graph in JSON format
 *
 * @author Sundeep (bhusu01)
 */
public class PrivateAPIGraphJson {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrivateAPIGraphJson.class);

    private static final String GRAPH_PATH = "/apm/appmap/private/graph";

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String pageUrl;

    public PrivateAPIGraphJson(WebDriver webDriver) {
        driver = webDriver;
        wait = new WebDriverWait(driver, 60);
        PageFactory.initElements(driver, this);
        pageUrl = driver.getCurrentUrl();
        LOGGER.info(pageUrl);
    }

    public String getJSonGraph(String webviewBaseURL) {
        driver.get(webviewBaseURL + GRAPH_PATH);
        String htmlSource = driver.getPageSource();

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        String jsonGraph = "";
        try {
            builder = builderFactory.newDocumentBuilder();
            InputSource inputSource = new InputSource(new StringReader(htmlSource));
            Document document = builder.parse(inputSource);
            Element rootElement = document.getDocumentElement();
            LOGGER.info("Root element is " + rootElement.getTagName());
            LOGGER.info("Last child is " + rootElement.getLastChild());
            jsonGraph = rootElement.getLastChild().getFirstChild().getTextContent();
        } catch (ParserConfigurationException e) {
            LOGGER.error("Trouble creating parser. JSON can't be extracted");
            LOGGER.debug("Trouble creating parser. JSON can't be extracted", e);
        } catch (SAXException e) {
            LOGGER.error("Error parsing returned output");
            LOGGER.debug("Error parsing returned output", e);
        } catch (IOException e) {
            LOGGER.error("Error reading the input stream");
            LOGGER.debug("Error reading the input stream", e);
        }

        return jsonGraph;
    }
}
