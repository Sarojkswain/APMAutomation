/*
 * Copyright (c) 2014 CA. All rights reserved.
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
 * 
 * Author : JAMSA07/ SANTOSH JAMMI
 * Date : 11/04/2016
 */
package com.ca.apm.commons.flow;

import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.apm.automation.action.flow.IFlowContext;

import static org.apache.http.util.Args.notNull;
import static org.apache.http.util.Args.positive;


public class StopServiceFlowContext implements IFlowContext {

    private final String processToKill;
    private int timeOut;
    private int retries;

  public StopServiceFlowContext(Builder builder) {
      processToKill = builder.processToKill;
      timeOut = builder.timeOut;
      retries = builder.retries;
      
  }
  
  public String getProcessToKill() {
      return processToKill;
    }

  public int getTimeOut() {
      return timeOut;
    }
  public int getRetries() {
      return retries;
    }

  
  public static class Builder implements IBuilder<StopServiceFlowContext> {

    private String processToKill;
    private int timeOut;
    private int retries;
    
    public Builder processToKill(String value) {
        this.processToKill = value;
        return this;
      }

    public Builder timeOut(int value) {
        this.timeOut = value;
        return this;
      }

    public Builder retries(int value) {
        this.retries = value;
        return this;
      }

     

    @Override
    public StopServiceFlowContext build() {
      StopServiceFlowContext stopServiceFlowContext = new StopServiceFlowContext(this);
      notNull(stopServiceFlowContext.processToKill, "roleId");
      notNull(stopServiceFlowContext.timeOut, "timeOut");
      notNull(stopServiceFlowContext.retries, "retries");
      return stopServiceFlowContext;
    }
  }
}
