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
 * Definition for a unit of work to be called.
 * @author macbr01
 *
 */
public class CalledUnitOfWork
{
    private static final int MIN_REPEAT = 1;
    private static final int MAX_REPEAT = 10;

    //***********************************************
    // Configuration properties
    // Warning: MAKE SURE YOU CLONE ANY NEW MEMBERS
    //***********************************************
    private int proportionOfCalls; // A value proportional to the amount of calls desired
                                   // as compared to sister units of work in the same distribution.
    private int transactionRepeat = MIN_REPEAT; // The number of times to repeat the transaction.
    private ArrayList<CalledTransaction> calledTransactions = null;
    private boolean testXA = false; // Should this test XA transaction calls
    private String  userID = null;
    private String  password = null;

    //**********************************
    // Derived Configuration properties
    //**********************************

    // There are no derived configuration properties

    public CalledUnitOfWork()
    {
    }

    public CalledUnitOfWork(int inProportionOfCalls)
    {
        setProportionOfCalls(inProportionOfCalls);
    }

    public CalledUnitOfWork(int inProportionOfCalls, int inTransactionRepeat,
                            boolean inTestXA, String inUserID, String inPassword,
                            ArrayList<CalledTransaction> inCalledTransactions)
    {
        setProportionOfCalls(inProportionOfCalls);
        setTransactionRepeat(inTransactionRepeat);
        setTestXA(inTestXA);
        setUserID(inUserID);
        setPassword(inPassword);
        if (inCalledTransactions != null)
        {
            int numberOfTransactions = inCalledTransactions.size();
            for (int index = 0; index < numberOfTransactions; index++)
            {
                addCalledTransaction(inCalledTransactions.get(index).clone());
            }
        }
    }

    public CalledUnitOfWork clone()
    {
        CalledUnitOfWork calledUnitOfWork = new CalledUnitOfWork(proportionOfCalls, transactionRepeat, testXA, userID, password, calledTransactions);
        return calledUnitOfWork;
    }

    // Getters and Setters
    public int getProportionOfCalls()
    {
        return proportionOfCalls;
    }
    public void setProportionOfCalls(int inProportionOfCalls)
    {
        proportionOfCalls = inProportionOfCalls;
    }

    public int getTransactionRepeat()
    {
        return transactionRepeat;
    }

    public boolean isTestXA()
    {
        return testXA;
    }

    public void setTestXA(boolean inTestXA)
    {
        this.testXA = inTestXA;
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

    public void setTransactionRepeat(int inTransactionRepeat)
    {
        if (inTransactionRepeat < MIN_REPEAT) {
            inTransactionRepeat = MIN_REPEAT;
        }
        if (inTransactionRepeat > MAX_REPEAT) {
            inTransactionRepeat = MAX_REPEAT;
        }
        this.transactionRepeat = inTransactionRepeat;
    }

    public ArrayList<CalledTransaction> getCalledTransactions()
    {
        return calledTransactions;
    }
    public void setCalledTransactions(ArrayList<CalledTransaction> inCalledTransactions)
    {
        calledTransactions = inCalledTransactions;
    }

    /**
     * Add a called transaction
     * @param calledTransaction Called transaction.
     */
    public void addCalledTransaction(CalledTransaction calledTransaction)
    {
        if (calledTransactions == null) {
            calledTransactions = new ArrayList<CalledTransaction>();
        }
        calledTransactions.add(calledTransaction);
    }
}
