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

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.ca.apm.powerpack.sysview.tools.cicstestdriver.xml.CallJobStack;
import com.ca.apm.powerpack.sysview.tools.cicstestdriver.xml.CalledTransaction;
import com.ca.apm.powerpack.sysview.tools.cicstestdriver.xml.CalledUnitOfWork;

/**
 * This is just a stub class
 * @author macbr01
 *
 */
public class WebServicesAdaptor extends GenericAdaptor
{
	private static final Logger LOGGER = Logger.getLogger(WebServicesAdaptor.class);

	public static final int MAX_WEB_TRANSACTIONS = 3;

	/**
	 * Initialize the adaptor with the command line arguments and
	 * returns true if successful.   If false then the arguments
	 * are invalid.
	 */
	@Override
	public boolean initialize(CallJobStack inCallJobStack)
	{
		boolean argumentsGood = true;

		callJobStack = inCallJobStack;

		return argumentsGood;
	};

	/**
	 * Return the usage string for the command line arguments that this adaptor uses
	 */
	@Override
	public String getUsageString()
	{
		return "-webservicename webservicename";
	};

	/**
	 * Run a unit of work using this adaptor
	 * @param calledUnitOfWork = the unit of work to run
	 */
	@Override
	public void RunUOW(int threadNumber, CalledUnitOfWork calledUnitOfWork)
	{
		ArrayList<CalledTransaction> calledTransactions = calledUnitOfWork.getCalledTransactions();

		if (calledTransactions != null)
		{
			int numberOfTransactions = calledTransactions.size();

			// If we have less than the number of transactions that can
			// be called in a group then
			if (numberOfTransactions <= MAX_WEB_TRANSACTIONS)
			{
				// Call them all as one group
				RunTransactionsAsGroup(calledTransactions);
			}

			// Otherwise call the transactions in groups
			else
			{
				ArrayList<CalledTransaction> shortenedCalledTransactions =
					new ArrayList<CalledTransaction>(MAX_WEB_TRANSACTIONS);

				for (int transactionIndex = 0; transactionIndex < numberOfTransactions; transactionIndex++)
				{
					// Move one item from long list to shortened list
					shortenedCalledTransactions.add(calledTransactions.get(transactionIndex));

					// If the shortened list is full then
					if (shortenedCalledTransactions.size() >= MAX_WEB_TRANSACTIONS)
					{
						// Call the web service with the shortened list
						RunTransactionsAsGroup(shortenedCalledTransactions);

						// Clear the list
						shortenedCalledTransactions.clear();
					}
				}

				// Process any left over transactions in the short list
				if (shortenedCalledTransactions.size() > 0)
				{
					// Call the web service with the shortened list
					RunTransactionsAsGroup(shortenedCalledTransactions);
				}
			}
		}
	};

	/**
	 * Run several transactions via a web service
	 * @param calledTransactions
	 */
	private void RunTransactionsAsGroup(ArrayList<CalledTransaction> calledTransactions)
	{
		int numberOfTransactions = calledTransactions.size();

		// Debugging
		if (LOGGER.isDebugEnabled())
		{
			String transactionListString = "Calling Web Service With: ";

			for (int transactionIndex = 0; transactionIndex < numberOfTransactions; transactionIndex++)
			{
				CalledTransaction calledTransaction = calledTransactions.get(transactionIndex);
				transactionListString +=  calledTransaction.toString() + ";";
			}
			LOGGER.debug(transactionListString);
		}
	}

}
