package com.ca.apm.systemtest.fld.common;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Freemarker engine utilities.
 */
public class FreemarkerUtils {
    private static final Version FREEMARKER_VERSION = new Version(2, 3, 21);
    private static final Logger log = LoggerFactory.getLogger(FreemarkerUtils.class);

    static {
        try {
            freemarker.log.Logger.selectLoggerLibrary(freemarker.log.Logger.LIBRARY_SLF4J);
        } catch (ClassNotFoundException e) {
            log.error("Impossible has happened!");
        }
        freemarker.log.Logger.setCategoryPrefix("freemarker");
    }

    public static Configuration getConfig() {
        Configuration freemarkerConfig = new Configuration(FREEMARKER_VERSION);
        freemarkerConfig.setObjectWrapper(new DefaultObjectWrapperBuilder(FREEMARKER_VERSION)
            .build());
        freemarkerConfig.setTemplateLoader(new ClassTemplateLoader(FreemarkerUtils.class, "/"));
        return freemarkerConfig;
    }


    /**
     * This function loads given Freemarker template.
     *
     * @param freemarkerConfig
     * @param templateName     name of the template to be loaded
     * @param encoding         encoding of the template file
     * @return
     */
    public static Template getTemplate(Configuration freemarkerConfig, String templateName,
        String encoding) {
        Template template;
        try {
            template = freemarkerConfig.getTemplate(templateName, Locale.ENGLISH, encoding);
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to retrieve {1} template. Exception: {0}", templateName);
        }
        return template;
    }


    /**
     * This function loads given Freemarker template.
     *
     * @param freemarkerConfig
     * @param templateName     name of the template to be loaded
     * @return
     */
    public static Template getTemplate(Configuration freemarkerConfig, String templateName) {
        Template template;
        try {
            template = freemarkerConfig.getTemplate(templateName, Locale.ENGLISH);
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to retrieve {1} template. Exception: {0}", templateName);
        }
        return template;
    }


    /**
     * Process given template with given properties and save output into given file.
     *
     * @param outputFile output file
     * @param template   template
     * @param props      properties
     */
    public static void processTemplate(File outputFile, Template template,
        Map<String, Object> props) {
        try (Writer out = new FileWriterWithEncoding(outputFile, StandardCharsets.UTF_8)) {
            template.process(props, out);
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to write temporary file {1}. Exception: {0}", outputFile);
        } catch (TemplateException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to process {1} template. Exception: {0}", template.getName());
        }
    }

    /**
     * Process given template with given properties and save output into given file with given
     * encoding.
     *
     * @param outputFile output file
     * @param template   template
     * @param props      properties
     * @param encoding   output file encoding
     */
    public static void processTemplate(File outputFile, Template template,
        Map<String, Object> props, String encoding) {
        try (Writer out = new FileWriterWithEncoding(outputFile, encoding)) {
            template.process(props, out);
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to write temporary file {1}. Exception: {0}", outputFile);
        } catch (TemplateException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to process {1} template. Exception: {0}", template.getName());
        }
    }
}
