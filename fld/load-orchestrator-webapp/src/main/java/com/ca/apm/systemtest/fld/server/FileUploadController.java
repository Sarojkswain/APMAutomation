/**
 *
 */
package com.ca.apm.systemtest.fld.server;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletContext;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Validator;
import org.apache.commons.io.IOUtils;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ca.apm.systemtest.fld.server.dao.PropertyValueDao;
import com.ca.apm.systemtest.fld.server.dao.ResourceFileDao;
import com.ca.apm.systemtest.fld.server.manager.DashboardManager;
import com.ca.apm.systemtest.fld.server.model.ConfigItem;
import com.ca.apm.systemtest.fld.server.model.Dashboard;
import com.ca.apm.systemtest.fld.server.model.PropertyValue;
import com.ca.apm.systemtest.fld.server.model.ResourceFile;
import com.ca.apm.systemtest.fld.server.util.BpmnParseError;
import com.ca.apm.systemtest.fld.server.util.BpmnValidationError;
import com.ca.apm.systemtest.fld.server.util.ConfigItemUtil;
import com.ca.apm.systemtest.fld.server.util.ConfigItemUtil.ConfigItemWrapper;
import com.ca.apm.systemtest.fld.server.util.FLDProcessValidator;
import com.ca.apm.systemtest.fld.shared.vo.ErrorMessage;
import com.ca.apm.systemtest.fld.shared.vo.FormPropertyVO;
import com.ca.apm.systemtest.fld.shared.vo.ProcessDefinitionVO;
import com.ca.apm.systemtest.fld.shared.vo.ResourceFileVO;
import com.ca.apm.systemtest.fld.shared.vo.Response;

/**
 * @author keyja01
 */
@Controller
public class FileUploadController {
    Logger log = LoggerFactory.getLogger(FileUploadController.class);

    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private ManagementService managementService;
    @Autowired
    private FormService formService;
    @Autowired
    private PropertyValueDao propertyDao;
    @Autowired
    private ResourceFileDao resourceFileDao;
    @Autowired
    private DashboardManager dashboardManager;

    @Autowired 
    private ServletContext servletContext;
    
    @Autowired
    private Mapper mapper;

    /**
     *
     */
    public FileUploadController() {
    }


    @ResponseBody
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<? extends Response> handleFormUpload(@RequestParam("processArchive") MultipartFile file) {
        // do something with the file
        log.debug("File is empty: {}", file.isEmpty());
        log.debug("File length: {}", file.getSize());
        log.debug("File name: {}", file.getName());
        log.debug("File original file name: {}", file.getOriginalFilename());

        String filename = file.getOriginalFilename();
        String deploymentId = null;
        try {
            DeploymentBuilder builder = repositoryService.createDeployment();
            if (filename.endsWith(".bar") || filename.endsWith(".zip")) {
                builder.addZipInputStream(new ZipInputStream(file.getInputStream()));
            } else {
                builder.addInputStream(filename, file.getInputStream());
            }
            Deployment d = builder.deploy();
            deploymentId = d.getId();
            log.debug("deployment ID: {}", deploymentId);
            log.debug("tenant ID: {}", d.getTenantId());
            
            List<String> resourceNames = repositoryService
                .getDeploymentResourceNames(d.getId());
            for (String name : resourceNames) {
                processPropertiesResource(name,
                    repositoryService.getResourceAsStream(d.getId(), name));
            }
        } catch (Exception e) {
            final String msg = MessageFormat.format(
                "Error parsing uploaded file \"{1}\". Exception: {0}",
                e.getMessage(), filename);
            log.error(msg, e);
            throw new BpmnParseError(msg, e);
        }

        ProcessDefinitionQuery nowDeployedQuery = repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId);
        List<ProcessDefinition> procDefList = nowDeployedQuery.list();

        Validator validator = new FLDProcessValidator();
        List<ValidationError> errors = new ArrayList<>(10);
        for (ProcessDefinition pd : procDefList) {
            log.debug("Validating process definition: {}", pd);
            BpmnModel bpmnModel = repositoryService.getBpmnModel(pd.getId());
            validator.validate(bpmnModel, errors);
        }
        
