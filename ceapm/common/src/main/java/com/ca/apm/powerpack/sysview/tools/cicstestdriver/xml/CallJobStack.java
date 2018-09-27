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

/**
 * Definition for a job stack to be called.
 * Jobs are run serially.
 * @author macbr01
 *
 */
public class CallJobStack
{
    //***********************************************
    // Configuration properties
    // Warning: MAKE SURE YOU CLONE ANY NEW MEMBERS
    //***********************************************
    private String name = "";       // The name of this job stack
    private String description = "";       // A description of this job stack

    private int numberOfThreadsToRun = 1;       // Number of threads to run

    private String  userID = null;
    private String  password = null;
    private String  jGate = "tcp://usilca31";
    private int     jGatePort = 2008;
    private String  serverName = "C660IPIC";
    private boolean useChannel = false;
    private boolean useCommarea = false;
    private boolean useDynamicDecoration = false;
    private String  programDataStr = null;

    // Socket configuration options
    private String socketServer = "usilca31";
    private int    socketPort = 15040;

    private ArrayList<CallDistribution> calledDistributions = null;

    //**********************************
    // Derived Configuration properties
    //**********************************

    // There are no derived configuration properties

    public CallJobStack()
    {
    }

    public CallJobStack(String inName)
    {
        name = inName;
    }

    public CallJobStack(String inName, ArrayList<CallDistribution> inCalledDistributions)
    {
        name = inName;
        calledDistributions = inCalledDistributions;
    }

    public static CallJobStack getInstance(String mappingFile, String xmlFile)
    {
        //          new ReadXmlFile(xmlFile, mappingFile);
        XMLParser xmlparser = new XMLParser(mappingFile);
        return xmlparser.getConfig(xmlFile);
    }


    public CallJobStack clone()
    {
        CallJobStack callJobStack = new CallJobStack(name);

        // Clone all fields
        callJobStack.setDescription(description);
        callJobStack.setNumberOfThreadsToRun(numberOfThreadsToRun);
        callJobStack.setJGate(jGate);
        callJobStack.setJGatePort(jGatePort);
        callJobStack.setServerName(serverName);
        callJobStack.setUseChannel(useChannel);
        callJobStack.setUseCommarea(useCommarea);
        callJobStack.setUseDynamicDecoration(useDynamicDecoration);
        callJobStack.setProgramDataStr(programDataStr);
        callJobStack.setSocketServer(socketServer);
        callJobStack.setSocketPort(socketPort);

        if (calledDistributions != null)
        {
            int numberOfDistributions = calledDistributions.size();
            for (int index = 0; index < numberOfDistributions; index++)
            {
                callJobStack.addCallDistribution(calledDistributions.get(index).clone());
            }
        }
        return callJobStack;
    }


    // Getters and Setters
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public int getNumberOfThreadsToRun()
    {
        if (numberOfThreadsToRun <= 0) {
            numberOfThreadsToRun = 1;
        }

        return numberOfThreadsToRun;
    }

    public void setNumberOfThreadsToRun(int numberOfThreadsToRun)
    {
        this.numberOfThreadsToRun = numberOfThreadsToRun;
    }

    public String getUserID()
    {
        return userID;
    }

    public void setUserID(String inUserID)
    {
        if (inUserID != null)
        {
            inUserID = inUserID.trim();
            if (inUserID.length()== 0) {
                inUserID = null;
            }
        }
        this.userID = inUserID;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String inPassword)
    {
        if (inPassword != null)
        {
            inPassword = inPassword.trim();
            if (inPassword.length()== 0) {
                inPassword = null;
            }
        }
        password = inPassword;
    }

    public String getJGate()
    {
        return jGate;
    }

    public void setJGate(String jGate)
    {
        this.jGate = jGate;
    }

    public int getJGatePort()
    {
        return jGatePort;
    }

    public void setJGatePort(int jGatePort)
    {
        this.jGatePort = jGatePort;
    }

    public String getServerName()
    {
        return serverName;
    }

    public void setServerName(String serverName)
    {
        this.serverName = serverName;
    }

    public boolean isUseChannel()
    {
        return useChannel;
    }

    public void setUseChannel(boolean useChannel)
    {
        this.useChannel = useChannel;
    }

    public boolean isUseCommarea()
    {
        return useCommarea;
    }

    public void setUseCommarea(boolean useCommarea)
    {
        this.useCommarea = useCommarea;
    }

    public boolean isUseDynamicDecoration()
    {
        return useDynamicDecoration;
    }

    public void setUseDynamicDecoration(boolean useDynamicDecoration)
    {
        this.useDynamicDecoration = useDynamicDecoration;
    }

    public String getProgramDataStr()
    {
        return programDataStr;
    }

    public void setProgramDataStr(String programDataStr)
    {
        this.programDataStr = programDataStr;
    }

    public String getSocketServer()
    {
        return socketServer;
    }

    public void setSocketServer(String socketServer)
    {
        this.socketServer = socketServer;
    }

    public int getSocketPort()
    {
        return socketPort;
    }

    public void setSocketPort(int socketPort)
    {
        this.socketPort = socketPort;
    }

    public ArrayList<CallDistribution> getCalledDistributions()
    {
        return calledDistributions;
    }

    public void setCalledDistributions(ArrayList<CallDistribution> inCalledDistributions)
    {
        calledDistributions = inCalledDistributions;
    }

    /**
     * Add a new call distribution
     * @param callDistribution Distribution call.
     */
    public void addCallDistribution(CallDistribution callDistribution)
    {
        if (calledDistributions == null) {
            calledDistributions = new ArrayList<CallDistribution>();
        }

        calledDistributions.add(callDistribution);
    }
}
