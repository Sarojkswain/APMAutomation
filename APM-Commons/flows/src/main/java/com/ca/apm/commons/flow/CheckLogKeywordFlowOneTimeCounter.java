/*
 * Copyright (c) 2015 CA. All rights reserved.
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
package com.ca.apm.commons.flow;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
//import com.ca.apm.tests.utils.emutils.EmBatLocalUtils;

/**
 * Flow that checks if some keywords appear in chosen logs.
 * Configuration in context
 * 
 * @author dwiba01
 *
 */
public class CheckLogKeywordFlowOneTimeCounter implements IAutomationFlow {



    private static final Logger LOGGER = LoggerFactory.getLogger(RunCommandFlow.class);


    @FlowContext
    private CheckLogKeywordFlowOneTimeCounterContext context;

    public CheckLogKeywordFlowOneTimeCounter(CheckLogKeywordFlowOneTimeCounterContext context) {
        this.context = context;
    }

    public CheckLogKeywordFlowOneTimeCounter() {

    }

    /*
     * (non-Javadoc)
     * Will fail the test by throwing exception if keyword is not found
     * 
     * @see com.ca.apm.automation.action.core.IAutomationFlow#run()
     */
    @Override
    public void run() throws Exception {

    	int counter=0;
    	counter=isKeywordInLog(context.getLogPath(), context.getTextToMatch());
    	LOGGER.info("Required keyword found in log:::"+counter+"times");
        if (counter>1) {
        	throw new IllegalStateException("Keyword found in log ::"+counter+"times ######");
        } else if(counter==1)
        	LOGGER.info("Required keyword found only one time in log."+"everything's fine.");
        else {
            throw new IllegalStateException("Required keyword not in log.");
        }
    }

    /**
     * Looks through log and checks if some keyword appears in it.
     * 
     * 
     * @param keyword
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static int isKeywordInLog(String logfilePath, String keyword)
        throws FileNotFoundException, IOException {

        LOGGER.info("Looking for " + keyword + " in " + logfilePath);
        int temp=0;
        try (FileReader fr = new FileReader(logfilePath);
            BufferedReader br = new BufferedReader(fr);) {
            String line = br.readLine();
            while (line != null) {
                if (line.contains(keyword)) {
                   
                    //return true;
                	temp++;
                	 LOGGER.info("Keyword found::::"+temp+"   Keyword:::"+keyword);
                }
                line = br.readLine();
            }

        }
        return temp;
    }

}
