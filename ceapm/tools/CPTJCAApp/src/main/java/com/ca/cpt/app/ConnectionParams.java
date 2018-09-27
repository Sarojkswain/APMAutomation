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

package com.ca.cpt.app;

public class ConnectionParams
{
    private String hostName;
    private String qmName;
    private String qName;
    private int port;
    private String qmType;

    public ConnectionParams(String host, String qmName, int p,
            String queueName, String type)
    {
        this.hostName = host;
        this.qmName = qmName;
        this.qName = queueName;
        this.port = p;
        this.qmType = type;
    }

    /**
     * @return the hostName
     */
    public String getHostName()
    {
        return hostName;
    }

    /**
     * @param hostName
     *            the hostName to set
     */
    public void setHostName(String hostName)
    {
        this.hostName = hostName;
    }

    /**
     * @return the qmName
     */
    public String getQmName()
    {
        return qmName;
    }

    /**
     * @param qmName
     *            the qmName to set
     */
    public void setQmName(String qmName)
    {
        this.qmName = qmName;
    }

    /**
     * @return the port
     */
    public int getPort()
    {
        return port;
    }

    /**
     * @param port
     *            the port to set
     */
    public void setPort(int port)
    {
        this.port = port;
    }

    /**
     * @return the qmType
     */
    public String getQmType()
    {
        return qmType;
    }

    /**
     * @param qmType
     *            the qmType to set
     */
    public void setQmType(String qmType)
    {
        this.qmType = qmType;
    }

    /**
     * @return the qName
     */
    public String getQName()
    {
        return qName;
    }

    /**
     * @param name
     *            the qName to set
     */
    public void setQName(String name)
    {
        qName = name;
    }
}
