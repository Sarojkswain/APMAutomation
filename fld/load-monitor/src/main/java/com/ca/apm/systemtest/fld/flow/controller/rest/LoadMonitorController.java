package com.ca.apm.systemtest.fld.flow.controller.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ca.apm.systemtest.fld.flow.controller.service.FldControllerService;
import com.ca.apm.systemtest.fld.flow.controller.vo.BaseVO;
import com.ca.apm.systemtest.fld.flow.controller.vo.ErrorInfo;
import com.ca.apm.systemtest.fld.flow.controller.vo.LoadsStatusesVO;

/**
 * REST controller providing load monitoring and controlling end-points. 
 * 
 * @author haiva01
 */
@RestController
@RequestMapping("/loads")
public class LoadMonitorController {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadMonitorController.class);
    
    @Autowired
    private FldControllerService fldControllerService;

    @RequestMapping(value = "/status", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    LoadsStatusesVO getStatus() {
        return fldControllerService.getLoadStatuses();
    }
    
    @RequestMapping(value = "/start", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    BaseVO startLoad(@RequestParam(value = "loadId", required = true) String loadId,
                     @RequestParam(value = "force", required = false, defaultValue = "false") Boolean force) {
        if (force) {
            fldControllerService.forceStartLoad(loadId);
        } else {
            fldControllerService.startLoad(loadId);
        }
        return new BaseVO();
    }

    @RequestMapping(value = "/stop", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    BaseVO stopLoad(@RequestParam(value = "loadId", required = true) String loadId,
                    @RequestParam(value = "force", required = false, defaultValue = "false") Boolean force) {
        if (force) {
            fldControllerService.forceStopLoad(loadId);
        } else {
            fldControllerService.stopLoad(loadId);
        }
        return new BaseVO();
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorInfo> handleException(Throwable ex) {
        LOGGER.error("FLD Load Monitor Controller | unexpected exception: ", ex);
        String errMessage = ex.getClass().getSimpleName() + ": " + ex.getMessage();
        ErrorInfo message = new ErrorInfo(null, errMessage);

        ResponseEntity<ErrorInfo> retval =
            new ResponseEntity<ErrorInfo>(message, HttpStatus.INTERNAL_SERVER_ERROR);

        return retval;
    }

//  @RequestMapping(value = "/acknowledge/start/{loadId}", method = RequestMethod.POST)
//  @ResponseBody
//  BaseVO acknowledgeStartRequest(@PathVariable String loadId) {
//      fldControllerService.acknowledgeStartRequest(loadId);
//      return new BaseVO();
//  }
//
//  @RequestMapping(value = "/acknowledge/stop/{loadId}", method = RequestMethod.POST)
//  @ResponseBody
//  BaseVO acknowledgeStopRequest(@PathVariable String loadId) {
//      fldControllerService.acknowledgeStopRequest(loadId);
//      return new BaseVO();
//  }
//
//  @RequestMapping(value = "/register/{loadId}", method = RequestMethod.POST)
//  @ResponseBody
//  BaseVO registerLoad(@PathVariable String loadId) {
//      fldControllerService.registerListener(loadId);
//      return new BaseVO();
//  }

}