        if (!errors.isEmpty()) {
            log.error("Model validation has found {} error(s).", errors.size());

            List<String> errorMsgs = new ArrayList<>(errors.size());

            for (ValidationError err : errors) {
                log.error("Error: {}", err.toString());
                errorMsgs.add(err.toString());
            }
            
            throw new BpmnValidationError(file.getOriginalFilename(), errors);
        }

        //Now check start form parameters of the just deployed process definitions 
        //with relative dashboard configurations.
        for (ProcessDefinition procDef : procDefList) {
            StartFormData startFormData = formService.getStartFormData(procDef.getId());
            List<FormProperty> formProps = startFormData.getFormProperties();
            Map<String, ConfigItemWrapper> newConfigItemsMap = null;
            List<ConfigItem> newConfigItems = null; 
            try {
                newConfigItems = ConfigItemUtil.convertFormProperties(formProps);
                newConfigItemsMap = ConfigItemUtil.convertToMap(newConfigItems);
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getMessage(), e.getCause());
                }
                throw new FileUploadException(e.getMessage(), e.getCause());
            }
            
            List<Dashboard> dashboards = getDashboardsByProcessKey(procDef.getKey());
            if (dashboards != null) {
                for (Dashboard dashboard : dashboards) {
                    //Skip running dashboards
                    if (!isDashboardRunning(dashboard)) {
                        List<ConfigItem> oldConfigItems = dashboard.getDashboardConfig().getConfigItems();
                        List<ConfigItem> updatedConfigItems = null;
                        try {
                            updatedConfigItems = ConfigItemUtil.updateConfigItems(newConfigItemsMap, oldConfigItems);
                        } catch (Exception e) {
                            if (log.isErrorEnabled()) {
                                log.error(e.getMessage(), e.getCause());
                            }
                            throw new FileUploadException(e.getMessage(), e.getCause());
                        }
                        if (updatedConfigItems != null && !updatedConfigItems.isEmpty()) {
                            dashboard.getDashboardConfig().setConfigItems(updatedConfigItems);
                        }
                        dashboard.setProcessDefinitionVersion(procDef.getVersion());
                        updateDashboard(dashboard);
                    }
                }
            }
        }

        ProcessDefinitionQuery allQuery = repositoryService.createProcessDefinitionQuery().latestVersion();
        List<ProcessDefinition> allProcDefList = allQuery.list();
        if (log.isDebugEnabled()) {
            log.debug("List of all process definitions deployed by now:");
            for (ProcessDefinition pd : allProcDefList) {
                log.debug("Process definition: {}", pd);
            }
        }

        List<ProcessDefinitionVO> processDefVOs = convertProcDefsToVOs(allProcDefList, true);
        Response response = new Response();
        response.setStatus(HttpStatus.OK);
        response.setProcessDefinitions(processDefVOs);
        response.setResourceFiles(fetchResources());
        return new ResponseEntity<Response>(response, HttpStatus.OK);
    }

    @ResponseBody
    @RequestMapping(value = "/listProcessDefinitions", method = RequestMethod.POST)
    public ResponseEntity<? extends Response> listProcessDefinitions() {
        List<ProcessDefinitionVO> processDefVOs = getProcessDefinitionVOs();
        Response response = new Response();
        response.setStatus(HttpStatus.OK);
        response.setProcessDefinitions(processDefVOs);
        return new ResponseEntity<Response>(response, HttpStatus.OK);
    }

    @ResponseBody
    @RequestMapping(value = "/deleteProcessDefinition/{deploymentId}", method = RequestMethod.DELETE)
    public ResponseEntity<? extends Response> deleteProcessDefinition(
        @PathVariable String deploymentId,
        @RequestParam(required = false, value = "cascade") Boolean cascade) {

        boolean deleteHistory = cascade != null ? cascade : false;
        try {
            repositoryService.deleteDeployment(deploymentId, deleteHistory);
        } catch (Exception e) {
            throw new ActivitiException("Failed to delete process definition (deployment id=" + deploymentId + "): " + e.getMessage(), e);
        }
        return listProcessDefinitions();
    }

    @ResponseBody
    @RequestMapping(value = "/deleteAllProcessDefinitions", method = RequestMethod.DELETE)
    public ResponseEntity<? extends Response> deleteAllProcessDefinitions(
        @RequestParam(required = false, value = "cascade") Boolean cascade) {
        boolean deleteHistory = cascade != null ? cascade : false;
        ProcessDefinitionQuery q = repositoryService.createProcessDefinitionQuery();
        List<ProcessDefinition> procDefs = q.list();
        if (procDefs != null) {
            for (ProcessDefinition procDef : procDefs) {
                try {
                    repositoryService.deleteDeployment(procDef.getDeploymentId(), deleteHistory);    
                } catch (Exception e) {
                    throw new ActivitiException("Exception caught while deleting all process definitions: " + e.getMessage(), e);
                }
            }
        }
        return listProcessDefinitions();
    }

    @ResponseBody
    @RequestMapping(value = "/listProperties", method = RequestMethod.GET)
    @Transactional(propagation = Propagation.REQUIRED)
    public String listPropertyValues(
        @RequestParam(required = false, value = "propertyName") String propertyName,
        @RequestParam(required = false, value = "propertiesFiles") String propertiesFile
    ) {
        List<PropertyValue> properties = null;
        if (propertyName == null && propertiesFile == null) {
            properties = propertyDao.findAll();
        } else if (propertiesFile == null) {
            properties = propertyDao.findByName(propertyName);
        } else if (propertyName == null) {
            properties = propertyDao.findByPropertiesFile(propertiesFile);
        } else {
            properties = new ArrayList<PropertyValue>();
            PropertyValue pv = propertyDao.findByNameAndFile(propertyName, propertiesFile);
            properties.add(pv);
        }
        Collections.sort(properties, new Comparator<PropertyValue>() {
            @Override
            public int compare(PropertyValue o1, PropertyValue o2) {
                return (o1.getPropertiesFile().compareTo(o2.getPropertiesFile()));
            }
        });
        StringBuilder builder = new StringBuilder();
        String lastPropertyFile = "";
        for (PropertyValue pv : properties) {
            if (!lastPropertyFile.equals(pv.getPropertiesFile())) {
                lastPropertyFile = pv.getPropertiesFile();
                builder.append(pv.getPropertiesFile());
                builder.append("\n");
            }
            builder.append(pv.getName());
            builder.append("=");
            builder.append(pv.getValue());
            builder.append("\n");
        }
        return (builder.toString());
    }

    @RequestMapping(value = "/listResources", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<? extends Response> getAllResources() {
        List<ResourceFileVO> resourceFiles = fetchResources();
        Response response = new Response();
        response.setStatus(HttpStatus.OK);
        response.setResourceFiles(resourceFiles);
        return new ResponseEntity<Response>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/deleteResources", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<? extends Response> deleteAllResources() {
        List<ResourceFile> result = resourceFileDao.findAll();
        int numOfAllResources = result != null ? result.size() : 0;
        int numOfRowsDeleted = 0;
        boolean success = true;
        if (numOfAllResources > 0) {
            numOfRowsDeleted = resourceFileDao.deleteAll();
            success = numOfRowsDeleted == numOfAllResources;
        }
        
        if (!success) {
            ErrorMessage em = new ErrorMessage();
            em.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            List<String> errors = new ArrayList<>();
            errors.add(MessageFormat.format("Expected to delete all resources ({0}) but deleted {1} of them!", 
                numOfAllResources, numOfRowsDeleted));
            em.setErrors(errors);
            return new ResponseEntity<ErrorMessage>(em, HttpStatus.BAD_REQUEST);
            
        }
        
        Response response = new Response();
        response.setStatus(HttpStatus.OK);
        
        return new ResponseEntity<Response>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/deleteResourceById", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<? extends Response> deleteResources(
        @RequestParam(required = true, value = "resourceId") Long resourceId) {
        resourceFileDao.deleteById(resourceId);
        Response response = new Response();
        response.setStatus(HttpStatus.OK);
        return new ResponseEntity<Response>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/getResource", method = RequestMethod.GET)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<byte[]> getResourceFile(
        @RequestParam(required = false, value = "resourceName") String resourceName) {
        if (resourceName == null || resourceName.isEmpty()) {
            ResponseEntity<byte[]> re = new ResponseEntity<byte[]>(HttpStatus.BAD_REQUEST);
            return (re);
        }
        ResourceFile rf = resourceFileDao.findByName(resourceName);
        if (rf == null) {
            ResponseEntity<byte[]> re = new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND);
            return (re);
        }

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        headers.add("Content-Disposition", "attachment; filename=\"" + rf.getName() + "\"");
//        headers.add("Content-Type", "text/html");
        return new ResponseEntity<byte[]>(rf.getContent(), headers, HttpStatus.OK);
        
    }

    @ExceptionHandler(BpmnValidationError.class)
    public ResponseEntity<ErrorMessage> handleBpmValidationErrors(BpmnValidationError ex) {
        log.warn("A BPMN validation error has occurred", ex);

        ResponseEntity<ErrorMessage> responseEntity = getGenericErrorResponse(ex);
        List<String> errors = responseEntity.getBody().getErrors();
        if (errors == null) {
            errors = new ArrayList<>();
            responseEntity.getBody().setErrors(errors);
        }
        
        for (ValidationError validationError : ex.getErrors()) {
            errors.add(validationError.toString());    
        }
        return responseEntity;
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ErrorMessage> handleApplicationException(FileUploadException ex) {
        log.warn("An application exception has occurred", ex);

        ErrorMessage em = new ErrorMessage();

        em.setStatus(HttpStatus.BAD_REQUEST);
        List<String> errors = new ArrayList<>();
        errors.add(ex.getMessage());
        if (ex.getCause() != null) {
            errors.add(ex.getCause().getMessage());
        }
        em.setErrors(errors);

        ResponseEntity<ErrorMessage> retval =
            new ResponseEntity<ErrorMessage>(em, HttpStatus.BAD_REQUEST);

        return retval;
    }

    @ExceptionHandler(ActivitiException.class) 
    public ResponseEntity<ErrorMessage> handleActivitiExceptions(ActivitiException ex) {
        log.warn("An ActivitiException has occurred", ex);
        return getGenericErrorResponse(ex);
    }

    @ExceptionHandler(BpmnParseError.class) 
    public ResponseEntity<ErrorMessage> handleBpmnParseErrors(BpmnParseError ex) {
        log.warn("A BpmnParseError has occurred", ex);
        return getGenericErrorResponse(ex);
    }

    private List<ResourceFileVO> fetchResources() {
        List<ResourceFile> resources = resourceFileDao.findAll();
        if (resources == null || resources.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<ResourceFileVO> resourceFiles = new ArrayList<ResourceFileVO>(resources.size());
        for (ResourceFile file : resources) {
            synchronized (mapper) {
                ResourceFileVO resourceFileVO = mapper.map(file, ResourceFileVO.class);
                resourceFileVO.setSizeInBytes(file.getContent() != null ? (long) file.getContent().length : 0);
                resourceFiles.add(resourceFileVO);
            }
        }
        return resourceFiles;
    }
    
    private ResponseEntity<ErrorMessage> getGenericErrorResponse(Exception ex) {
        List<String> errors = new ArrayList<>();
        errors.add(ex.getMessage());
        ErrorMessage em = new ErrorMessage();
        em.setStatus(HttpStatus.BAD_REQUEST);
        em.setErrors(errors);
        return new ResponseEntity<ErrorMessage>(em, HttpStatus.BAD_REQUEST);
    }

    private List<Dashboard> getDashboardsByProcessKey(String processKey) {
        return dashboardManager.getDashboardsByProcessKey(processKey);
    }
    
    private boolean isDashboardRunning(Dashboard dashboard) {
        return dashboardManager.isDashboardRunning(dashboard);
    }
    
    private Dashboard updateDashboard(Dashboard dashboard) {
        return dashboardManager.updateDashboard(dashboard);
    }
    
    private List<ProcessDefinitionVO> getProcessDefinitionVOs() {
        ProcessDefinitionQuery q = repositoryService.createProcessDefinitionQuery()
            .latestVersion();
        List<ProcessDefinition> list = q.list();
        Comparator<? super ProcessDefinition> comp = new Comparator<ProcessDefinition>() {

            @Override
            public int compare(ProcessDefinition p1, ProcessDefinition p2) {
                String n1 = p1.getName();
                String n2 = p2.getName();
                if (n1 == null) {
                    p1.getId();
                }
                if (n2 == null) {
                    p2.getId();
                }
                // failsafe - just use "null"
                if (n1 == null) {
                    n1 = "null";
                }
                if (n2 == null) {
                    n2 = "null";
                }
                return n2.compareTo(n1);
            }
            
        };
        Collections.sort(list, comp);
        return convertProcDefsToVOs(list, true);
    }
    
    private List<ProcessDefinitionVO> convertProcDefsToVOs(List<ProcessDefinition> procDefs, boolean includeFormProperties) {
        //First sort the original list
        Collections.sort(procDefs, new ProcessDefinitionComparatorById());
        
        
        List<ProcessDefinitionVO> processDefVOs = new ArrayList<ProcessDefinitionVO>();
        for (ProcessDefinition procDef : procDefs) {
            ProcessDefinitionVO procDefVO = null;
            synchronized (mapper) {
                procDefVO = mapper.map(procDef, ProcessDefinitionVO.class);
            }

            if (includeFormProperties) {
                StartFormData formData = formService.getStartFormData(procDef.getId());
                List<FormProperty> formProperties = formData.getFormProperties() != null ? formData.getFormProperties() : Collections.<FormProperty>emptyList();
                List<FormPropertyVO> formPropertyVOs = new ArrayList<>(formProperties.size());
                for (FormProperty formProperty : formProperties) {
                    FormPropertyVO formPropertyVO = null;
                    synchronized (mapper) {
                        formPropertyVO = mapper.map(formProperty, FormPropertyVO.class);
                    }
                    formPropertyVOs.add(formPropertyVO);
                }
                procDefVO.setFormProperties(formPropertyVOs);
            }
            
            processDefVOs.add(procDefVO);
        }
        return processDefVOs;
    }
    
    private void processPropertiesResource(String name, InputStream stream) {
        name = name != null ? name.toLowerCase() : "";
        
        String allowedResourceExtensions = servletContext.getInitParameter("allowedResourceExtensions");
        allowedResourceExtensions = allowedResourceExtensions != null ? allowedResourceExtensions.trim() : "";
        if (allowedResourceExtensions.length() > 0) {
            String[] extensions = allowedResourceExtensions.split(",");
            if (extensions != null && extensions.length > 0) {
                for (String extension : extensions) {
                    extension = extension != null ? extension.trim().toLowerCase() : "";
                    if (name.endsWith(extension)) {
                        if (extension.equals(".properties")) {
                            Properties properties = new Properties();
                            try {
                                properties.load(stream);
                            } catch (IOException e) {
                                final String msg = MessageFormat.format("Failed to load from {0}. Exception: {1}",
                                    name, e.getMessage());
                                log.error(msg, e);
                            }
                            propertyDao.deleteByPropertiesFile(name);
                            for (String s : properties.stringPropertyNames()) {
                                PropertyValue pv = new PropertyValue();
                                pv.setName(s);
                                pv.setValue(properties.getProperty(s));
                                pv.setPropertiesFile(name);
                                propertyDao.create(pv);
                            }
                        } else {
                            byte[] content = null;
                            try {
                                content = IOUtils.toByteArray(stream);
                            } catch (IOException e) {
                                final String msg = MessageFormat.format(
                                    "Failed to load stream into byte array. Exception: {0}",
                                    e.getMessage());
                                log.error(msg, e);
                            }
                            if (content != null) {
                                resourceFileDao.deleteByName(name);
                                ResourceFile rf = new ResourceFile();
                                rf.setName(name);
                                rf.setContent(content);
                                resourceFileDao.create(rf);
                            }
                        }
                    }
                }
            }            
        }
    }

    private static class ProcessDefinitionComparatorById implements Comparator<ProcessDefinition> {
        @Override
        public int compare(ProcessDefinition pd1, ProcessDefinition pd2) {
            return (pd2.getId().compareTo(pd1.getId()));
        }
    }
}
