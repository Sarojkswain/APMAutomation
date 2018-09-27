package com.ca.apm.systemtest.fld.plugin.file.transformation.config.transformation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.ErrorUtils;

/**
 * Created by haiva01 on 23.6.2015.
 */
@XmlType(name = "xslt-transform",
    namespace = "com.ca.apm.systemtest.fld.plugin.file.transformation")
public class XsltTransform extends TransformationBase {
    private static final Logger log = LoggerFactory.getLogger(XsltTransform.class);

    private String xsltTransformation;

    public XsltTransform() {
    }

    public String getXslt() {
        return xsltTransformation;
    }

    @XmlElement(name = "xslt")
    public void setXslt(String xsltTransformation) {
        this.xsltTransformation = xsltTransformation;
    }


    @Override
    public String toString() {
        return "XsltTransform{"
            + "xsltTransformation='" + /*xsltTransformation*/ ".." + '\'' + '}';
    }

    @Override
    public void apply(String fileString, TransformationContext context) throws Exception {
        final File file = new File(fileString);

        final String evaluatedXslt = context.getStringEvaluator()
            .evaluateString(xsltTransformation);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory
                .newTransformer(new StreamSource(IOUtils.toInputStream(evaluatedXslt)));
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
        } catch (TransformerConfigurationException ex) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, ex,
                "Failed to parse XSL style sheet. Exception: {0}");
        }

        InputStream inputStream;
        try (InputStream fileInputStream = FileUtils.openInputStream(file)) {
            inputStream = IOUtils.toBufferedInputStream(fileInputStream);
        } catch (IOException ex) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, ex,
                "Failed to read {1}. Exception: {0}", file.getAbsolutePath());
        }

        ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
        Result streamResult = new StreamResult(resultStream);
        try {
            transformer.transform(new StreamSource(inputStream), streamResult);
        } catch (TransformerException ex) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, ex,
                "Failed to transform XML. Exception: {0}");
        }

        log.info("Transformed XML:\n{}", resultStream.toString(StandardCharsets.UTF_8.name()));

        try (OutputStream outputStream = FileUtils.openOutputStream(file)) {
            IOUtils.write(resultStream.toByteArray(), outputStream);
        } catch (IOException ex) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, ex,
                "Failed to write transformation result into {1}. Exception: {0}",
                file.getAbsolutePath());
        }
    }
}
