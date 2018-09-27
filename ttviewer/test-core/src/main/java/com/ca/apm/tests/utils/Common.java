/*
 * Copyright (c) 2017 CA. All rights reserved.
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

package com.ca.apm.tests.utils;

import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.tas.restClient.IRestResponse;
import com.ca.tas.restClient.RestClient;
import com.fasterxml.jackson.databind.util.ISO8601Utils;

public class Common {
    private static final Logger log = LoggerFactory.getLogger(Common.class);

    /*
     * Change ServerName from localhost to collector machine name Takes scenario
     * file name, file location and machine name as input and changes server
     * from localhost to machine name in file.
     */

    public void changemyH2ServerHost(String Filename, String location, String machineName,
        String placeholder) {
        String dbtcScenario;
        try {
            dbtcScenario = FileUtils.readFileToString(new File(location + "\\" + Filename));
            String newdbtcScenario = dbtcScenario.replaceAll(placeholder, machineName);
            FileUtils.writeStringToFile(new File(location + "\\" + Filename), newdbtcScenario);
        } catch (IOException e) {
            e.printStackTrace();
            log.info(" Failed to change host machine name in problem scenario xml file  ");
        }
    }

    /*
     * Starts PipeOrgan Run Scripts on machines Takes bat file location, bat
     * file name and scenario file name as input and starts that script on
     * specified machine.
     */
    public RunCommandFlowContext runPipeOrganScenario(String location, String batFile,
        String folderName, String xmlScript) {
        String command =
            "START /D " + location + " " + batFile + " \"" + location + "*\"" + " ./" + folderName
                + "/" + xmlScript;

        RunCommandFlowContext runCommandFlowContext =
            new RunCommandFlowContext.Builder("").args(Arrays.asList(command)).build();

        return runCommandFlowContext;
    }

    /*
     * Stops PipeOrgan Process on machine Takes process name as input and stops
     * that process on specified machine.
     */
    public RunCommandFlowContext stopPipeOrganProcess(String processName) {

        RunCommandFlowContext runCommandFlowContext =
            new RunCommandFlowContext.Builder("").args(
                Arrays.asList("wmic process Where \"CommandLine Like '%" + processName
                    + "%'\" Call Terminate && exit")).build();
        return runCommandFlowContext;
    }

    /*
     * Starts a transaction trace session on TomcatAgent when requested.
     */
    public RunCommandFlowContext runCLWTraceonScenario(String clwLocation, String machineName) {

        String command =
            "cd "
                + clwLocation
                + " && java -Xmx256M -Duser=admin -Dpassword= -Dhost="
                + machineName
                + " -Dport=5001 -jar \""
                + clwLocation
                + "\\CLWorkstation.jar\" trace transactions exceeding 1 ms in agents matching \".*Tomcat Agent.*\" for 60 seconds";

        RunCommandFlowContext runCommandFlowContext =
            new RunCommandFlowContext.Builder("").args(Arrays.asList(command)).build();

        return runCommandFlowContext;
    }


    /*
     * Returns current TimeStamp and 2hrsPastTime for testing time. Takes in int
     * value representing hours in past.
     */
    public Timestamp getCurrentTimeinISO8601Format(int Hours) {

        Calendar calendar = Calendar.getInstance();
        Date currentDate = new Date();
        calendar.setTime(currentDate);
        calendar.add(Calendar.HOUR, +Hours);

        Date twoHourTime = calendar.getTime();
        Timestamp currentTimestamp = new Timestamp(twoHourTime.getTime());
        return currentTimestamp;
    }

    /* Converts TimeStamp to String for Rest Calls */
    public String timestamp2String(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        if (timestamp.getTime() % 1000 == 0) {
            // no need to include milliseconds
            return ISO8601Utils.format(timestamp);
        } else {
            return ISO8601Utils.format(timestamp, true);
        }
    }

    /* Converts String to TimeStamp for testing */
    public Timestamp string2Timestamp(String timestamp) throws ParseException {
        if (!StringUtils.isEmpty(timestamp)) {
            Date date = ISO8601Utils.parse(timestamp, new ParsePosition(0));
            return new Timestamp(date.getTime());
        } else {
            return null;
        }

    }

    /* Converts String to TimeStamp for testing */
    public Timestamp epoch2Timestamp(Long timestamp) throws ParseException {
        if (!(timestamp == null)) {
            Date date = new Date();
            date.setTime(timestamp);
            return new Timestamp(date.getTime());
        } else {
            return null;
        }

    }

    public String restResponse(RestClient restClient, String urlPart, String payload)
        throws IOException {
        EmRestRequest request = new EmRestRequest(urlPart, payload);
        IRestResponse<String> response = restClient.process(request);
        return response.getContent();

    }

    public int restIResponse(RestClient restClient, String urlPart, String payload)
        throws IOException {
        EmRestRequest request = new EmRestRequest(urlPart, payload);
        IRestResponse<String> response = restClient.process(request);
        return response.getHttpStatus().getStatusCode();

    }

    public boolean isNull(Object obj) {
        return obj == null;
    }

    // Checks to see if start time for a trace occurs after/at the start time filter given in REST.
    // Also checks to see if start time in trace is empty.
    public void verifyTraceTime(Long startTime, Long restStartTime) throws ParseException {

        if (startTime == null) {
            fail(" Start Time of Trace is empty.");
        } else {
            if (startTime <= restStartTime) {
                fail("Start Time for Trace is before Start Time of Rest filter. Time filter in Rest doesn't work. ");
            }
        }

    }


    /**
     * This function wraps RE pattern compilation and matching on given text.
     * 
     * @param patternStr RE pattern
     * @param text string to match pattern against
     * @return {@link Matcher} on match, {@code null} otherwise.
     */
    public static Matcher matchRegExAndLog(@Language("RegExp") String patternStr, CharSequence text) {
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(text);
        log.info("testing text >{}< against RE /{}/", text, pattern.pattern());
        boolean match = matcher.matches();
        log.info("result: {}", match ? "match found" : "match NOT found");
        return match ? matcher : null;
    }
}
