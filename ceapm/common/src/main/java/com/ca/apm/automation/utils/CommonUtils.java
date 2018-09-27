/*
 * Copyright (c) 2016 CA. All rights reserved.
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

package com.ca.apm.automation.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import org.apache.commons.lang.WordUtils;
import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class CommonUtils {
    private static final Logger logger = LoggerFactory.getLogger(CommonUtils.class);

    /**
     * Identifies whether a text fragment is part of a stream.
     * The stream is checked line by line.
     *
     * @param stream Stream to check.
     * @param needle Text to look for.
     * @return true if the text is found, false otherwise.
     * @throws IOException If reading the stream fails.
     */
    public static boolean streamContains(InputStream stream, String needle) throws IOException {
        Args.notNull(stream, "stream");
        Args.notNull(needle, "needle");

        return streamContains(stream, Pattern.compile(Pattern.quote(needle)));
    }

    /**
     * Identifies whether a pattern is part of a stream.
     * The stream is checked line by line.
     *
     * @param stream Stream to check.
     * @param pattern Pattern to look for.
     * @return true if the pattern is found, false otherwise.
     * @throws IOException If reading the stream fails.
     */
    public static boolean streamContains(InputStream stream, Pattern pattern) throws IOException {
        boolean contains = false;
        try (BufferedReader output = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while (!contains && (line = output.readLine()) != null) {
                contains = pattern.matcher(line).find();
            }
        }
        return contains;
    }

    /**
     * Convert constant case (e.g. MY_CONSTANT) to camel case (e.g. MyConstant). Removes
     * underscores, even between numbers.
     *
     * @param constantCase input in constant case
     * @return camel cased input
     */
    public static String constantToCamelCase(String constantCase) {
        return WordUtils.capitalizeFully(constantCase, new char[] {'_'}).replaceAll("_", "");
    }

    /**
     * Saves the contents of an XML document to a file.
     *
     * @param doc XML document.
     * @param path File path.
     */
    public static  void saveDocumentToFile(Document doc, File path) {
        assert doc != null;
        assert path != null;

        try {
            final File directory = path.getParentFile();
            if (directory != null && !directory.exists()) {
                if (!directory.mkdir()) {
                    logger.warn("Failed to create target directory for XML document output");
                    return;
                }
            }

            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            DOMSource source = new DOMSource(doc.getDocumentElement());
            FileOutputStream fileStream = new FileOutputStream(path);
            StreamResult stream = new StreamResult(fileStream);

            transformer.transform(source, stream);
            logger.info("Saved XML document to {}", path.getCanonicalPath());
        } catch (TransformerException e) {
            logger.warn("Failed to transform XML document for output to a file", e);
        } catch (FileNotFoundException e) {
            logger.warn("Failed to save XML document to a file", e);
        } catch (IOException e) {
            // This is debug because it only impacts the log output
            logger.debug("Failed to get canonical path for output file");
        }
    }
}
