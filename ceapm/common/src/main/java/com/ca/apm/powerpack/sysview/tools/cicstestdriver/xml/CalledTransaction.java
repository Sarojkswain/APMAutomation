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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * One Called Transaction
 * @author macbr01
 *
 */
public class CalledTransaction {
    private static final Logger LOGGER = LoggerFactory.getLogger(CalledTransaction.class);

    //***********************************************
    // Configuration properties
    // Warning: MAKE SURE YOU CLONE ANY NEW MEMBERS
    //***********************************************
    private String transactionID = null;
    private int bufferLength = -1;
    private int commAreaLength = -1;
    private int outBoundLength = -1;
    private int inBoundLength = -1;
    private int socketBufferSize = 1;  // Size of buffer of socket data
    private ArrayList<String> parameters = null;
    private String  commAreaData = null;

    //**********************************
    // Derived Configuration properties
    //**********************************

    // There are no derived configuration properties


    public CalledTransaction() {
    }

    public CalledTransaction(String transactionID) {
        this.transactionID = transactionID;
    }

    public CalledTransaction(String inTransactionID, int inBufferLength, int inCommAreaLength,
        int inOutBoundLength, int inInBoundLength, int inSocketBufferSize,
        ArrayList<String> inParameters, String inCommAreaData) {
        transactionID = inTransactionID;
        bufferLength = inBufferLength;
        commAreaLength = inCommAreaLength;
        outBoundLength = inOutBoundLength;
        inBoundLength = inInBoundLength;
        socketBufferSize = inSocketBufferSize;
        if (inParameters != null)
        {
            int numberOfParameters = inParameters.size();
            for (int index = 0; index < numberOfParameters; index++)
            {
                addParameter(inParameters.get(index));
            }
        }
        commAreaData = inCommAreaData;
    }

    public CalledTransaction clone() {
        return new CalledTransaction(transactionID, bufferLength, commAreaLength, outBoundLength,
            inBoundLength, socketBufferSize, parameters, commAreaData);
    }

    // Getters and Setters
    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public ArrayList<String> getParameters() {
        return parameters;
    }

    public void setParameters(ArrayList<String> inParameters) {
        parameters = inParameters;
    }

    public void addParameter(String parameter) {
        if (parameters == null) {
            parameters = new ArrayList<>();
        }
        parameters.add(parameter);
    }

    public int getBufferLength() {
        return bufferLength;
    }

    public void setBufferLength(int bufferLength) {
        this.bufferLength = bufferLength;
    }

    public int getCommAreaLength() {
        return commAreaLength;
    }

    public void setCommAreaLength(int commAreaLength) {
        this.commAreaLength = commAreaLength;
    }

    public int getOutBoundLength() {
        return outBoundLength;
    }

    public void setOutBoundLength(int outBoundLength) {
        this.outBoundLength = outBoundLength;
    }

    public boolean isOutBoundLength() {
        return outBoundLength >= 0;
    }

    public int getInBoundLength() {
        return inBoundLength;
    }

    public void setInBoundLength(int inBoundLength) {
        this.inBoundLength = inBoundLength;
    }

    public boolean isInBoundLength() {
        return inBoundLength >= 0;
    }

    public int getSocketBufferSize() {
        return socketBufferSize;
    }

    public void setSocketBufferSize(int socketBufferSize) {
        this.socketBufferSize = socketBufferSize;
    }

    public String getCommAreaData() {
        return commAreaData;
    }

    public void setCommAreaData(String commAreaData) {
        this.commAreaData = commAreaData;
    }

    public String toString() {
        String transactionString = getTransactionID() + "(";
        if (parameters != null) {
            int numberOfParameters = parameters.size();
            for (int index = 0; index < numberOfParameters; index++) {
                String parameterValue = parameters.get(index);
                transactionString += parameterValue +
                    (((index + 1) < numberOfParameters) ? ", " : "");
            }
        }
        transactionString += ")";
        if (commAreaData != null) {
            transactionString += ":[" + commAreaData + "]";
        }
        return transactionString;
    }

    /**
     * Set Commarea related lengths to a minimum required
     * Validate all length setting related to the commarea
     * @param minimumLen Length to use.
     */
    public void minCommArea(int minimumLen) {
        if(minimumLen < 0) {
            minimumLen = 0;
        }
        LOGGER.debug("minCommArea starts with commAreaLength = " + commAreaLength + " and min at "
            + minimumLen);
        // Commarea length cannot be less than the minimum
        if (commAreaLength < minimumLen) {
            commAreaLength = minimumLen;
        }

        // If defined then out bound length cannot be below the minimum
        if (outBoundLength >= 0) {
            // The out bound length cannot be less than the minimum commarea length
            if (outBoundLength < minimumLen) {
                outBoundLength = minimumLen;
            }

            // The buffer length cannot be less than the defined outbound length
            if (bufferLength < outBoundLength) {
                bufferLength = outBoundLength;
            }

            // The commarea length cannot be less than the outbound length
            if (commAreaLength < outBoundLength) {
                commAreaLength = outBoundLength;
            }
        }
        else {
            // Since the out bound length is not defined the commarea length
            // will be used for the out bound length.   So the buffer cannot
            // be less than this.

            // The buffer length cannot be less than the commarea length
            if (bufferLength < commAreaLength) {
                bufferLength = commAreaLength;
            }

        }

        // If defined then in bound length cannot be below the minimum
        if (inBoundLength >= 0) {
            // The buffer length cannot be less than the defined inbound length
            if (bufferLength < inBoundLength) {
                bufferLength = inBoundLength;
            }

            // The commarea length cannot be less than the inbound length
            if (commAreaLength < inBoundLength) {
                commAreaLength = inBoundLength;
            }
        }
        else {
            // Since the in bound length is not defined the commarea length
            // will be used for the in bound length.   So the buffer cannot
            // be less than this.

            // The buffer length cannot be less than the commarea length
            if (bufferLength < commAreaLength) {
                bufferLength = commAreaLength;
            }

        }

        LOGGER.debug("minCommArea ends with commAreaLength = " + commAreaLength) ;

        //********************************************************************************************
        //  Otherwise commAreaLength is allowed to be larger than buffer length, or out bound length
        //  bufferLength is also allowed to be bigger than commAreaLength or out bound length
        //********************************************************************************************
    }
}