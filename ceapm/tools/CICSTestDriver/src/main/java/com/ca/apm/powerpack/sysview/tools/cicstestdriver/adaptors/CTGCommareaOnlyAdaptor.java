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

package com.ca.apm.powerpack.sysview.tools.cicstestdriver.adaptors;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.ca.apm.powerpack.sysview.tools.cicstestdriver.xml.CallJobStack;
import com.ca.apm.powerpack.sysview.tools.cicstestdriver.xml.CalledTransaction;
import com.ca.apm.powerpack.sysview.tools.cicstestdriver.xml.CalledUnitOfWork;
import com.ibm.ctg.client.*;

public class CTGCommareaOnlyAdaptor extends GenericAdaptor
{
	private static final Logger LOGGER = Logger.getLogger(CTGCommareaOnlyAdaptor.class);
	private ECIRequest eciRequest = null;
	private JavaGateway javaGatewayObject = null;
	
	private boolean bDataConv = true;
	private String strDataConv = "IBM037";
	
    
	/**
	 * Initialize the adaptor with the command line arguments and
	 * returns true if successful.   If false then the arguments
	 * are invalid.
	 */
    @Override
	public boolean initialize(CallJobStack inCallJobStack)
	{
		boolean argumentsGood = true;
		
		String strClientSecurity = null;
		String strServerSecurity = null;

		callJobStack = inCallJobStack;
		
		LOGGER.info("Connecting to CTG server gate: '" + inCallJobStack.getJGate() +
		            "' port: '" + inCallJobStack.getJGatePort() +
		            "' server: '" + inCallJobStack.getServerName() + "' " +
		            (inCallJobStack.isUseCommarea() ? "useCommarea " : "useNothing ")+ 
		            ((inCallJobStack.getProgramDataStr() != null) ? (" ProgramDataStr=" + inCallJobStack.getProgramDataStr() + " "): " ") +
		            (inCallJobStack.isUseDynamicDecoration() ? "useDynamicDecoration" : ""));
		
		try
		{
    		javaGatewayObject = new JavaGateway(inCallJobStack.getJGate(),
    		                                    inCallJobStack.getJGatePort(),
                    strClientSecurity,
                    strServerSecurity);
		}
		catch(java.io.IOException ex)
		{
            LOGGER.fatal("javaGatewayObject IOException: " + ex.getLocalizedMessage());
            return false;
		}
		catch(Exception ex)
		{
			javaGatewayObject = null;
			LOGGER.fatal("javaGatewayObject could not be allocated because: " + ex.getLocalizedMessage());
			return false;
		}
		
		return argumentsGood;
	};
	
	/** 
	 * Terminate the adaptor releasing resources
	 */
    @Override
	public void terminate()
	{
	    if (javaGatewayObject != null)
	    {
	        try
	        {
	            javaGatewayObject.close();
	        }
	        catch (IOException ex)
	        {
	            // Do nothing
	        }
	        javaGatewayObject = null;
	    }
	}
	
	/**
	 * Return the usage string for the command line arguments that this adaptor uses
	 */
    @Override
	public String getUsageString() 
	{ 
		return "-JGATE <CTG Server> -JGATEPORT <CTG PORT> -SERVER <IPIC Server>";
	};
		
	/** 
	 * Run a unit of work using this adaptor
	 * @param calledUnitOfWork = the unit of work to run
	 */
    @Override
	public void RunUOW(int threadNumber, CalledUnitOfWork calledUnitOfWork) 
	{
		ArrayList<CalledTransaction> calledTransactions = calledUnitOfWork.getCalledTransactions();
		LOGGER.debug("#"+threadNumber+":"+ 
                     "Using CTGAdaptor to run unit of work.");
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
				
				RunNoExtendTransaction(threadNumber, calledTransaction);
				return;
			}
			LOGGER.debug("#"+threadNumber+":"+ 
	                        "There are multiple transactions.");
			
			StartUOW(threadNumber, calledUnitOfWork);
			
			for (int transactionIndex = 0; transactionIndex < numberOfTransactions; transactionIndex++)
			{
				// Run the next transaction (in the extended UOW)
				RunExtendedTransaction(threadNumber, calledTransactions.get(transactionIndex));
			}
			
