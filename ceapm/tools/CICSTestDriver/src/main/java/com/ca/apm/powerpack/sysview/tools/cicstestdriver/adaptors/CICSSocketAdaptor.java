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

package com.ca.apm.powerpack.sysview.tools.cicstestdriver.adaptors;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.ca.apm.powerpack.sysview.tools.cicstestdriver.xml.CallJobStack;
import com.ca.apm.powerpack.sysview.tools.cicstestdriver.xml.CalledTransaction;
import com.ca.apm.powerpack.sysview.tools.cicstestdriver.xml.CalledUnitOfWork;

public class CICSSocketAdaptor
    extends GenericAdaptor
{
    private static final Logger LOGGER = Logger.getLogger(CICSSocketAdaptor.class);
    private String server = "USILCA31";
    private int port = 15032;

    private Socket echoSocket = null;
    private OutputStream outputToEchoServer = null;
    private InputStream inputFromEchoServer = null;


    @Override
    public boolean initialize(CallJobStack inCallJobStack)
    {
        boolean argumentsGood = true;
        echoSocket = null;
        outputToEchoServer = null;
        inputFromEchoServer = null;

        callJobStack = inCallJobStack;
        server = inCallJobStack.getSocketServer();
        port = inCallJobStack.getSocketPort();

        try {
            echoSocket = new Socket(server, port);
        } catch (UnknownHostException e) {
            LOGGER.error("Don't know about host: " + server);
            return false;
        } catch (IOException e) {
            LOGGER.error("Couldn't get I/O for "
                               + "the connection to:" + server + ":" + port);
            return false;
        }

        try {
            outputToEchoServer = echoSocket.getOutputStream();
        } catch (IOException e) {
            LOGGER.error("Couldn't get I/O for "
                               + "the connection to:" + server + ":" + port);
            terminate();
            return false;
        }

        try {
            inputFromEchoServer = echoSocket.getInputStream();

        } catch (IOException e) {
            LOGGER.error("Couldn't get I/O for "
                               + "the connection to:" + server + ":" + port);
            terminate();
            return false;
        }

        return argumentsGood;
    }

    /**
     * Terminate the adaptor releasing resources
     */
    @Override
    public void terminate()
    {
        // Release resources in reverse order and only if allocated
        if (inputFromEchoServer != null)
        {
            try
            {
                inputFromEchoServer.close();
            }
            catch (IOException ex)
            {
                // Do nothing
            }
            inputFromEchoServer = null;
        }
        if (outputToEchoServer != null)
        {
            try
            {
                outputToEchoServer.close();
            }
            catch (IOException ex)
            {
                // Do nothing
            }
            outputToEchoServer = null;
        }
        if (echoSocket != null)
        {
            try
            {
                echoSocket.close();
            }
            catch (IOException ex)
            {
                // Do nothing
            }
            echoSocket = null;
        }
    }


    @Override
    public void RunUOW(int threadNumber, CalledUnitOfWork calledUnitOfWork)
    {
        ArrayList<CalledTransaction> calledTransactions = calledUnitOfWork.getCalledTransactions();
        LOGGER.debug("#" + threadNumber + ":"+
                     "Using CICSSocketAdaptor to run unit of work.");
        if (calledTransactions != null)
        {
            int numberOfTransactions = calledTransactions.size();
            if (numberOfTransactions == 0)
            {
                LOGGER.debug("#"+threadNumber+":"+
                                "No transaction in unit of work.");
                return;
            }

            if (numberOfTransactions == 1)
            {
                LOGGER.debug("#"+threadNumber+":"+
                                "There is only one transaction.");
                CalledTransaction calledTransaction = calledTransactions.get(0);

                LOGGER.debug("#"+threadNumber+":"+
                             "Got that called transaction and calling RunNoExtendTransaction with it.");

                RunTransaction(threadNumber, calledTransaction);
                return;
            }
            LOGGER.debug("#"+threadNumber+":"+
                            "There are multiple transactions.");

            StartUOW(threadNumber, calledUnitOfWork);

            for (int transactionIndex = 0; transactionIndex < numberOfTransactions; transactionIndex++)
            {
                // Run the next transaction (in the extended UOW)
                RunTransaction(threadNumber, calledTransactions.get(transactionIndex));
            }

            CommitUOW(threadNumber, calledUnitOfWork);
        }
        else
        {
            LOGGER.debug("#"+threadNumber+":"+
                            "Transaction list is null.");
        }
    }

    /**
     * Start a unit of work using this adaptor
     */
    private void StartUOW(int threadNumber, CalledUnitOfWork calledUnitOfWork)
    {
        LOGGER.info("Thread "+ threadNumber + " started UOW");
    }

    /**
     * Commit a unit of work using this adaptor
     */
    private void CommitUOW(int threadNumber, CalledUnitOfWork calledUnitOfWork)
    {
        LOGGER.info("Thread "+ threadNumber + " ended UOW");
    }

    /**
     * Call a transaction as part of a unit of work, or not part of one
     * calledTransaction = the transaction to call
     */
    private void RunTransaction(int threadNumber, CalledTransaction calledTransaction)
    {
        // Create a buffer of data to send
        int bufferSize = calledTransaction.getSocketBufferSize();
        byte[] outBuffer = new byte[bufferSize];
        byte[] inBuffer = new byte[bufferSize + 64];

        // Initialize it with data
        for (int bufferIndex = 0; bufferIndex < bufferSize; bufferIndex++)
        {
            outBuffer[bufferIndex] = (byte) bufferIndex;
        }

        long startTime = System.currentTimeMillis();

        try
        {
            LOGGER.info("#"+threadNumber+":"+ "Writing " + bufferSize + " bytes to socket.");
            // Write to the echo server
            outputToEchoServer.write(outBuffer);
            LOGGER.info("#"+threadNumber+":"+ "Flushing " + bufferSize + " bytes to socket.");
            outputToEchoServer.flush();
        }
        catch(IOException ex)
        {
            LOGGER.error("#"+threadNumber+":"+
                    " IOException writing  " + bufferSize + " bytes to echo server. " +
                    ex.getLocalizedMessage());
            return;
        }

        // Read from the echo server
        int totalRead = 0;
        int sizeRead = 0;

        LOGGER.info("#"+threadNumber+":"+ "Reading loop for " + bufferSize + " bytes from socket.");
        for (int len = bufferSize; len > 0; totalRead += sizeRead, len -= sizeRead)
        {
            try
            {
                LOGGER.info("#"+threadNumber+":"+ "Reading " + len + " bytes from socket.");
                sizeRead = inputFromEchoServer.read(inBuffer, totalRead, len);
                LOGGER.info("#"+threadNumber+":"+ "Read in " + sizeRead + " bytes from socket.");
                if (sizeRead == -1)
                {
                    break;
                }
            }
            catch(IOException ex)
            {
                LOGGER.error("#"+threadNumber+":"+
                        " IOException reading  " + bufferSize + " bytes back from echo server. " +
                        ex.getLocalizedMessage());
                break;
            }
        }
        LOGGER.info("#"+threadNumber+":"+ "Done reading bytes from socket.");

        long endTime = System.currentTimeMillis();

        LOGGER.info("#"+threadNumber+":"+
                    "CICS socket write/read of " + bufferSize +
                    " bytes took " + (endTime - startTime) + " ms");

        // Validate the returned bytes
        int bytesToCompare = bufferSize;
        if (bufferSize > totalRead)
        {
            bytesToCompare = totalRead;
            LOGGER.error("#"+threadNumber+":"+
                         " Only recieved " + totalRead + " bytes of " + outBuffer.length + " back from echo server.");
        }
        else if (bufferSize < totalRead)
        {
            LOGGER.error("#"+threadNumber+":"+
                    " Recieved " + (totalRead - outBuffer.length) + " more bytes than " + outBuffer.length + " back from echo server.");
        }

        // Do byte by byte compare
        for (int bufferIndex = 0; bufferIndex < bytesToCompare; bufferIndex++)
        {
            if (outBuffer[bufferIndex] != inBuffer[bufferIndex])
            {
                LOGGER.error("#"+threadNumber+":"+
                        " Echoed data mismatch at index " + bufferIndex +
                        " should be " + outBuffer[bufferIndex] + " but is " + inBuffer[bufferIndex]);
            }
        }

    }




    @Override
    public String getUsageString()
    {
        // TODO Auto-generated method stub
        return super.getUsageString();
    }

}
