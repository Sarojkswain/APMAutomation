package com.ca.apm.systemtest.fld.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Created by meler02 on 10.12.2014.
 */
public final class XmlUtils {
    private static Logger log = LoggerFactory.getLogger(XmlUtils.class);

    public static Document openDocument(String xmlFile) throws ParserConfigurationException,
        SAXException, IOException {
        return openDocument(new File(xmlFile));
    }

    public static Document openDocument(File xmlFile) throws ParserConfigurationException,
        SAXException, IOException {
        log.debug("Parsing file {}", xmlFile.getAbsolutePath());
        try (InputStream fis = new BufferedInputStream(new FileInputStream(xmlFile))) {
            DocumentBuilderFactory docbf = DocumentBuilderFactory.newInstance();
            docbf.setNamespaceAware(true);
            DocumentBuilder docbuilder = docbf.newDocumentBuilder();
            return docbuilder.parse(fis);
        }
    }

    public static void saveDocument(Document document, String xmlFile) throws TransformerException,
        IOException {
        saveDocument(document, new File(xmlFile));
    }

    public static void saveDocument(Document document, File xmlFile) throws TransformerException,
        IOException {
        log.debug("Saving document to file {}", xmlFile.getAbsolutePath());
        try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(xmlFile))) {
            // Use a Transformer for output
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            //
            Source source = new DOMSource(document);
            Result result = new StreamResult(fos);
            transformer.transform(source, result);
        }
    }

}
