package com.ca.apm.systemtest.ttstormload.server.rest;

import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * Sample app for tt load simulation.
 * 
 * POST http://localhost:8080/tt-stormer/api/simulateTransaction
 * forwardHost: localhost:8080
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 */
@RestController("ttStormController")
public class TTStormController {
    private Logger LOGGER = LoggerFactory.getLogger(TTStormController.class);
    
    public static final String FORWARD_URL_TEMPLATE = "http://%s/tt-stormer/api/simulateTransaction";

    @RequestMapping(value = "/simulateTransaction", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> simulateTransaction(
        @RequestParam(required = false, value = "forwardHosts") String forwardHosts,
        @RequestParam(required = false, value = "waitMillis", defaultValue = "10") Integer waitMillis,
        HttpServletRequest request) throws Exception {
        
        LOGGER.info("simulateTransaction called with params: forwardHosts='{}', waitMillis={}", forwardHosts, waitMillis);
        
        boolean decidedToForward = getRandomBoolean();
        LOGGER.info("Randomly deciding to forward request: {}", decidedToForward ? "yes" : "no");
        
        if (forwardHosts != null && decidedToForward) {
            String[] forwardHostsArray = forwardHosts.split(",");
            String randomlyPickedForwardHost = getRandomForwardHost(forwardHostsArray);
            
            String url = String.format(FORWARD_URL_TEMPLATE, randomlyPickedForwardHost);
            
            LOGGER.info("Forwarding request to {}", url);
            MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
            params.add("waitMillis", waitMillis.toString());
            params.add("forwardHost", getSubtractedRandomForwardHosts(forwardHostsArray, randomlyPickedForwardHost));

            HttpEntity<MultiValueMap<String, String>> httpEntity =
                new HttpEntity<MultiValueMap<String, String>>(params, null);

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForLocation(url, httpEntity);
            LOGGER.info("Forwarded. Quitting.");
            ResponseEntity<String> response = new ResponseEntity<String>("Forwarded.", HttpStatus.OK);
            return response;
        } else if (decidedToForward) {
            LOGGER.info("Decided to forward the request but no forward host was provided, so, just waiting for {} milliseconds and quitting.", waitMillis);
        }

        LOGGER.info("Starting wait. Sleep time: {} milliseconds", waitMillis);
        Thread.sleep(waitMillis);
        LOGGER.info("Waiting finished, quitting.");
        
        ResponseEntity<String> response = new ResponseEntity<String>("Waited.", HttpStatus.OK);
        return response;
    }

    private static boolean getRandomBoolean() {
        return new Random().nextBoolean();
    }
    
    private String getRandomForwardHost(String[] forwardHosts) {
        int randomInd = new Random().nextInt(forwardHosts.length);
        String randomForwardHost = forwardHosts[randomInd];
        LOGGER.info("Picked up random forward host index={}, random forward host is '{}'", randomInd, randomForwardHost);
        return randomForwardHost;
    }
    
    private String getSubtractedRandomForwardHosts(String[] allForwardHosts, String pickedForwardHost) {
        if (allForwardHosts.length == 1) {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        
        for (String forwardHost : allForwardHosts) {
            if (!forwardHost.equals(pickedForwardHost)) {
                if (buffer.length() > 0) {
                    buffer.append(',');
                }
                buffer.append(forwardHost);    
            }
        }
        return buffer.toString();
    }
}
