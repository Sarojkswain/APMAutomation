package com.ca.apm.systemtest.fld.plugin.file.transformation;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.common.spel.ConfigurationPlaceholderResolver;
import com.ca.apm.systemtest.fld.common.spel.StringEvaluator;
import com.ca.apm.systemtest.fld.plugin.AbstractPluginImpl;
import com.ca.apm.systemtest.fld.plugin.AppServerPlugin;
import com.ca.apm.systemtest.fld.plugin.Plugin;
import com.ca.apm.systemtest.fld.plugin.PluginConfiguration;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.downloadMethod.HttpDownloadMethod;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactManagerException;
import com.ca.apm.systemtest.fld.plugin.file.transformation.config.Binding;
import com.ca.apm.systemtest.fld.plugin.file.transformation.config.Configuration;
import com.ca.apm.systemtest.fld.plugin.file.transformation.config.Files;
import com.ca.apm.systemtest.fld.plugin.file.transformation.config.Transformation;
import com.ca.apm.systemtest.fld.plugin.file.transformation.config.binding.FilesRef;
import com.ca.apm.systemtest.fld.plugin.file.transformation.config.binding.TransformationRef;
import com.ca.apm.systemtest.fld.plugin.file.transformation.config.transformation.TransformationBase;
import com.ca.apm.systemtest.fld.plugin.file.transformation.config.transformation.TransformationContext;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;


/**
 * Plugin to transform/edit files.
 *
 * @author haiva01
 */
public class FileTransformationPluginImpl extends AbstractPluginImpl implements FileTransformationPlugin, ApplicationContextAware {
    private static final Logger log = LoggerFactory.getLogger(FileTransformationPluginImpl.class);

    private File tempDir;

    @Autowired
    private HttpDownloadMethod dm;

    private Map<String, Plugin> plugins;
    
    public FileTransformationPluginImpl() {
        plugins = new HashMap<>();
    }



    @Override
    @ExposeMethod(
        description = "Transform files using transformation file downloaded from given URL")
    public void transformUrl(String configurationUrl, ConfigurationFormat format, 
                             Map<String, Object> vars) {
        Configuration configuration;
        try {
            configuration = readConfigurationFromUrl(new URL(configurationUrl), format);
        } catch (IOException | JAXBException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to read transformation configuration. Exception: {0}");
        }

        vars = addPluginsToVars(vars);
        
        try {
            runTransformations(configuration, vars);
        } catch (Exception e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Transformation failed. Exception: {0}");
        }
    }


    @Override
    @ExposeMethod(description = "Transform files using transformation passed as string")
    public void transform(String configurationString, ConfigurationFormat format,
                          Map<String, Object> vars) {
        Configuration configuration;
        try {
            configuration = readConfigurationFromString(configurationString, format);
        } catch (IOException | JAXBException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to read transformation configuration. Exception: {0}");
        }
        
        vars = addPluginsToVars(vars);

        try {
            runTransformations(configuration, vars);
        } catch (Exception e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Transformation failed. Exception: {0}");
        }
    }

