package com.ca.apm.tests.utility;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;

public class XMLModification {

    public void modifyRecordingXMLFile(String filepath, String RecordingID, String NodeName) {
        // System.out.println("in xml modification mthod");
        File file = new File(filepath);
        // System.out.println("file path :" + filepath);
        DocumentBuilderFactory d = DocumentBuilderFactory.newInstance();
        DocumentBuilder d1;
        Document doc;
        try {
            d1 = d.newDocumentBuilder();
            doc = d1.parse(file);

            // Delete child
            Element deletechild = (Element) doc.getElementsByTagName(NodeName).item(0);
            deletechild.getParentNode().removeChild(deletechild);
            // create child element
            Element childElement1 = doc.createElement(NodeName);
            Text childElement2 = doc.createTextNode(RecordingID);
            childElement1.appendChild(childElement2);
            doc.getElementsByTagName("RecordingSession").item(0).appendChild(childElement1);
            // doc.getElementsByTagName("RecordingSession").item(0).insertBefore(childElement1,doc.getElementsByTagName("ComponentList").item(0));

            // set up a transformer
            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = transfac.newTransformer();

            StreamResult result = new StreamResult(file);
            DOMSource source = new DOMSource(doc);
            trans.transform(source, result);


        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }


}
