/**
 * 
 */
package com.ca.apm.systemtest.fld.flow.mm;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.utils.archive.TasArchiveFactory;
import com.ca.apm.automation.utils.file.FileDeleteData;
import com.ca.apm.automation.utils.file.FileDeleter;

/**
 * @author keyja01
 *
 */
@Flow
public class UpdateFLDMMFlow implements IAutomationFlow {
    private File mmXmlFile;
    private TasArchiveFactory archiveFactory;
    private Document doc;
    private XPath xpath;

    @FlowContext
    private UpdateFLDMMFlowContext ctx;
    
    /* (non-Javadoc)
     * @see com.ca.apm.automation.action.core.IAutomationFlow#run()
     */
    @Override
    public void run() throws Exception {
        Path tmpDir = null;
        try {
            tmpDir = Files.createTempDirectory("fld");
            File tmpDirFile = tmpDir.toFile();
            archiveFactory = new TasArchiveFactory();
            File mmJarFile = new File(ctx.mmJarFile);
            archiveFactory.createArchive(mmJarFile).unpack(tmpDirFile);
            mmXmlFile = new File(tmpDirFile, "ManagementModule.xml");
            System.out.println("mmXmlFile exists: " + mmXmlFile.exists());
            
            parseXml();
            
            xpath = XPathFactory.newInstance().newXPath();
            
            updateEmails(ctx.emailAddresses);
            
            updateMetricGroupings();
            
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource src = new DOMSource(doc);
            StreamResult tgt = new StreamResult(mmXmlFile);
            t.transform(src, tgt);
            
            archiveFactory.createArchive(mmJarFile).packDirectory(tmpDirFile);
        } catch (Exception e) {
            
        } finally {
            FileDeleter fd = new FileDeleter();
            FileDeleteData fdd = FileDeleteData.withoutFilter(tmpDir.toString());
            fd.perform(fdd);
        }
    }
    
    private void updateMetricGroupings() throws Exception {
        HashMap<String, String> map = new HashMap<>();
        map.put("FLDWLS01_1 ART", "fldwls01\\|WebLogic\\|WebLogic_Wurlitzer1");
        map.put("Fldwls01_2 ART", "fldwls01\\|WebLogic\\|WebLogic_Wurlitzer2");
        map.put("fldwls02_1 ART", "fldwls02\\|WebLogic\\|WebLogic_Wurlitzer1");
        map.put("fldwls02_2 ART", "fldwls02\\|WebLogic\\|WebLogic_Wurlitzer2");
        map.put("Tomcat9080 ART", "fldtomcat01\\|TomcatAgent_9080\\|TomcatAgent_9080");
        map.put("Tomcat9081 ART", "fldtomcat01\\|TomcatAgent_9081\\|TomcatAgent_9081");
        
        XPathExpression xp = xpath.compile("//MetricGrouping");
        NodeList nl = (NodeList) xp.evaluate(doc, XPathConstants.NODESET);
        for (int i = 0; i < nl.getLength(); i++) {
            updateMG(nl.item(i), map);
        }
    }

    private void updateMG(Node n, HashMap<String, String> map) throws Exception {
        XPathExpression xp = xpath.compile("./Name");
        String v = xp.evaluate(n);
        System.out.println(v);
        String newVal = map.get(v);
        if (newVal != null) {
            xp = xpath.compile("./FullMetricSpecifier/FullMetricSpecifierEntry/ProcessSpecifier/ProcessSpecifierRegExp");
            Node regexNode = (Node) xp.evaluate(n, XPathConstants.NODE);
            regexNode.setTextContent(newVal);
        }
    }

    private void updateEmails(String emailAddr) throws Exception {
        XPathExpression xp = xpath.compile("//SMTPRecipient");
        NodeList nl = (NodeList) xp.evaluate(doc, XPathConstants.NODESET);
        System.out.println(nl);
        for (int i = 0; i < nl.getLength(); i++) {
            Node item = nl.item(i);
            System.out.println("previos value: " + item.getTextContent());
            item.setTextContent(emailAddr);
            System.out.println(item.getTextContent());
        }
    }

    private void parseXml() throws Exception {
        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = fact.newDocumentBuilder();
        doc = builder.parse(mmXmlFile);
    }

}
