
package com.ca.apm.systemtest.fld.plugin.em;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

public class AcceptEula {


    @Test
    public void acceptIntroscopeEula() throws Exception {
        Path eula = prepareEula("ca-eula.txt");
        new Eula(eula).acceptEula();

        String content = new String(Files.readAllBytes(eula), StandardCharsets.ISO_8859_1);
        assertTrue(!content.isEmpty(), "File cannot be empty");
        assertTrue(content.trim().endsWith("accept"), "Eula must be accepted");
    }

    @Test
    public void acceptOsgiEula() throws Exception {
        Path eula = prepareEula("eula.txt");
        new Eula(eula).acceptEula();

        String content = new String(Files.readAllBytes(eula), StandardCharsets.ISO_8859_1);
        assertTrue(!content.isEmpty(), "File cannot be empty");
        assertTrue(content.trim().endsWith("accept"), "Eula must be accepted");
    }

    private Path prepareEula(String eula) throws Exception {
        Path outFile = Paths.get("target/eula.txt");
        Enumeration<URL> urls = AcceptEula.class.getClassLoader().getResources(eula);

        try (InputStream input = Files.newInputStream(Paths.get(urls.nextElement().toURI()));
            OutputStream output = Files.newOutputStream(outFile)) {
            IOUtils.copyLarge(input, output);
        }

        return outFile;
    }
}
