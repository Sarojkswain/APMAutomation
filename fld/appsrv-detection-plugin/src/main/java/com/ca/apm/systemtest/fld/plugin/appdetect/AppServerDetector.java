package com.ca.apm.systemtest.fld.plugin.appdetect;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.ca.apm.systemtest.fld.plugin.AppServerPlugin;
import com.ca.apm.systemtest.fld.plugin.vo.KeyValuePair;

public abstract class AppServerDetector {
    private static final Logger log = LoggerFactory.getLogger(AppServerDetector.class);
    
    public static final String VERSION         = "version";
    public static final String BASE_DIR        = "baseDir";
    public static final String NAME            = "name";
    public static final String SERVER_NAME     = "server_name";
    public static final String PORT            = "httpPort";

    protected List<Path>       installations   = new ArrayList<>();
    protected List<Map<String, Object>> foundProperties = new ArrayList<>();

    public abstract void testFile(String fileString);

    protected abstract AppServerPlugin getAppServerPlugin();

    protected abstract void scanInstallation(Path installation);

    protected String parseConfig(Path config, String... expressions) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(Files.newInputStream(config));

            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr;
            for (String expression : expressions) {
                expr = xpath.compile(expression);
                String result = expr.evaluate(doc);
                if (result != null && !result.isEmpty()) {
                    return result;
                }
            }
        } catch (Exception e) {
            log.error("There is problem when parsing XML file", e);
        }
        return null;
    }

    public void runDetection() {
        foundProperties.clear();
        
        for (Path installation : installations) {
            scanInstallation(installation);
        }
    }

    public void saveResults() {
        for (Map<String, Object> prop : foundProperties) {
            try {
                String id = (String) prop.get(NAME);
                if (id == null) {
                    continue;
                }
                
                String sp = (String) prop.get(PORT);
                if (sp == null || sp.isEmpty()) {
                    continue;
                }
                int port = Integer.parseInt(sp);

                ArrayList<KeyValuePair> kvpList = new ArrayList<KeyValuePair>();
                
                String baseDir = (String) prop.get(BASE_DIR);
                if (baseDir != null && !baseDir.isEmpty()) {
                    kvpList.add(new KeyValuePair(BASE_DIR, baseDir));
                }
                
                Object version = prop.get(VERSION);
                if (version != null && !version.toString().isEmpty()) {
                    kvpList.add(new KeyValuePair(VERSION, version));
                }

                getAppServerPlugin()
                        .configureServerInstance(id,
                                                 port,
                                                 (KeyValuePair[]) kvpList
                                                         .toArray(new KeyValuePair[kvpList.size()]));
            } catch (NumberFormatException e) {
                ;
            }
        }
    }
}
