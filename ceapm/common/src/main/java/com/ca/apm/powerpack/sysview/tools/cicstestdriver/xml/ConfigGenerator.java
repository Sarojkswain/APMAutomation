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

package com.ca.apm.powerpack.sysview.tools.cicstestdriver.xml;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Class for easy construction of simple/typical CICSTestDriver configuration sets.
 */
public class ConfigGenerator {
    private Collection<CalledTransaction> calls = new ArrayList<>();
    private final CalledUnitOfWork unitOfWork;
    private final CallJobStack jobStack;

    /**
     * Constructor.
     *
     * @param ctgHost CTG host.
     * @param ctgPort CTG port.
     * @param ctgServer CTG CICS definition to use.
     * @param duration Test duration [min].
     * @param delay Delay between program executions [us].
     * @param repeat Number of times to execute the program.
     * @param commarea Commarea data to pass to the program (default). Can be {@code null}.
     */
    public ConfigGenerator(String ctgHost, int ctgPort, String ctgServer, int duration, long delay,
        int repeat, String commarea) throws Exception {

        unitOfWork = new CalledUnitOfWork(1);
        unitOfWork.setTransactionRepeat(repeat);

        CallDistribution distribution =
            new CallDistribution("Generated", duration, true, false, false, 0, delay);
        distribution.addCalledUnitOfWork(unitOfWork);

        jobStack = new CallJobStack("Generated");
        jobStack.setJGate(ctgHost);
        jobStack.setJGatePort(ctgPort);
        jobStack.setServerName(ctgServer);
        jobStack.setUseChannel(false);
        jobStack.setUseCommarea(false);
        jobStack.addCallDistribution(distribution);
        if (commarea != null) {
            jobStack.setUseCommarea(true);
            jobStack.setUseDynamicDecoration(true);
            jobStack.setProgramDataStr(commarea);
        } else {
            jobStack.setUseCommarea(false);
            jobStack.setUseDynamicDecoration(false);
        }
    }

    /**
     * Adds a program call.
     *
     * @param programName Name of CICS program to execute.
     * @param commarea Commarea data to pass to the program (override). Can be {@code null}.
     * @return Reference to the instance the method was called on.
     */
    public ConfigGenerator addProgramCall(String programName, String commarea) {
        CalledTransaction call = new CalledTransaction(programName);
        call.addParameter("1");
        if (commarea != null) {
            call.setCommAreaData(commarea);
            jobStack.setUseCommarea(true);
            jobStack.setUseDynamicDecoration(true);
        }
        calls.add(call);

        return this;
    }

    /**
     * Generates an output XML file.
     *
     * @param mappingFile Mapping file to use.
     * @param outputFile Out file path.
     * @throws IllegalStateException If no program calls were added (via
     * {@link #addProgramCall(String, String)}).
     */
    public void generate(String mappingFile, String outputFile) throws Exception {
        if (calls.isEmpty()) {
            throw new IllegalStateException("No program calls were added");
        }

        for (CalledTransaction call : calls) {
            unitOfWork.addCalledTransaction(call);
        }

        // Write test definition to file
        XMLParser xmlparser = new XMLParser(mappingFile);
        xmlparser.setConfig(jobStack, outputFile);
    }
}