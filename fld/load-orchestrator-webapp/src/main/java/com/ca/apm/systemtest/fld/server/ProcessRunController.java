package com.ca.apm.systemtest.fld.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.util.json.JSONArray;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Testing process run controller.
 * @author KEYJA01
 *
 */
@Controller
public class ProcessRunController {
    Logger log = LoggerFactory.getLogger(ProcessRunController.class);

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

    public ProcessRunController() {}

    /**
     * Processes the request.
     * @param processName
     * @param allRequestParams
     * @param model
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/startProcess", method = RequestMethod.POST)
    public String runProcess(
        @RequestParam(required = true, value = "processName") String processName,
        @RequestParam Map<String, String> allRequestParams, ModelMap model) {

        ProcessEngineConfiguration config = processEngine.getProcessEngineConfiguration();
        System.out.println("asyncFailedJobWaitTime: " + config.getAsyncFailedJobWaitTime());
        AsyncExecutor executor = config.getAsyncExecutor();
        System.out.println("maxAsyncJobsDuePerAquisition: "
            + executor.getMaxAsyncJobsDuePerAcquisition());
        System.out.println("asyncJobLockTimeInMillis: " + executor.getAsyncJobLockTimeInMillis());
        CommandExecutor ce = executor.getCommandExecutor();
        System.out.println(ce);

        HashMap<String, Object> args = new HashMap<>();
        HashMap<String, String> params = new HashMap<>();

        for (int i = 1; i < 20; i++) {

            String key = allRequestParams.get("p" + i + "name");
            String val = allRequestParams.get("p" + i + "val");

            if (StringUtils.hasText(key) && StringUtils.hasText(val)) {
                args.put(key, val);
                params.put(key, val);
                System.err.println("key: " + key + "; value: " + val);
            }
        }

        boolean oldWay = false;
        ProcessInstance process = null;

        if (oldWay) {
            process = runtimeService.startProcessInstanceByKey(processName, args);
        } else {
            ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
            ProcessDefinition processDefinition =
                query.processDefinitionKey(processName).latestVersion().singleResult();
            process = formService.submitStartFormData(processDefinition.getId(), params);
        }
        log.info("Created process instance {}", process);
        System.out.println("Created process instance " + process);

        if (process.isEnded()) {
            return "-1";
        } else {
            return process.getProcessInstanceId();
        }
        // return "<html><head><title>Foo foo foo</title></head><body><script>alert('you win!')</script><a href=\"upload.html\">Back to upload page</a><br/></body></html>";
    }

    /**
     * Processes the request.
     * @param processInstanceId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/stopProcess", method = RequestMethod.POST)
    public String stopProcess(
        @RequestParam(required = true, value = "processInstanceId") String processInstanceId) {
            System.out.println("Deleting process instance " + processInstanceId);
            runtimeService.deleteProcessInstance(processInstanceId, "user request");
            return ("success");
    }

    /**
     * Processes the request.
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/listProcesses", method = RequestMethod.POST)
    public String listProcesses() {
        ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
        List<ProcessInstance> instances = query.list();
        JSONArray array = new JSONArray();
        for (ProcessInstance i : instances) {
            JSONObject jo = new JSONObject();
            jo.put("id", i.getProcessDefinitionId());
            jo.put("name", i.getProcessDefinitionName());
            jo.put("instanceId", i.getProcessInstanceId());
            array.put(jo);
        }
        JSONObject json = new JSONObject();
        json.put("processes", array);
        return (json.toString());
    }

}
