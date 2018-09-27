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

public class WriteInitialCICSTestXMLFile
{

    WriteInitialCICSTestXMLFile(String propertiesFile, String mappingFile) throws Exception
    {
        // Create a called transaction
        CalledTransaction calledTransaction = new CalledTransaction("SLP0");
        calledTransaction.addParameter("500");

        // Created a unit of work
        CalledUnitOfWork calledUnitOfWork = new CalledUnitOfWork(100);
        calledUnitOfWork.addCalledTransaction(calledTransaction.clone());
        calledUnitOfWork.addCalledTransaction(calledTransaction.clone());

        // Create a distribution
        CallDistribution callDistribution = new CallDistribution("DistributionName", 2, true, false, false, 0, 0);
        callDistribution.addCalledUnitOfWork(calledUnitOfWork.clone());
        callDistribution.addCalledUnitOfWork(calledUnitOfWork.clone());

        // Root of XML file
        CallJobStack callJobStack = new CallJobStack("JobStackTestRunName");
        CallDistribution callDistribution1 = callDistribution.clone();
        callDistribution1.setName("DistributionNameA");
        callDistribution1.setRandomSeed(0L);
        callJobStack.addCallDistribution(callDistribution1);
        CallDistribution callDistribution2 = callDistribution.clone();
        callDistribution1.setName("DistributionNameB");
        callJobStack.addCallDistribution(callDistribution2);

        // Create the XML parser
        XMLParser xmlparser = new XMLParser(mappingFile);

        // Generate the configuration file
        xmlparser.setConfig(callJobStack, propertiesFile);
    }


    public static void main(String[] args) throws Exception
    {
        String xmlFile = "c:\\tmp\\CICSTestProperties.xml";
        String mappingFile = "c:\\tmp\\mapping.xml";
        String rwoption = "rw";

        boolean defmapfile = true;
        boolean defxmlfile = true;
        boolean defrw = true;

        System.out.println("starting WriteInitialCICSTestXMLFile.");

        if (args.length == 0)
        {
            printusage();
            System.exit(0);
        }

        for (int i = 0; i < args.length; i++)
        {
            if (args[i].equalsIgnoreCase("-mapfile"))
            {
                mappingFile = args[i + 1];
                defmapfile = false;
            }
            else if (args[i].equalsIgnoreCase("-xmlfile"))
            {
                xmlFile = args[i + 1];
                defxmlfile = false;
            }
            else if (args[i].equalsIgnoreCase("-rw"))
            {
                rwoption = args[i + 1];
                defrw = false;
            }
            else if (args[i].equalsIgnoreCase("-?") || args[i].equalsIgnoreCase("?") || args[i].equalsIgnoreCase("/?")
                    || args[i].equalsIgnoreCase("-help") || args[i].equalsIgnoreCase("help"))
            {
                printusage();
                System.exit(0);
            }

        }

        if (!((rwoption.equalsIgnoreCase("read")) || (rwoption.equalsIgnoreCase("write")) || (rwoption
                .equalsIgnoreCase("rw"))))
        {
            System.out.println("-rw innvalid. Valid values : [read|write|rw]");
            System.exit(0);
        }

        if (defrw)
        {
            System.out.println("Using default Read-Write option: write and read");
        }
        else
        {
            if (rwoption.equalsIgnoreCase("read")) {
                System.out.println("Read-Write option: read only");
            } else if (rwoption.equalsIgnoreCase("write")) {
                System.out.println("Read-Write option: write only");
            } else {
                System.out.println("Read-Write option: write and read");
            }
        }

        if (defmapfile) {
            System.out.println("Using default map file: " + mappingFile);
        } else {
            System.out.println("Using given map file: " + mappingFile);
        }

        if (defxmlfile) {
            System.out.println("Using default xml file:" + xmlFile);
        } else {
            System.out.println("Using given xml file: " + xmlFile);
        }

        if ((rwoption.equalsIgnoreCase("write") || rwoption.equalsIgnoreCase("rw")))
        {
            new WriteInitialCICSTestXMLFile(xmlFile, mappingFile);
        }
        System.out.println("Done!");
    }


    public static void printusage()
    {
        String usage = "com.ca.wily.iscopensm.config.unitest.Runxmltest \n"
                + "-mapfile mapfile -xmlfile xmlfile -rw [read|write|rw]";
        System.out.println(usage);
    }

}