			CommitUOW(threadNumber, calledUnitOfWork);
		}
		else
		{
			LOGGER.debug("#"+threadNumber+":"+ 
	                        "Transaction list is null.");
		}
		
	};
	
	/** 
	 * Start a unit of work using this adaptor
	 */
	private void StartUOW(int threadNumber, CalledUnitOfWork calledUnitOfWork)
	{
        try
        {
            if (calledUnitOfWork.isTestXA())
            {
                XARequest xaRequest = new XARequest(XARequest.START_NEW);
                if (calledUnitOfWork.getUserID() != null)
                {
                    xaRequest.setUsername(calledUnitOfWork.getUserID());
                    xaRequest.setPassword(calledUnitOfWork.getPassword());
                }
                javaGatewayObject.flow(xaRequest);
            }
        }
        catch(Exception ex)
        {
        }
        
		try
		{
			eciRequest = new ECIRequest(callJobStack.getServerName(), // CICS Server
			                            callJobStack.getUserID(),     // UserId, null for none
			                            callJobStack.getPassword(),   // Password, null for none
                             null,          // Program name
                             (byte[])null,    // Commarea
			                 ECIRequest.ECI_EXTENDED,
			                 ECIRequest.ECI_LUW_NEW);
		}
		catch(Exception ex)
		{
			eciRequest = null;
		}
	}
	
	/** 
	 * Commit a unit of work using this adaptor
	 */
	private void CommitUOW(int threadNumber, CalledUnitOfWork calledUnitOfWork)
	{
		try
		{
			if (eciRequest != null)
			{
				eciRequest.Cics_Rc = 0;
	            eciRequest.Extend_Mode = ECIRequest.ECI_COMMIT;
	
	            LOGGER.debug("#"+threadNumber+":"+ 
	                         "Commiting UOW");
	            javaGatewayObject.flow(eciRequest);
	            LOGGER.debug("#"+threadNumber+":"+ 
	                         "Done committing UOW.");
          
			}
		}
		catch(Exception ex)
		{
		}

        try
        {
            if (calledUnitOfWork.isTestXA())
            {
                XARequest xaRequest = new XARequest(XARequest.COMMIT_ONE_PHASE);
                if (calledUnitOfWork.getUserID() != null)
                {
                    xaRequest.setUsername(calledUnitOfWork.getUserID());
                    xaRequest.setPassword(calledUnitOfWork.getPassword());
                }
                javaGatewayObject.flow(xaRequest);
            }
        }
        catch(Exception ex)
        {
        }
	}
	
	/** 
	 * Call a transaction as part of a unit of work
	 * calledTransaction = the transaction to call
	 */
	private void RunExtendedTransaction(int threadNumber, CalledTransaction calledTransaction)
	{
	    byte[] commareaBytes = null;
	    
        // Saved commarea lenght that was sent in case of using commarea string
        // If we generated the commarea based on config xml file then this remains -1
        int sentCommareaLen = -1;  // A -1 indicates we generated the commarea
        
        try
        {
        	eciRequest.Cics_Rc = 0;
        	eciRequest.Extend_Mode = ECIRequest.ECI_EXTENDED;
        }
		catch(Exception ex)
		{
	       	LOGGER.error("#"+threadNumber+":"+ 
	                        "Exception using eciRequest object " + ex);
		}

        try
        {
        	eciRequest.Program = calledTransaction.getTransactionID();
        }
		catch(Exception ex)
		{
	       	LOGGER.error("#"+threadNumber+":"+ 
	                        "Exception getting transaction id " + ex);
		}
        
		try
		{
	        if (callJobStack.isUseCommarea())
	        {
	            LOGGER.debug("#"+threadNumber+":"+ 
	                         "Transaction defined with buflen = " + calledTransaction.getBufferLength()+
	                         " commArea len = " + calledTransaction.getCommAreaLength() +
	                         " out len = " + calledTransaction.getOutBoundLength() +
	                         " in len = " + calledTransaction.getInBoundLength());
	            
	            String commareaStr = calledTransaction.getCommAreaData() != null
					? calledTransaction.getCommAreaData()
					: callJobStack.getProgramDataStr();
	        	if ((commareaStr != null) && (callJobStack.isUseDynamicDecoration() == true))
	        	{
	        	    LOGGER.debug("#"+threadNumber+":"+ 
	                             "CTG Calling extended program " + eciRequest.Program + " using commareaStr=" + commareaStr);
	        		commareaBytes = commareaStr.getBytes();
	        		eciRequest.Commarea = commareaBytes;
                    sentCommareaLen = commareaBytes.length;
                    eciRequest.Commarea_Length = sentCommareaLen;
                    eciRequest.setCommareaOutboundLength(true);
                    eciRequest.setCommareaOutboundLength(sentCommareaLen);
                    eciRequest.setCommareaInboundLength(sentCommareaLen);
	        	}
	        	else
	        	{
                    LOGGER.debug("#"+threadNumber+":"+ 
                                 "CTG Calling extended program " + eciRequest.Program + " using calledTransaction");
                    commareaBytes = GenerateCommarea(threadNumber, calledTransaction);
	        	}
	        	
	        }
		}
		catch(Exception ex)
		{
	       	LOGGER.error("#"+threadNumber+":"+ 
	                        "Exception constructing commarea " + ex);
		}
		
		try
		{
	    	long startTime = System.currentTimeMillis();
	    	
	    	javaGatewayObject.flow(eciRequest);
	    	
	    	long endTime = System.currentTimeMillis();
	    	
	    	LOGGER.info("#"+threadNumber+":"+ 
                        "CTG calling program " + calledTransaction.toString() +
	    			" took " + (endTime - startTime) + " ms");

			// Validate the returned commarea
			validateReturnedCommarea(threadNumber, commareaBytes, sentCommareaLen, calledTransaction);
		}
		catch(Exception ex)
		{
	       	LOGGER.error("#"+threadNumber+":"+ 
	                        "Exception doing flow " + ex);
	       	ex.printStackTrace();
	    }
	}
	
	/** 
	 * Call a transaction in non-extended mode
	 * calledTransaction = the transaction to call
	 */
	private void RunNoExtendTransaction(int threadNumber, CalledTransaction calledTransaction)
	{
        byte[] commareaBytes = null;
	    
	    LOGGER.debug("#"+threadNumber+":"+ 
                     "Entered RunNoExtendTransaction.");

	    // Saved commarea length that was sent in case of using commarea string
	    // If we generated the commarea based on config xml file then this remains -1
	    int sentCommareaLen = -1;  // A -1 indicates we generated the commarea
	    
        try
        {
            String programName = null;
            try
            {
                programName = calledTransaction.getTransactionID();
            }
            catch(Exception ex)
            {
                LOGGER.error("#"+threadNumber+":"+ 
                             "Exception getting transaction id " + ex);
                return;
            }
            
            LOGGER.debug("#"+threadNumber+":"+ 
                         "Allocating ECIRequest object");
            
            eciRequest = new ECIRequest(callJobStack.getServerName(), // CICS Server
                                        callJobStack.getUserID(),     // UserId, null for none
                                        callJobStack.getPassword(),   // Password, null for none
                     programName,          // Program name
                     commareaBytes,    // Commarea
                     ECIRequest.ECI_NO_EXTEND,
                     ECIRequest.ECI_LUW_NEW);

            LOGGER.debug("#"+threadNumber+":"+ 
                         "Done allocating ECIRequest object");
            try
			{
		        if (callJobStack.isUseCommarea())
		        {
		            LOGGER.debug("#"+threadNumber+":"+ 
		                         "Transaction defined with buflen = " + calledTransaction.getBufferLength()+
		                         " commArea len = " + calledTransaction.getCommAreaLength() +
		                         " out len = " + calledTransaction.getOutBoundLength() +
		                         " in len = " + calledTransaction.getInBoundLength());

                    String commareaStr = calledTransaction.getCommAreaData() != null
                        ? calledTransaction.getCommAreaData()
                        : callJobStack.getProgramDataStr();
	                if ((commareaStr != null) && (callJobStack.isUseDynamicDecoration() == true))
		        	{
	                    LOGGER.debug("#"+threadNumber+":"+ 
	                                 "CTG Calling program " + eciRequest.Program + " using commareaStr=" + commareaStr);

	                    commareaBytes = commareaStr.getBytes();
	                    eciRequest.Commarea = commareaBytes;
	                    sentCommareaLen = commareaBytes.length;
	                    eciRequest.Commarea_Length = sentCommareaLen;
	                    eciRequest.setCommareaOutboundLength(true);
	                    eciRequest.setCommareaOutboundLength(sentCommareaLen);
	                    eciRequest.setCommareaInboundLength(sentCommareaLen);
		        	}
		        	else
		        	{
	                    LOGGER.debug("#"+threadNumber+":"+ 
	                                 "CTG Calling program " + eciRequest.Program + " using calledTransaction");
	                    
	                    commareaBytes = GenerateCommarea(threadNumber, calledTransaction);
		        	}
		        	
		        }
			}
			catch(Exception ex)
			{
		       	LOGGER.error("#"+threadNumber+":"+ 
		                        "Exception constructing commarea " + ex);
		       	return;
			}
        }
		catch(Exception ex)
		{
	       	LOGGER.error("#"+threadNumber+":"+ 
	                        "Exception using eciRequest object " + ex);
	       	return;
		}
		
     

		try
		{
        	eciRequest.Cics_Rc = 0;
	    	long startTime = System.currentTimeMillis();
	    	
	    	javaGatewayObject.flow(eciRequest);
	    	
	    	long endTime = System.currentTimeMillis();
	    	
	    	LOGGER.info("#"+threadNumber+":"+ 
                        "CTG calling program " + calledTransaction.toString() +
	    			" took " + (endTime - startTime) + " ms");

            // Validate the returned commarea
            validateReturnedCommarea(threadNumber, commareaBytes, sentCommareaLen, calledTransaction);

		}
		catch(Exception ex)
		{
	       	LOGGER.error("#"+threadNumber+":"+ 
	                        "Exception doing flow " + ex);
            ex.printStackTrace();
		}

	}
   
	/**
	 * Validate the information in the returned commarea based on the called transaction
	 * @param sentCommareaLen = Length of the commarea sent if the called transaction info was not used.
	 *                          This will be -1 if calledTransaction information was used instead
	 * @param calledTransaction
	 */
    private void validateReturnedCommarea(int threadNumber, byte[] commareaBytes, int sentCommareaLen, CalledTransaction calledTransaction)
    {
        int correctBufLen = sentCommareaLen;
        int correctCommareaLen = sentCommareaLen;
        int correctOutboundLen = sentCommareaLen;
        int correctInboundLen = sentCommareaLen;
        byte[] commarea = eciRequest.Commarea;
 
        try
        {
	        // Don't validate if not using commarea
	        if (!callJobStack.isUseCommarea())
	        {
	            return;
	        }
	        
	        // If sent length is set to -1 that indicates we used the callTransaction info
	        // instead of the sent length
	        if (sentCommareaLen < 0)
	        {
	            correctBufLen = calledTransaction.getBufferLength();
	            correctCommareaLen = calledTransaction.getCommAreaLength();
	            correctOutboundLen = calledTransaction.getOutBoundLength();
	            correctInboundLen = calledTransaction.getInBoundLength();
	        }
	
	        // If returned buffer is smaller than original then error
	        if (commarea != commareaBytes)
	        {
	            LOGGER.warn("#"+threadNumber+":"+ 
	                         "Recieved back a different commarea buffer than was sent.  This is not a problem if you set ceapm.commarea.restore.bytes=false");
	        }
	        
	        // If returned buffer is smaller than original then error
	        if (commarea.length < correctBufLen)
	        {
	            LOGGER.error("#"+threadNumber+":"+ 
	                         "Recieved back commarea buffer of length " + commarea.length + " smaller than original of " + correctBufLen);
	        }
	        
	        // If the commarea length has changed that is wrong
	        if (correctCommareaLen != eciRequest.Commarea_Length)
	        {
	            LOGGER.error("#"+threadNumber+":"+ 
	                         "Recieved back commarea length of " + eciRequest.Commarea_Length + " smaller than original of " + correctCommareaLen);
	        }
	        
	        // Check if out bound length is still correct
	        // If no out bound length was originally defined then
	        if (correctOutboundLen < 0)
	        {
	            // If an out bound lenght is now defined then that is wrong
	            if (eciRequest.isCommareaOutboundLength())
	            {
	                LOGGER.error("#"+threadNumber+":"+ 
	                             "Recieved back an outbound length of " + eciRequest.getCommareaOutboundLength() + " when originally there was none.");
	            }
	        }
	        // Otherwise the out bound length was defined so
	        else
	        {
	            // If it is no longer defined then error
	            if (!eciRequest.isCommareaOutboundLength())
	            {
	                LOGGER.error("#"+threadNumber+":"+ 
	                             "Recieved back no outbound length when originally it was " +  correctOutboundLen);
	            }
	            // Otherwise it is defined so check if correct
	            else
	            {
	                // If returned out bound length is different then
	                if (eciRequest.getCommareaOutboundLength() != correctOutboundLen)
	                {
	                    LOGGER.error("#"+threadNumber+":"+ 
	                                 "Recieved back outbound length of " +  eciRequest.getCommareaOutboundLength() +
	                                 " instead of " + correctOutboundLen);
	                }
	            }
	        }
	
	        // Check if in bound length is still correct
	        // If no in bound length was originally defined then
	        if (correctInboundLen < 0)
	        {
	            // If an in bound length is now defined then it must match
	            // the length of the commarea
	            if (eciRequest.isCommareaInboundLength())
	            {
	                if (eciRequest.getCommareaInboundLength() != correctCommareaLen)
	                    LOGGER.error("#"+threadNumber+":"+ 
	                                 "Recieved back an inbound length of " + eciRequest.getCommareaInboundLength() + " when originally there was none. It should equal the commarea length of " + correctCommareaLen);
	            }
	        }
	        // Otherwise the in bound length was defined so
	        else
	        {
	            // If it is no longer defined then error
	            if (!eciRequest.isCommareaInboundLength())
	            {
	                LOGGER.error("#"+threadNumber+":"+ 
	                             "Recieved back no inbound length when originally it was " +  correctInboundLen);
	            }
	            // Otherwise it is defined so check if correct
	            else
	            {
	                // If returned in bound length is greater then error
	                if (eciRequest.getCommareaInboundLength() > correctInboundLen)
	                {
	                    LOGGER.error("#"+threadNumber+":"+ 
	                                 "Recieved back inbound length of " +  eciRequest.getCommareaInboundLength() +
	                                 " which is greater than the max asked for of " + correctInboundLen);
	                }
	            }
	        }
		}
		catch(Exception ex)
		{
	       	LOGGER.error("#"+threadNumber+":"+ 
	                        "Exception validateReturnedCommarea " + ex);
	       	ex.printStackTrace();
	    }
        
    }

	private byte[] GenerateCommarea(int threadNumber, CalledTransaction calledTransaction)
	{
		//RUN0001   Uses a COMMAREA to control the instruction loops that the program goes through. Caution needs to used here so that abends are not generated.
		//
		// The COMMAREA is  40 bytes in length with the following format
		// Byte 1  is equal to the letter D
		// Bytes 2-9     should always be equal to zeros
		// Bytes 10 - 17     Minor loop control factor   
		//		Example:  00000990          Do not increase this number unless it is reviewed by the
		//                                                                           SYSVIEW team  (abends will result in the transaction)
		// Bytes 18 - 25     Major loop control factor
		// 		Example:  00000999
		// Bytes  25 - 40   Filler

		try
		{
		    // RUN0001 expects a 40 byte commarea
		    calledTransaction.minCommArea(40); 
		    
		    // Allocate a commarea buffer to the size of the buffer defined in the transaction
			byte[] commarea = new byte[calledTransaction.getBufferLength()]; 
			
			commarea[0] = "D".getBytes("IBM037")[0];
			
			System.arraycopy("00000000".getBytes("IBM037"), 0, commarea, 1, 8);
			System.arraycopy("00000344".getBytes("IBM037"), 0, commarea, 9, 8);
			
			String majorLoopControl = calledTransaction.getParameters().get(0);
			int leftOver = 8 - majorLoopControl.length();
			if (leftOver < 0)
			{
				String fixedLoopControl = majorLoopControl.substring(0, 8);
				LOGGER.warn("#"+threadNumber+":"+ 
	                        "Parameter too large, max 8 chars.  Value is <" + majorLoopControl + 
							"> changed to <"+fixedLoopControl+">");
				majorLoopControl = fixedLoopControl;
			}
			
			for (int i = 0; i < leftOver; i++)
			{
				commarea[17 + i] = "0".getBytes("IBM037")[0];
			}
			
			System.arraycopy(majorLoopControl.getBytes("IBM037"), 0, commarea, 17 + leftOver, 
							 majorLoopControl.length());
			
			for (int i = 0; i < 16; i++)
			{
				commarea[24 + i] = " ".getBytes("IBM037")[0];
			}
			
            eciRequest.setCommareaOutboundLength(calledTransaction.isOutBoundLength());
            if (calledTransaction.isOutBoundLength())
                eciRequest.setCommareaOutboundLength(calledTransaction.getOutBoundLength());
            
            eciRequest.setCommareaInboundLength(calledTransaction.isInBoundLength());
            if (calledTransaction.isInBoundLength())
                eciRequest.setCommareaInboundLength(calledTransaction.getInBoundLength());
            
            eciRequest.Commarea = commarea;
            eciRequest.Commarea_Length = calledTransaction.getCommAreaLength();
                        
            LOGGER.debug("#"+threadNumber+":"+ 
                         "Commarea: OutboundLen = " + (eciRequest.isCommareaOutboundLength() ? eciRequest.getCommareaOutboundLength() : "NONE") + 
                         ", buflen = " + commarea.length + 
                         " len = " + eciRequest.Commarea_Length); 
                         
            LOGGER.debug("#"+threadNumber+":"+ 
                         "Commarea value = " + commarea);
			
			
			return commarea;
			
		}
		catch (Exception ex)
		{
			LOGGER.debug("#"+threadNumber+":"+ 
	                        "GenerateCommarea() " + ex);
			return null;
		}
	}
	
	byte[] getBytes(String source) throws java.io.UnsupportedEncodingException
	{
		if (bDataConv)
		{
			return source.getBytes(strDataConv);
		}
		else
		{
			return source.getBytes();
		}
	}

}
