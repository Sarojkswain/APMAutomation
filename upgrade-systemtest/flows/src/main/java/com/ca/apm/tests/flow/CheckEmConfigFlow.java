package com.ca.apm.tests.flow;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;

/**
 * Created by jirji01 on 5/16/2017.
 */
@Flow
public class CheckEmConfigFlow extends FlowBase {

    @FlowContext
    private CheckEmConfigFlowContext context;

    @Override
    public void run() throws Exception {

        if (context.getFileName().endsWith("xml")) {
            checkXml(context.getFileName(), context.getProperties());
        } else {
            checkProperties(context.getFileName(), context.getProperties());
        }
    }

    private void checkXml(String configFile, Map<String, List<String>> data) throws Exception {

        Path path = Paths.get(configFile);
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();

        Document doc = builder.parse(Files.newInputStream(path));
        XPath xpath = XPathFactory.newInstance().newXPath();

        switch (path.getFileName().toString()) {
            case "realms.xml":
                break;
            case "users.xml":
                users(data, xpath, doc);
                break;
        }
    }

    private void users(Map<String, List<String>> data, XPath xpath, Document doc) throws Exception {
        XPathExpression expr = xpath.compile("//users/user");
        Object result = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            String name = node.getAttributes().getNamedItem("name").getNodeValue();
            String password = node.getAttributes().getNamedItem("password").getNodeValue();

            if (data.containsKey(name) && ! data.get(name).contains(password)) {
                throw new IllegalStateException("password '" + password + "' for user '" + name + "' does not match expected values '" + data.get(name) + "' in users.xml");
            }
        }
    }

    private void checkProperties(String configFile, Map<String, List<String>> properties) throws Exception {

        Properties configProperties = new Properties();
        configProperties.load(Files.newInputStream(Paths.get(configFile)));

        for (Map.Entry<String, List<String>> entry : properties.entrySet()) {

            String value = configProperties.getProperty(entry.getKey());

            if (value == null) {
                throw new IllegalStateException("Property '" + entry.getKey() + "' not found in file " + configFile);
            }

            for (String item : value.split(" ")) {
                if (!entry.getValue().contains(item)) {
                    throw new IllegalStateException("Property '" + entry.getKey() + "' -> '" + value + "' \n expected items '" +entry.getValue() + "' does not contain '" + item + "' () in file " + configFile);
                }
            }
        }
    }
}