    /**
     * Adds the {@link AppServerPlugin} beans to the vars for use in evaluating expressions
     * @param vars
     * @return
     */
    private Map<String, Object> addPluginsToVars(Map<String, Object> vars) {
        HashMap<String, Object> map = new HashMap<>(vars);
        
        for (Entry<String, Plugin> entry: plugins.entrySet()) {
            Plugin p = entry.getValue();
            PluginConfiguration cfg = p.getPluginConfiguration();
            if (!(cfg instanceof EmptyPluginConfiguration)) {
                map.put(entry.getKey(), p);
            }
        }
        
        return map;
    }



    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        plugins = new HashMap<>();
        Map<String, Plugin> map = applicationContext.getBeansOfType(Plugin.class);
        for (Entry<String, Plugin> e: map.entrySet()) {
            PluginAnnotationComponent pac = applicationContext.findAnnotationOnBean(e.getKey(), PluginAnnotationComponent.class);
            plugins.put(pac.pluginType(), e.getValue());
        }
    }

    private Configuration readConfigurationFromXmlFile(
        File configurationFile) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (Configuration) jaxbUnmarshaller.unmarshal(configurationFile);
    }


    private Configuration readConfigurationFromJsonFile(File configurationFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector(
            mapper.getTypeFactory());
        mapper.setAnnotationIntrospectors(introspector, introspector);
        return mapper.readValue(configurationFile, Configuration.class);
    }


    private Configuration readConfigurationFromXmlString(String str) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (Configuration) jaxbUnmarshaller.unmarshal(IOUtils.toInputStream(str));
    }


    private Configuration readConfigurationFromJsonString(String str) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector(
            mapper.getTypeFactory());
        mapper.setAnnotationIntrospectors(introspector, introspector);
        return mapper.readValue(str, Configuration.class);
    }


    private Configuration readConfigurationFromFile(File configurationFile,
        ConfigurationFormat configurationFormat) throws JAXBException, IOException {
        Configuration config;

        switch (configurationFormat) {
            case XML:
                config = readConfigurationFromXmlFile(configurationFile);
                break;

            case JSON:
                config = readConfigurationFromJsonFile(configurationFile);
                break;

            default:
                throw ErrorUtils.logErrorAndReturnException(log,
                    "Unhandled configuration format: {0}", configurationFormat);
        }

        return config;
    }


    private Configuration readConfigurationFromString(String str,
        ConfigurationFormat configurationFormat) throws JAXBException, IOException {
        Configuration config;

        switch (configurationFormat) {
            case XML:
                config = readConfigurationFromXmlString(str);
                break;

            case JSON:
                config = readConfigurationFromJsonString(str);
                break;

            default:
                throw ErrorUtils.logErrorAndReturnException(log,
                    "Unhandled configuration format: {0}", configurationFormat);
        }

        return config;
    }


    private Configuration readConfigurationFromUrl(URL configurationUrl,
        ConfigurationFormat configurationFormat) throws JAXBException, IOException {
        if (configurationUrl == null) {
            throw ErrorUtils.logErrorAndReturnException(log,
                "Transformation configuration URL not set.");
        }
        if (configurationFormat == null) {
            throw ErrorUtils.logErrorAndReturnException(log,
                "Transformation configuration format not set.");
        }

        File configurationFile = downloadConfiguration(configurationUrl);
        return readConfigurationFromFile(configurationFile, configurationFormat);
    }


    private void runTransformations(Configuration configuration, Map<String, Object> vars)
            throws Exception {
        Map<String, Transformation> transformationsMap = configuration.getTransformationsMap();
        Map<String, Files> filesMap = configuration.getFilesMap();

        List<Binding> bindings = configuration.getBindings();
        for (Binding binding : bindings) {
            processBinding(binding, transformationsMap, filesMap, vars);
        }
    }

    private void processBinding(Binding binding, Map<String, Transformation> transformationsMap,
                                Map<String, Files> filesMap, Map<String, Object> vars) 
                                    throws Exception {
        log.debug("Processing binding with ID {}", binding.getId());

        final List<FilesRef> filesRefs = binding.getFilesRef();
        final List<TransformationRef> transformationRefs = binding.getTransformationRef();
        ConfigurationPlaceholderResolver resolver = new ConfigurationPlaceholderResolver(vars);
        final StringEvaluator evaluator = new StringEvaluator(resolver);
        for (FilesRef filesRef : filesRefs) {
            processBindingFileRef(filesRef, transformationRefs, transformationsMap, filesMap,
                evaluator);
        }
    }


    private void processBindingFileRef(FilesRef filesRef,
        Iterable<TransformationRef> transformationRefs,
        Map<String, Transformation> transformationsMap, Map<String, Files> filesMap,
        StringEvaluator evaluator) throws Exception {
        final String frefId = filesRef.getId();
        log.debug("Processing files-ref to files group ID {}", frefId);
        final Files filesGroup = filesMap.get(frefId);
        if (filesGroup == null) {
            log.error("Files group with ID {} is not declared", frefId);
            return;
        }

        for (String fileString : filesGroup.getFiles()) {
            processBindingSingleFile(fileString, transformationRefs, transformationsMap, evaluator);
        }
    }


    private void processBindingSingleFile(String fileString,
        Iterable<TransformationRef> transformationRefs,
        Map<String, Transformation> transformationsMap, StringEvaluator evaluator)
        throws Exception {
        final String evaluatedFilename = evaluator.evaluateString(fileString);
        log.debug("Evaluated file path to: {}", evaluatedFilename);
        for (TransformationRef transRef : transformationRefs) {
            final String transRefId = transRef.getId();
            log.debug("Processing transformation-ref with ID {} on file {}",
                transRefId, evaluatedFilename);
            final Transformation transformation = transformationsMap.get(transRefId);
            if (transformation == null) {
                log.error("Transformation group with ID {} is not declared", transRefId);
                continue;
            }

            final List<TransformationBase> transList = transformation.getTransformations();
            doOneFileTransformations(transList, evaluatedFilename, evaluator);
        }
    }


    private void doOneFileTransformations(Iterable<TransformationBase> transList,
        String evaluatedFilename, StringEvaluator evaluator) throws Exception {
        for (TransformationBase trans : transList) {
            try {
                doSingleTransformation(evaluatedFilename, trans, evaluator);
            } catch (Exception e) {
                throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                    "Failed transformation {1} on {2}. Exception: {0}",
                    trans.toString(), evaluatedFilename);
            }
            log.info("Transformation {} applied to {}", trans.toString(),
                evaluatedFilename);
        }
    }


    private void doSingleTransformation(String filePath, TransformationBase trans,
        StringEvaluator stringEvaluator) throws Exception {
        trans.apply(filePath, new TransformationContext(stringEvaluator));
    }


    private File downloadConfiguration(URL url) {
        ArtifactFetchResult fetchResult;
        try {
            fetchResult = dm.fetch(url.toString(), getTempDir().getAbsoluteFile(), true);
        } catch (ArtifactManagerException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to download configuration from {1}. Exception: {0}", url.toString());
        }
        return fetchResult.getFile();
    }


    @ExposeMethod(description = "Creates temporary working directory.")
    @Override
    public void createTempDir() {
        try {
            setTempDir(
                java.nio.file.Files.createTempDirectory("file-transformation-plugin").toFile());
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrap(log, e,
                "Failed to create temporary directory. Exception: {0}");
        }
    }


    /**
     * Get temporary directory File.
     *
     * @return temporary directory as File
     */
    public File getTempDir() {
        if (tempDir == null) {
            createTempDir();
        }

        return tempDir;
    }

    public void setTempDir(File tempDir) {
        this.tempDir = tempDir;
    }


    @ExposeMethod(description = "Delete temporary working directory.")
    @Override
    public void deleteTempDir() {
        File tempDir = null;
        try {
            tempDir = getTempDir();
            FileUtils.deleteDirectory(getTempDir());
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to delete directory {1}. Exception: {0}", tempDir);
        }
    }
}
