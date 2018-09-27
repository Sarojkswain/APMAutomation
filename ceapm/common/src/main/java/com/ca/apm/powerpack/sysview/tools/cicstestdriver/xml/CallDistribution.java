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
 * A distribution of calls over multiple units of work.
 * The number of calls defined for each unit of work is summed and
 * then each is run in proper proportion to the others, for the
 * number of minutes specified.   If minutes are zero then the
 * proportions are used as absolute counts to run.
 * The calls are distributed randomly or not depending on the option.
 * @author macbr01
 *
 */
public class CallDistribution
{
    //***********************************************
    // Configuration properties
    // Warning: MAKE SURE YOU CLONE ANY NEW MEMBERS
    //***********************************************

    private String name = "";       // The name of this distribution
    private long minutesToRun;  // How long to run the distribution
    private boolean useCTG;     // Whether to make CTG calls
    private boolean useWebServices; // Whether to use web services calls
    private boolean useCICSSockets; // Whether to use CICS Sockets calls
    private Long    randomSeed = null;          // Seed to use for random number generator
    private long microsecondsToSleepPerCall = 0; // How long to sleep per call
    private ArrayList<CalledUnitOfWork> calledUnitsOfWork = null; // Units of work in this distribution

    //**********************************
    // Derived Configuration properties
    //**********************************

    // There are no derived configuration properties

    public CallDistribution()
    {
    }


    public CallDistribution(String inName, long inMinutesToRun,
                            boolean inUseCTG, boolean inUseWebServices, boolean inUseCICSSockets,
                            long inRandomSeed, long inMicrosecondsToSleepPerCall)
    {
        name = inName;
        minutesToRun = inMinutesToRun;
        useCTG = inUseCTG;
        useWebServices = inUseWebServices;
        useCICSSockets = inUseCICSSockets;
        randomSeed = inRandomSeed;
        microsecondsToSleepPerCall = inMicrosecondsToSleepPerCall;
    }

    public CallDistribution clone()
    {
        CallDistribution callDistribution = new CallDistribution(name, minutesToRun,
                                                                 useCTG, useWebServices, useCICSSockets,
                                                                 randomSeed, microsecondsToSleepPerCall);


        if (calledUnitsOfWork != null)
        {
            int numberOfUnitsOfWork = calledUnitsOfWork.size();
            for (int index = 0; index < numberOfUnitsOfWork; index++)
            {
                callDistribution.addCalledUnitOfWork(calledUnitsOfWork.get(index).clone());
            }
        }
        return callDistribution;
    }


    // Getters and Setters
    public Long getRandomSeed()
    {
        return randomSeed;
    }


    public void setRandomSeed(Long randomSeed)
    {
        this.randomSeed = randomSeed;
    }
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public long getMinutesToRun()
    {
        return minutesToRun;
    }
    public void setMinutesToRun(long inMinutesToRun)
    {
        minutesToRun = inMinutesToRun;
    }
    public boolean isUseCTG()
    {
        return useCTG;
    }
    public void setUseCTG(boolean inUseCTG)
    {
        useCTG = inUseCTG;
    }
    public boolean isUseWebServices()
    {
        return useWebServices;
    }
    public void setUseWebServices(boolean inUseWebServices)
    {
        useWebServices = inUseWebServices;
    }
    public boolean isUseCICSSockets()
    {
        return useCICSSockets;
    }
    public void setUseCICSSockets(boolean useCICSSockets)
    {
        this.useCICSSockets = useCICSSockets;
    }
    public ArrayList<CalledUnitOfWork> getCalledUnitsOfWork()
    {
        return calledUnitsOfWork;
    }
    public void setCalledUnitsOfWork(ArrayList<CalledUnitOfWork> inCalledUnitsOfWork)
    {
        int numberOfUOW = inCalledUnitsOfWork.size();
        calledUnitsOfWork = new ArrayList<CalledUnitOfWork>();

        for (int index = 0; index < numberOfUOW; index++)
        {
            addCalledUnitOfWork(inCalledUnitsOfWork.get(index).clone());
        }
    }
    public int getTotalCalls()
    {
        // Because of using castor we need to recalculate this every time.
        // We don't want to force the user to update the xml and castor
        // doesn't load vectors in the expected way.
        int totalCalls = 0;
        int numberOfUOW = calledUnitsOfWork.size();
        for (int index = 0; index < numberOfUOW; index++)
        {
            totalCalls += calledUnitsOfWork.get(index).getProportionOfCalls();
        }

        return totalCalls;
    }

    public void setMicrosecondsToSleepPerCall(long inMicrosecondsToSleepPerCall)
    {
        this.microsecondsToSleepPerCall = inMicrosecondsToSleepPerCall;
    }


    public long getMicrosecondsToSleepPerCall()
    {
        return microsecondsToSleepPerCall;
    }


    /**
     * Add a new called unit of work
     * @param calledUnitOfWork Unit of work.
     */
    public void addCalledUnitOfWork(CalledUnitOfWork calledUnitOfWork)
    {
        if (calledUnitsOfWork == null) {
            calledUnitsOfWork = new ArrayList<CalledUnitOfWork>();
        }

        calledUnitsOfWork.add(calledUnitOfWork);
    }
}
