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

package com.ca.apm.powerpack.sysview.tools.cicstestdriver;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.ca.apm.powerpack.sysview.tools.cicstestdriver.adaptors.GenericAdaptor;
import com.ca.apm.powerpack.sysview.tools.cicstestdriver.xml.*;

import java.util.Vector;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class CICSTestDriver extends Thread {
    private static final Logger LOGGER = Logger.getLogger(CICSTestDriver.class);

    private static final String WEB_SERVICES_ADAPTOR_CLASS_NAME =
        "com.ca.apm.powerpack.sysview.tools.cicstestdriver.adaptors.WebServicesAdaptor";
    private static final String CTG_COMMAREA_ONLY_ADAPTOR_CLASS_NAME =
        "com.ca.apm.powerpack.sysview.tools.cicstestdriver.adaptors.CTGCommareaOnlyAdaptor";
    private static final String CTG_ADAPTOR_CLASS_NAME =
        "com.ca.apm.powerpack.sysview.tools.cicstestdriver.adaptors.CTGAdaptor";
    private static final String CICS_SOCKET_ADAPTOR_CLASS_NAME =
        "com.ca.apm.powerpack.sysview.tools.cicstestdriver.adaptors.CICSSocketAdaptor";
    private Class<GenericAdaptor> WebServicesAdaptorClass = null;
    private Class<GenericAdaptor> CTGAdaptorClass = null;
    private Class<GenericAdaptor> CICSSocketsAdaptorClass = null;

    // Usage strings
    static String UsageStr = "com.ca.wily.iscopensm.config.unitest.CICSTestDriver \n"
        + "-mapfile mapfile -xmlfile xmlfile";
    static String CTGUsageStr = "";
    static String WebServicesUsageStr = "";
    static String CICSSocketsUsageStr = "";

    private static String[] arguments = null;

    // Thread control variables
    private static CountDownLatch startCountdown = null;
    private static CountDownLatch finishCountdown = null;
    private int threadNumber;


    // Instance members
    GenericAdaptor ctgAdaptor = null;
    GenericAdaptor webServicesAdaptor = null;
    GenericAdaptor cicsSocketsAdaptor = null;

    private CallJobStack callJobStack = null;
    private Vector<Integer> unusedProportions = null;
    private int totalRemaining = 0;
    private boolean useCTG;
    private boolean useWebServices;
    private boolean useCICSSockets;

    /**
     * Constructor
     *
     * @param callJobStack
     */
    public CICSTestDriver(int inThreadNumber, CallJobStack callJobStack) {
        super("CICSTestDriver");
        this.threadNumber = inThreadNumber;
        this.callJobStack = callJobStack;
    }

    public static String[] getArguments() {
        return arguments;
    }

    /**
     * Load the CTG Adaptor Class
     */
    @SuppressWarnings({"unchecked"})
    synchronized void LoadCTGAdaptorClass() {
        if (CTGAdaptorClass == null) {
            try {
                CTGAdaptorClass = (Class<GenericAdaptor>) Class.forName(CTG_ADAPTOR_CLASS_NAME);
            } catch (ClassNotFoundException ex) {
                LOGGER.error(ex);
                LOGGER.fatal("Could not load class: " + CTG_ADAPTOR_CLASS_NAME);
                System.exit(-1);
            }
        }
    }

    /**
     * Load the CTG Adaptor
     *
     * @return false if arguments are bad
     */
    synchronized boolean LoadCTGAdaptor() {
        if (ctgAdaptor == null) {
            LoadCTGAdaptorClass();
            try {
                ctgAdaptor = CTGAdaptorClass.newInstance();
                CTGUsageStr = ctgAdaptor.getUsageString();
            } catch (IllegalAccessException ex) {
                LOGGER.error(ex);
                LOGGER.fatal("Could not access class constructor of " + CTG_ADAPTOR_CLASS_NAME);
                System.exit(-1);
            } catch (InstantiationException ex) {
                LOGGER.error(ex);
                LOGGER.fatal("Could not instantiate instance of class " + CTG_ADAPTOR_CLASS_NAME);
                System.exit(-1);
            }
            return ctgAdaptor.initialize(callJobStack);
        }
        return true;
    }

    /**
     * Load the CTG Adaptor Class
     */
    @SuppressWarnings({"unchecked"})
    synchronized void LoadCTGCommareaOnlyAdaptorClass() {
        if (CTGAdaptorClass == null) {
            try {
                CTGAdaptorClass =
                    (Class<GenericAdaptor>) Class.forName(CTG_COMMAREA_ONLY_ADAPTOR_CLASS_NAME);
            } catch (ClassNotFoundException ex) {
                LOGGER.error(ex);
                LOGGER.fatal("Could not load class: " + CTG_COMMAREA_ONLY_ADAPTOR_CLASS_NAME);
                System.exit(-1);
            }
        }
    }

    /**
     * Load the CTG Adaptor
     *
     * @return false if arguments are bad
     */
    synchronized boolean LoadCTGCommareaOnlyAdaptor() {
        if (ctgAdaptor == null) {
            LoadCTGCommareaOnlyAdaptorClass();
            try {
                ctgAdaptor = CTGAdaptorClass.newInstance();
                CTGUsageStr = ctgAdaptor.getUsageString();
            } catch (IllegalAccessException ex) {
                LOGGER.error(ex);
                LOGGER.fatal("Could not access class constructor of "
                    + CTG_COMMAREA_ONLY_ADAPTOR_CLASS_NAME);
                System.exit(-1);
            } catch (InstantiationException ex) {
                LOGGER.error(ex);
                LOGGER.fatal("Could not instantiate instance of class "
                    + CTG_COMMAREA_ONLY_ADAPTOR_CLASS_NAME);
                System.exit(-1);
            }
            return ctgAdaptor.initialize(callJobStack);
        }
        return true;
    }

    /**
     * Load the Web Services Adaptor Class
     *
     */
    @SuppressWarnings({"unchecked"})
    synchronized void LoadWebServicesAdaptorClass() {
        if (WebServicesAdaptorClass == null) {
            try {
                WebServicesAdaptorClass =
                    (Class<GenericAdaptor>) Class.forName(WEB_SERVICES_ADAPTOR_CLASS_NAME);
            } catch (ClassNotFoundException ex) {
                LOGGER.error(ex);
                LOGGER.fatal("Could not load class: " + WEB_SERVICES_ADAPTOR_CLASS_NAME);
                System.exit(-1);
            }
        }
    }

    /**
     * Load the Web Services Adaptor
     *
     * @return false if arguments are bad
     */
    synchronized boolean LoadWebServicesAdaptor() {
        if (webServicesAdaptor == null) {
            LoadWebServicesAdaptorClass();
            try {
                webServicesAdaptor = (GenericAdaptor) WebServicesAdaptorClass.newInstance();
                WebServicesUsageStr = webServicesAdaptor.getUsageString();
            } catch (IllegalAccessException ex) {
                LOGGER.error(ex);
                LOGGER.fatal("Could not access class constructor of "
                    + WEB_SERVICES_ADAPTOR_CLASS_NAME);
                System.exit(-1);
            } catch (InstantiationException ex) {
                LOGGER.error(ex);
                LOGGER.fatal("Could not instantiate instance of class "
                    + WEB_SERVICES_ADAPTOR_CLASS_NAME);
                System.exit(-1);
            }
            return webServicesAdaptor.initialize(callJobStack);
        }
        return true;
    }

    /**
     * Load the CTG Adaptor Class
     */
    @SuppressWarnings({"unchecked"})
    synchronized void LoadCICSSocketsAdaptorClass() {
        if (CICSSocketsAdaptorClass == null) {
            try {
                CICSSocketsAdaptorClass =
                    (Class<GenericAdaptor>) Class.forName(CICS_SOCKET_ADAPTOR_CLASS_NAME);
            } catch (ClassNotFoundException ex) {
                LOGGER.error(ex);
                LOGGER.fatal("Could not load class: " + CICS_SOCKET_ADAPTOR_CLASS_NAME);
                System.exit(-1);
            }
        }
    }

    /**
     * Load the CTG Adaptor
     *
     * @return false if arguments are bad
     */
    synchronized boolean LoadCICSSocketsAdaptor() {
        if (cicsSocketsAdaptor == null) {
            LoadCICSSocketsAdaptorClass();
            try {
                cicsSocketsAdaptor = CICSSocketsAdaptorClass.newInstance();
                CICSSocketsUsageStr = cicsSocketsAdaptor.getUsageString();
            } catch (IllegalAccessException ex) {
                LOGGER.error(ex);
                LOGGER.fatal("Could not access class constructor of "
                    + CICS_SOCKET_ADAPTOR_CLASS_NAME);
                System.exit(-1);
            } catch (InstantiationException ex) {
                LOGGER.error(ex);
                LOGGER.fatal("Could not instantiate instance of class "
                    + CICS_SOCKET_ADAPTOR_CLASS_NAME);
                System.exit(-1);
            }
            return cicsSocketsAdaptor.initialize(callJobStack);
        }
        return true;
    }

    public void run() {
        // Wait for signal from main thread to start
        try {
            LOGGER.info("#" + threadNumber + ":" + "CICSTestDriver thread #" + threadNumber
                + " awaiting signal to start.");
            startCountdown.await();
            LOGGER.info("#" + threadNumber + ":" + "Thread #" + threadNumber + " starting.");
        } catch (Exception ex) {
            // Ignore all exceptions
        }

        runJobs();

        terminate();

        // Tell that we have finished
        LOGGER.info("#" + threadNumber + ":" + "Thread #" + threadNumber + " finished.");


        // Tell main thread this thread is finished
        // by decrementing the finish count down
        finishCountdown.countDown();
    }

    /**
     * Run all the jobs
     */
    public void runJobs() {
        ArrayList<CallDistribution> calledDistributions = callJobStack.getCalledDistributions();
        if (calledDistributions == null) {
            LOGGER.warn("#" + threadNumber + ":" + "No units of work to run in job stack named "
                + callJobStack.getName() + ".");
            return;
        }

        // Indicate what job stack we are running
        LOGGER.info("#" + threadNumber + ":" + "Running Job:" + callJobStack.getName());

        // Run all the distributions
        if (calledDistributions != null) {
            int numberOfDistributions = calledDistributions.size();
            for (int index = 0; index < numberOfDistributions; index++) {
                CallDistribution callDistribution = calledDistributions.get(index);

                // Run the distribution
                runDistribution(callDistribution);
            }
        }
    }

    /**
     * Run the distribution
     *
     * @param callDistribution
     */
    public void runDistribution(CallDistribution callDistribution) {
        ArrayList<CalledUnitOfWork> calledUnitsOfWork = callDistribution.getCalledUnitsOfWork();

        if (calledUnitsOfWork == null) {
            LOGGER.warn("#" + threadNumber + ":" + "No units of work to run in distribution named "
                + callDistribution.getName() + ".");
            return;
        }

        // Fetch call methods used
        useCTG = callDistribution.isUseCTG();
        useWebServices = callDistribution.isUseWebServices();
        useCICSSockets = callDistribution.isUseCICSSockets();

        // Indicate what distribution we are running
        LOGGER.info("#" + threadNumber + ":" + "Running Distribution:" + callDistribution.getName()
            + (useCICSSockets ? " using CICS Sockets " : "") + (useCTG ? " using CTG " : "")
            + (useWebServices ? " using Web Services " : ""));


        // Get the start time and calculate the finish time
        long startTime = System.currentTimeMillis();
        long minutesToRun = callDistribution.getMinutesToRun();
        long finishTime = startTime + (minutesToRun * 60 * 1000);

        // Get the current time
        long currentTime = startTime;

        // Initialize the random number generator according to the distribution
        // If the distribution has no seed value then use the startime as seed
        long seed = startTime;
        Long randomSeed = callDistribution.getRandomSeed();
        if (randomSeed != null) seed = randomSeed.longValue();
        Random random = new Random(seed);

        // Figure out how long to sleep per transaction and how many transactions
        // after which one millisecond of sleep is required.
        long sleepMicroseconds = callDistribution.getMicrosecondsToSleepPerCall();
        long sleepMilliseconds = sleepMicroseconds / 1000;
        long remainingMicrosecondsOfSleep = sleepMicroseconds % 1000;
        float numberOfExtraTransactionsPerMillisecondSleep =
            (remainingMicrosecondsOfSleep == 0) ? 0 : (1000 / remainingMicrosecondsOfSleep);
        float remainingExtraTransactionsNeededToSleep =
            numberOfExtraTransactionsPerMillisecondSleep;
        int extraTransactionCount = 0;


        // Allocate variable to hold maximum number of runs to process
        // Initialize to unlimited so that the finish time is what terminates
        // the processing loop.
        long maxRunCount = Long.MAX_VALUE;

        // If minutes to run is zero that means to run a single full distribution
        // no matter how long it takes. So set finish time to infinity, but the
        // maximum runs to 1.
        if (minutesToRun == 0) {
            finishTime = Long.MAX_VALUE;
            maxRunCount = 1;
        }

        // Processing loop
        for (int distributionsRunCount = 0; ((currentTime < finishTime) && (distributionsRunCount < maxRunCount)); currentTime =
            System.currentTimeMillis(), distributionsRunCount++) {
            // Get the total remaining calls to be made to use up the distribution
            totalRemaining = callDistribution.getTotalCalls();

            // Fill up the unused proportions table
            int numberOfUnitsOfWork = calledUnitsOfWork.size();
            unusedProportions = new Vector<Integer>(numberOfUnitsOfWork);
            for (int index = 0; index < numberOfUnitsOfWork; index++) {

                unusedProportions.add(calledUnitsOfWork.get(index).getProportionOfCalls());
            }

            // Loop to run the distribution once (breaking if we over run alloted time
            for (; (totalRemaining > 0) && (currentTime < finishTime); totalRemaining--, currentTime =
                System.currentTimeMillis(), extraTransactionCount++) {
                // Sleep for amount needed per transaction
                if (sleepMilliseconds > 0) {
                    try {
                        Thread.sleep(sleepMilliseconds);
                    } catch (InterruptedException e) {}
                }

                // Sleep for one millisecond every so often to get
                if (numberOfExtraTransactionsPerMillisecondSleep > 0) {
                    if ((extraTransactionCount + 1) > remainingExtraTransactionsNeededToSleep) {
                        remainingExtraTransactionsNeededToSleep +=
                            (numberOfExtraTransactionsPerMillisecondSleep - extraTransactionCount);
                        extraTransactionCount = 0;
                        try {
                            // Sleep for one millisecond
                            Thread.sleep(1);
                        } catch (InterruptedException e) {}
                    }
                }

                int nextPickedFromRemaining = random.nextInt(totalRemaining);

                // Determine which UOW to run
                for (int uowIndex = 0; uowIndex < numberOfUnitsOfWork; uowIndex++) {
                    int currentProportion = unusedProportions.get(uowIndex);

                    // If the next picked is in this portion then
                    // use this UOW
                    if (nextPickedFromRemaining < currentProportion) {
                        // Run the picked UOW
                        RunUOW(calledUnitsOfWork.get(uowIndex));

                        // Used up one item so reset unused proportion
                        unusedProportions.set(uowIndex, new Integer(currentProportion - 1));
                    }
                    // Otherwise move on to the next portion by deducting the current
                    // proportion from the picked one.
                    else {
                        nextPickedFromRemaining -= currentProportion;
                    }
                }
            }
        }
    }

    /**
     * Terminate
     */
    void terminate() {
        if (useCTG && (ctgAdaptor != null)) {
            ctgAdaptor.terminate();
        }
        if (useWebServices && (webServicesAdaptor != null)) {
            webServicesAdaptor.terminate();
        }
        if (useCICSSockets && (cicsSocketsAdaptor != null)) {
            cicsSocketsAdaptor.terminate();
        }
    }

    /**
     * Run a particular UOW
     *
     * @param calledUnitOfWork
     */
    void RunUOW(CalledUnitOfWork calledUnitOfWork) {
        int repeat = calledUnitOfWork.getTransactionRepeat();
        for (int count = 0; count < repeat; count++) {
            if (useCTG) {
                RunUOWUsingCTG(calledUnitOfWork);
            }
            if (useWebServices) {
                RunUOWUsingWebServices(calledUnitOfWork);
            }
            if (useCICSSockets) {
                RunUOWUsingCICSSockets(calledUnitOfWork);
            }
        }
    }

    /**
     * Run a particular UOW
     *
     * @param calledUnitOfWork
     */
    void RunUOWUsingCTG(CalledUnitOfWork calledUnitOfWork) {
        // If the CTG adaptor is not loaded then
        if (ctgAdaptor == null) {
            // Load the correct version of the
            boolean argsGood =
                (callJobStack.isUseChannel()) ? LoadCTGAdaptor() : LoadCTGCommareaOnlyAdaptor();
            if (!argsGood) {
                printUsage();
                System.exit(-1);
            }
        }

        ctgAdaptor.RunUOW(threadNumber, calledUnitOfWork);
    }

    /**
     * Run a particular UOW
     *
     * @param calledUnitOfWork
     */
    void RunUOWUsingWebServices(CalledUnitOfWork calledUnitOfWork) {
        if (webServicesAdaptor == null) {
            boolean argsGood = LoadWebServicesAdaptor();
            if (!argsGood) {
                printUsage();
                System.exit(-1);
            }

        }
        webServicesAdaptor.RunUOW(threadNumber, calledUnitOfWork);
    }

    /**
     * Run a particular UOW
     *
     * @param calledUnitOfWork
     */
    void RunUOWUsingCICSSockets(CalledUnitOfWork calledUnitOfWork) {
        if (cicsSocketsAdaptor == null) {
            boolean argsGood = LoadCICSSocketsAdaptor();
            if (!argsGood) {
                printUsage();
                System.exit(-1);
            }
        }

        cicsSocketsAdaptor.RunUOW(threadNumber, calledUnitOfWork);
    }

    /**
     * Entry point for the CICSTestDriver
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String xmlFile = ".\\CICSTestProperties.xml";
        String mappingFile = ".\\mapping.xml";

        // Configure log4j
        PropertyConfigurator.configure("log4j.properties");

        // Save the arguments to the static for passing to adaptors
        arguments = args;

        boolean defmapfile = true;
        boolean defxmlfile = true;
        int bookendSleep = 20;

        System.out.println("starting CICSTestDriver.");

        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-mapfile")) {
                mappingFile = args[i + 1];
                defmapfile = false;
            } else if (args[i].equalsIgnoreCase("-xmlfile")) {
                xmlFile = args[i + 1];
                defxmlfile = false;
            } else if (args[i].equalsIgnoreCase("-nosleep")) {
                bookendSleep = 0;
            } else if (args[i].equalsIgnoreCase("-?") || args[i].equalsIgnoreCase("?")
                || args[i].equalsIgnoreCase("/?") || args[i].equalsIgnoreCase("-help")
                || args[i].equalsIgnoreCase("help")) {
                new CICSTestDriver(0, null).printUsage();
                System.exit(0);
            }

        }

        if (defmapfile)
            System.out.println("Using default map file: " + mappingFile);
        else
            System.out.println("Using given map file: " + mappingFile);

        if (defxmlfile)
            System.out.println("Using default xml file:" + xmlFile);
        else
            System.out.println("Using given xml file: " + xmlFile);

        if (bookendSleep > 0) {
            final String initMessage =
                "Sleeping for " + bookendSleep + " seconds to give you chance to trace!";
            System.out.println(initMessage);
            LOGGER.info(initMessage);

            long startTime = System.currentTimeMillis();
            Thread.sleep(bookendSleep * 1000);
            long currTime = System.currentTimeMillis();
            long sleepTime = (currTime - startTime) / 1000;

            final String termMessage = "Done sleeping for " + sleepTime + " seconds.";
            LOGGER.info(termMessage);
            System.out.println(termMessage);
        }

        // new ReadXmlFile(xmlFile, mappingFile);
        CallJobStack callJobStack = CallJobStack.getInstance(mappingFile, xmlFile);

        // Determine the number of threads to run
        int numberOfThreadsToRun = callJobStack.getNumberOfThreadsToRun();

        // Create the start countdown latch that will prevent the threads
        // from starting until triggered by this main thread.
        startCountdown = new CountDownLatch(1);

        // Create the finish countdown latch that will block the main thread
        // until all the threads count down their completion.
        finishCountdown = new CountDownLatch(numberOfThreadsToRun);

        // Allocate an array to contain a CICSTestDriver object for each thread
        CICSTestDriver[] drivers = new CICSTestDriver[numberOfThreadsToRun];

        // Allocate all the threads
        for (int driverIndex = 0; driverIndex < drivers.length; driverIndex++) {
            drivers[driverIndex] = new CICSTestDriver(driverIndex, callJobStack);
        }


        // Start all the threads
        LOGGER.info("Starting " + numberOfThreadsToRun + " threads to run the job call stack.");
        for (int driverIndex = 0; driverIndex < drivers.length; driverIndex++) {
            CICSTestDriver cicsTestDriver = drivers[driverIndex];
            cicsTestDriver.start();
        }

        // Trigger the threads to start
        startCountdown.countDown();

        // Wait for all threads to end
        LOGGER.info("Awaiting completion of the " + numberOfThreadsToRun + " threads.");
        try {
            finishCountdown.await();
        } catch (Exception ex) {
            // Catch all exceptions
        }

        // Sleep again to give trace a chance to be shipped
        if (bookendSleep > 0) {
            LOGGER.info("All threads terminated. Sleeping " + bookendSleep
                + " seconds to give traces a chance to be delivered.");
            Thread.sleep(bookendSleep * 1000);
        }

        System.out.println("Done!");
    }

    /**
     * Print the usage of the program
     */
    public void printUsage() {
        // Load the adaptors to get their usage strings
        LoadCTGCommareaOnlyAdaptor();
        LoadWebServicesAdaptor();
        LoadCICSSocketsAdaptor();

        System.out.println(UsageStr + " " + CTGUsageStr + " " + WebServicesUsageStr + " "
            + CICSSocketsUsageStr);
    }
}
