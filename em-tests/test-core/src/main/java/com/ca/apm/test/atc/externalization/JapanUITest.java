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
package com.ca.apm.test.atc.externalization;

import java.util.Set;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.IClass;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;

import com.ca.apm.test.atc.UITest;

import static com.ca.apm.test.atc.common.Browser.addCommonCaps;

public class JapanUITest extends UITest {

    @Override
    protected DesiredCapabilities prepChromeCaps() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("enable-precise-memory-info", "disable-extensions", "start-maximized",
            "disable-infobars", "lang=ja");
        DesiredCapabilities cap = DesiredCapabilities.chrome();
        cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        cap.setCapability(ChromeOptions.CAPABILITY, options);
        addCommonCaps(cap);
        return cap;
    }

    @AfterMethod
    public void after(final boolean success) {
        super.after(new ITestResult() {
            
            @Override
            public int compareTo(ITestResult o) {
                // TODO Auto-generated method stub
                return 0;
            }
            
            @Override
            public void setAttribute(String arg0, Object arg1) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public Object removeAttribute(String arg0) {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public Set<String> getAttributeNames() {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public Object getAttribute(String arg0) {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public void setThrowable(Throwable arg0) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void setStatus(int arg0) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void setParameters(Object[] arg0) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void setEndMillis(long arg0) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public boolean isSuccess() {
                return success;
            }
            
            @Override
            public Throwable getThrowable() {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public String getTestName() {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public ITestContext getTestContext() {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public IClass getTestClass() {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public int getStatus() {
                if (success) {
                    return ITestResult.SUCCESS;
                } else {
                    return ITestResult.FAILURE;
                }
            }
            
            @Override
            public long getStartMillis() {
                // TODO Auto-generated method stub
                return 0;
            }
            
            @Override
            public Object[] getParameters() {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public String getName() {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public ITestNGMethod getMethod() {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public String getInstanceName() {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public Object getInstance() {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public String getHost() {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public long getEndMillis() {
                // TODO Auto-generated method stub
                return 0;
            }
        });
    }
}
