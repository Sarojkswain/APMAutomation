package com.ca.apm.tests.test;
import java.util.ArrayList;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
public class WebViewLoginLogout extends WebDriverWrapper
{

    ArrayList<String> handleCount =null;
    String oldtab=null;
    int BSCount=1,BSCount1=1,BSCount2=1,BSCount3=1,BSCount4=1,BSCount5=1;
    public void logintoCemAdminConsole(String url,String userName, String password)
    {
      logintoATC();
      fd.get(url);
      we=  waitExplicitPresenceOfElementByXPath(CEM_LOGIN_LOGINHOMELINK);
      we.click();
      we=  waitExplicitPresenceOfElementByXPath(CEM_LOGIN_USERNAME);
      we.sendKeys(userName);
      we=  waitExplicitPresenceOfElementByXPath(CEM_LOGIN_PASSWORD);
      String password1 = password;
      we.sendKeys(password);
      we=  waitExplicitPresenceOfElementByXPath(CEM_LOGIN_LOGINBUTTON);
      we.click();
    }
    
    public void loginlogoutFromCemAdminConsole()
    {
        WebElement Logout = waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_ADDALLCOMP_LOGOUT);
        Logout.click();
        WebElement UserName = waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_ADDALLCOMP_USERNAME);
        UserName.sendKeys("cemadmin");
        WebElement Password =  waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_ADDALLCOMP_PASSWORD);
        Password.sendKeys("quality");
        WebElement Login =  waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_ADDALLCOMP_LOGIN);
        Login.click();
    }
    public void addBS(String bsName, String bsDescription, int numbeOfBSCount)
      //Now click on Administration Add BS
    {
        for(int i=1;i<=numbeOfBSCount;i++)
        {
      we=  waitExplicitPresenceOfElement(CEM_ADMINISTRATION4);
      we.click();
      we=  waitExplicitPresenceOfElement(CEM_ADMINISTRATION_BUSINESSSERVICE);
      we.click();
      we=  waitExplicitPresenceOfElement(CEM_ADMINISTRATION_BSERVICE_NEW);
      we.click();
      we=  waitExplicitPresenceOfElement(CEM_ADMINISTRATION_BSERVICE_BSNAME1);
      we.sendKeys(bsName);
      we=  waitExplicitPresenceOfElement(CEM_ADMINISTRATION_BSERVICE_BSDESCRIPTION1);
      we.sendKeys(bsDescription);
      we=  waitExplicitPresenceOfElement(CEM_ADMINISTRATION_BSERVICE_BSSAVE);
      we.click();
        }
     
    }
    
    public void addBT(String btName, String btDescription, int numberOfBTCount)
    {
        
        WebElement BTNew =  waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_BSERVICE_BTNEW);
        BTNew.click();
        WebElement BTNa = waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_BSERVICE_BTNAME);
        BTNa.sendKeys(btName+numberOfBTCount);
        WebElement BTDescr = waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_BSERVICE_BTDESCR);
        BTDescr.sendKeys(btDescription);
        WebElement BTSave = waitExplicitPresenceOfElement(CEM_ADMINISTRATION_BSERVICE_BTSAVE);
        BTSave.click();
        try
        {
            Thread.sleep(5000);
        } catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
            
        }
        
    
        public void addAllBT()
        {   
            
            int rowcount=0;
            int columncount=0;
            int cellncount=0;
            
            try
            {
            Thread.sleep(1000);
            }
            catch(Exception e)
            {
                System.out.println("The Exception is"+e);
            }
            WebElement Administration4 = waitExplicitPresenceOfElement(CEM_ADMINISTRATION_ADDALLBT_ADMINISTRATION4);
            
            
            Administration4.click();
            WebElement BusinessService =waitExplicitPresenceOfElement(CEM_ADMINISTRATION_ADDALLBT_BUSINESSSERVICE);
                   
           
            BusinessService.click();
            try
            {
                Thread.sleep(10000);
            } catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            rowcount = fd.findElementsByXPath(CEM_ADMINISTRATION_ADDALLBT_ROWCOUNT).size();
            columncount = fd.findElementsByXPath(CEM_ADMINISTRATION_ADDALLBT_COLUMNCOUNT).size();
            cellncount = fd.findElementsByXPath(CEM_ADMINISTRATION_ADDALLBT_CELLCOUNT).size();
            System.out.println(rowcount+""+columncount+""+cellncount);
            
            for(;BSCount<=rowcount;BSCount++)
            {
               
                WebElement Administration5 = waitExplicitPresenceOfElement(CEM_ADMINISTRATION_ADDALLBT_ADMINISTRATION5);
                Administration5.click();
                WebElement BusinessService5 =waitExplicitPresenceOfElement(CEM_ADMINISTRATION_ADDALLBT_BUSINESSSERVICE5);
                BusinessService5.click();
                WebElement BSElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='tranDefGroup']/tbody/tr["+BSCount+"]/td[2]/a")));
                BSElement.click();
                addBT("BT","NewBT",BSCount);
                        
            }
            
            //loginlogoutFromCemAdminConsole();
            
        }
        
        
    
        
        
        public void addAllTransaction()
        {
           
            int rowcount=0;
            int columncount=0;
            int cellncount=0;
           
            try
            {
            Thread.sleep(1000);
            }
            catch(Exception e)
            {
                System.out.println("The Exception is"+e);
            }
            WebElement Administration4 = waitExplicitPresenceOfElement(CEM_ADMINISTRATION_ADDALLT_ADMINISTRATION4);
            Administration4.click();
            WebElement BusinessService = waitExplicitPresenceOfElement(CEM_ADMINISTRATION_ADDALLT_BUSINESSSERVICE);
            BusinessService.click();
            try
            {
                Thread.sleep(10000);
            } catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            rowcount =fd.findElementsByXPath(CEM_ADMINISTRATION_ADDALLT_ROWCOUNT).size();
            columncount = fd.findElementsByXPath(CEM_ADMINISTRATION_ADDALLT_COLUMNCOUNT).size();
            cellncount = fd.findElementsByXPath(CEM_ADMINISTRATION_ADDALLT_ADMINISTRATION_AddALLT_CELLCOUNT).size();
            System.out.println(rowcount+""+columncount+""+cellncount);
            for(;BSCount1<=rowcount;BSCount1++)
            {
                WebElement Administration5 =waitExplicitPresenceOfElement(CEM_ADMINISTRATION_ADDALLT_ADMINISTRATION5);
                Administration5.click();
                WebElement BusinessService5 =waitExplicitPresenceOfElement(CEM_ADMINISTRATION_ADDALLT_BUSINESSSERVICE5);
                BusinessService5.click();
                WebElement BSElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='tranDefGroup']/tbody/tr["+BSCount1+"]/td[2]/a")));
                BSElement.click();
                addAllChildTransaction("T","NewT",BSCount1);
            }
            //loginlogoutFromCemAdminConsole();
        }
        
        public void addAllChildTransaction(String tName, String tDescription, int numberOfTCount)
        {
           
            WebElement BT = waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_ADDALLCHILDT_BT);
            BT.click();
            WebElement NewT = waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_ADDALLCHILDT_NEWT);
            NewT.click();
            WebElement TName =waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_ADDALLCHILDT_TNAME);
            TName.sendKeys("T"+numberOfTCount);
            WebElement TDescription =waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_ADDALLCHILDT_TDESCRIPTION);
            TDescription.sendKeys("This is a sample Transaction"+numberOfTCount);
            WebElement TSave = waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_ADDALLCHILDT_TSAVE);
            TSave.click();
            
                    
        }
        
        
      
        public void addAllComponents()
        {
             
            int rowcount=0;
            int columncount=0;
            int cellncount=0;
           
            try
            {
            Thread.sleep(1000);
            }
            catch(Exception e)
            {
                System.out.println("The Exception is"+e);
            }
            WebElement Administration4 = waitExplicitPresenceOfElement(CEM_ADMINISTRATION_ADDALLCOMP_ADMINISTRATION4);
             
            Administration4.click();
            WebElement BusinessService = waitExplicitPresenceOfElement(CEM_ADMINISTRATION_ADDALLCOMP_BUSINESSSERVICE);
                   
           
            BusinessService.click();
            try
            {
                Thread.sleep(10000);
            } catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            rowcount =fd.findElementsByXPath(CEM_ADMINISTRATION_ADDALLCOMP_ROWCOUNT).size();
            columncount = fd.findElementsByXPath(CEM_ADMINISTRATION_ADDALLCOMP_COLUMNCOUNT).size();
            cellncount = fd.findElementsByXPath(CEM_ADMINISTRATION_ADDALLCOMP_CELLCOUNT).size();
            System.out.println(rowcount+""+columncount+""+cellncount);
            for(;BSCount2<=rowcount;BSCount2++)
            {
                WebElement Administration5 = waitExplicitPresenceOfElement(CEM_ADMINISTRATION_ADDALLCOMP_ADMINISTRATION5);
                        
               
                Administration5.click();
                WebElement BusinessService5 = waitExplicitPresenceOfElement(CEM_ADMINISTRATION_ADDALLCOMP_BUSINESSERVICE5);
                        
                
                BusinessService5.click();
                WebElement BSElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='tranDefGroup']/tbody/tr["+BSCount2+"]/td[2]/a")));
                
                BSElement.click();
                WebElement BT = waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_ADDALLCOMP_BT);
                        
                
                BT.click();
                WebElement NewT = waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_ADDALLCOMP_NEWT);
                        
                
                NewT.click();
                addAllChildComponents("C","NewC",BSCount2);
                
            }
            //loginlogoutFromCemAdminConsole();
           
        }
        
        public void addAllChildComponents(String cName, String cDescription, int numberOfcCount)
        {
            
            WebElement CNew =  waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_ADDALLCHILDCOMP_CNEW);
                   
           
            CNew.click();
            WebElement CName =  waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_ADDALLCHILDCOMP_CNAME);
                    
            
            CName.sendKeys("C"+numberOfcCount);
            WebElement CDescr =  waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_ADDALLCHILDCOMP_CDESCR);
                    
           
            CDescr.sendKeys("This is a sample Componet"+numberOfcCount);
            WebElement CSave =  waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_ADDALLCHILDCOMP_CSAVE);
                  
            
            CSave.click();
            
        }
        
        
        
   
       /* public void addAllURLSubComponents(String urlHost)
        {
           
         
            int rowcount=0;
            int columncount=0;
            int cellncount=0;
         
            try
            {
            Thread.sleep(1000);
            }
            catch(Exception e)
            {
                System.out.println("The Exception is"+e);
            }
            WebElement Administration4 = waitExplicitPresenceOfElement(CEM_ADMINISTRATION_AddALLURL_ADMINISTRATION4);
                    
                 
            

            Administration4.click();
            WebElement BusinessService = waitExplicitPresenceOfElement(CEM_ADMINISTRATION_AddALLURL_BUSINESSSERVICE);
                   
            
            BusinessService.click();
            rowcount =fd.findElementsByXPath(CEM_ADMINISTRATION_AddALLURL_ROWCOUNT).size();
            columncount = fd.findElementsByXPath(CEM_ADMINISTRATION_AddALLURL_columncount).size();
            cellncount = fd.findElementsByXPath(CEM_ADMINISTRATION_AddALLURL_CELLCOUNT).size();
            System.out.println(rowcount+""+columncount+""+cellncount);
            for(;BSCount3<=rowcount;BSCount3++)
            {
                WebElement Administration5 = waitExplicitPresenceOfElement(CEM_ADMINISTRATION_AddALLURL_ADMINISTRATION5);
                      
               
                Administration5.click();
                WebElement BusinessService5 = waitExplicitPresenceOfElement(CEM_ADMINISTRATION_AddALLURL_BUSINESSSERVICE5);
                       
               
                BusinessService5.click();
                WebElement BSElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='tranDefGroup']/tbody/tr["+BSCount3+"]/td[2]/a")));
                
                BSElement.click();
                WebElement BT =waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_AddALLURL_BT);
                        
                                BT.click();
                WebElement NewT = waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_AddALLURL_BT);
                        
                
                NewT.click();
                WebElement NewC = waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_AddALLURL_BT);
                        
                
                NewC.click();
                int count11 = BSCount3;
                addAllChildURLSubComponents(urlHost,count11-1);
                
        }

            loginlogoutFromCemAdminConsole();
        }
        public void addAllChildURLSubComponents(String host,int count2)
        {
            
            
            
            WebElement NewURL = waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_ADDALLCHILDURL_NEWURL);
           NewURL.click();
         
           WebElement drp = waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_ADDALLCHILDURL_DRP);
           Select select=  selectfromDropDown(drp); 
            
          
            select.selectByIndex(3);
            try
            {
            Thread.sleep(1000);
            }
            catch(Exception e)
            {
                System.out.println("Exception is"+e);
            }
            WebElement Pattern = waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_ADDALLCHILDURL_PATTERN);
                   
           
            if(count2<10)
            {
            Pattern.sendKeys(host);
            }
            
            WebElement URLSave = waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_ADDALLCHILDURL_URLSAVE);
                   
            
            URLSave.click();
                
        }*/
        
        
      
        public void addAllPathSubComponents(String pathPre, String pathPost, int bsCount)
        {
           
            int rowcount=0;
            int columncount=0;
            int cellncount=0;
            
            try
            {
            Thread.sleep(1000);
            }
            catch(Exception e)
            {
                System.out.println("The Exception is"+e);
            }
            WebElement Administration4 =  waitExplicitPresenceOfElement(CEM_ADMINISTRATION_ADDALLPATH_ADMINISTRATION4);
                   
       
            Administration4.click();
            WebElement BusinessService =waitExplicitPresenceOfElement(CEM_ADMINISTRATION_ADDALLPATH_BUSINESSSERVICE); 
                   
           
            BusinessService.click();
            try
            {
                Thread.sleep(10000);
            } catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            rowcount =fd.findElementsByXPath(CEM_ADMINISTRATION_ADDALLPATH_ROWCOUNT).size();
            columncount = fd.findElementsByXPath(CEM_ADMINISTRATION_ADDALLPATH_COLUMNCOUNT).size();
            cellncount = fd.findElementsByXPath(CEM_ADMINISTRATION_ADDALLPATH_CELLCOUNT).size();
            System.out.println(rowcount+""+columncount+""+cellncount);
            BSCount4=bsCount;
            //for(;BSCount4<=rowcount;BSCount4++)
            //{
                WebElement Administration5 = waitExplicitPresenceOfElement(CEM_ADMINISTRATION_ADDALLPATH_ADMINISTRATION5);
                      
               
                Administration5.click();
                WebElement BusinessService5 =waitExplicitPresenceOfElement(CEM_ADMINISTRATION_ADDALLPATH_BUSINESSSERVICE5);
                        
               
                BusinessService5.click();
                WebElement BSElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='tranDefGroup']/tbody/tr["+BSCount4+"]/td[2]/a")));
               
                BSElement.click();
                WebElement BT =waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_ADDALLPATH_BT);
                     
              
                BT.click();
                WebElement NewT =waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_ADDALLPATH_NewT);
                        
              
                NewT.click();
                WebElement NewC =waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_ADDALLPATH_NewC);
                       
                
                NewC.click();
                int count12 = BSCount4;
                addAllChildPathSubComponents(pathPre,pathPost,count12-1);
                
        //}
      
            //loginlogoutFromCemAdminConsole();
            
        }
        
        public void addAllChildPathSubComponents(String pre, String post,int count2)
        {
         
           
            WebElement NewURL = waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_AddALLCHILDPATH_NEWURL);
                    
           
            NewURL.click();
            
            WebElement drp =waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_AddALLCHILDPATH_DRP);
                    
          
           Select select = selectfromDropDown(drp); 
            select.selectByIndex(3);
            try
            {
            Thread.sleep(1000);
            }
            catch(Exception e)
            {
                System.out.println("Exception is"+e);
            }
            
            WebElement drp1 = waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_AddALLCHILDPATH_DRP1);
                    
            
            Select select1 = selectfromDropDown(drp1); 
            select1.selectByIndex(1);
            try
            {
            Thread.sleep(1000);
            }
            catch(Exception e)
            {
                System.out.println("Exception is"+e);
            }
            WebElement Pattern = waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_AddALLCHILDPATH_PATTERN);
                   
         
            Pattern.sendKeys(pre+post);
            WebElement URLSave =waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_AddALLCHILDPATH_URLSAVE);
                
           
            URLSave.click();
                
        }
        
     
        public void enableAllBS()
        {
            
         
            int rowcount=0;
            int columncount=0;
            int cellncount=0;
           
            try
            {
            Thread.sleep(1000);
            }
            catch(Exception e)
            {
                System.out.println("The Exception is"+e);
            }
           
            WebElement Administration4 = waitExplicitPresenceOfElement(CEM_ADMINISTRATION_ENAALLBS_ADMINISTRATION4);
                  
           
            Administration4.click();
            WebElement BusinessService = waitExplicitPresenceOfElement(CEM_ADMINISTRATION_ENAALLBS_BUSINESSSERVICE);
                  
           
            BusinessService.click();
            try
            {
                Thread.sleep(10000);
            } catch (InterruptedException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            rowcount =fd.findElementsByXPath(CEM_ADMINISTRATION_ENAALLBS_ROWCOUNT).size();
            columncount = fd.findElementsByXPath(CEM_ADMINISTRATION_ENAALLBS_COLUMNCOUNT).size();
            cellncount = fd.findElementsByXPath(CEM_ADMINISTRATION_ENAALLBS_CELLCOUNT).size();
            System.out.println(rowcount+""+columncount+""+cellncount);
            for(;BSCount5<=rowcount;BSCount5++)
            {
               
                WebElement Administration5 =  waitExplicitPresenceOfElement(CEM_ADMINISTRATION_ENAALLBS_ADMINISTRATION5);
                        
               
                Administration5.click();
                WebElement BusinessService5 =  waitExplicitPresenceOfElement(CEM_ADMINISTRATION_ENAALLBS_BUSINESSSERVICE5);
                       
              
                BusinessService5.click();
                WebElement BSElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='tranDefGroup']/tbody/tr["+BSCount5+"]/td[2]/a")));
               
                BSElement.click();
                WebElement CheckBT = waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_ENAALLBS_CHECKBT);
                        
               
                CheckBT.click();
                WebElement EnableBT = waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_ENAALLBS_ENABLEBT);
                   
                
                EnableBT.click();
                try
                {
                    Thread.sleep(1000);
                }
                catch(Exception e)
                {
                    System.out.println("The Exception is"+ e);
                }
                BSCount=1;
                BSCount1=1;
                BSCount2=1;
                BSCount3=1;
                BSCount4=1;
                BSCount5=1; 
                
        }
            
              
        }
        
        
        
        public void disableAllBS()
        {
            
         
            int rowcount=0;
            int columncount=0;
            int cellncount=0;
           
            try
            {
            Thread.sleep(1000);
            }
            catch(Exception e)
            {
                System.out.println("The Exception is"+e);
            }
           
            WebElement Administration4 = waitExplicitPresenceOfElement(CEM_ADMINISTRATION_ENAALLBS_ADMINISTRATION4);
                  
           
            Administration4.click();
            WebElement BusinessService = waitExplicitPresenceOfElement(CEM_ADMINISTRATION_ENAALLBS_BUSINESSSERVICE);
                  
           
            BusinessService.click();
            try
            {
                Thread.sleep(10000);
            } catch (InterruptedException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            rowcount =fd.findElementsByXPath(CEM_ADMINISTRATION_ENAALLBS_ROWCOUNT).size();
            columncount = fd.findElementsByXPath(CEM_ADMINISTRATION_ENAALLBS_COLUMNCOUNT).size();
            cellncount = fd.findElementsByXPath(CEM_ADMINISTRATION_ENAALLBS_CELLCOUNT).size();
            System.out.println(rowcount+""+columncount+""+cellncount);
            for(;BSCount5<=rowcount;BSCount5++)
            {
               
                WebElement Administration5 =  waitExplicitPresenceOfElement(CEM_ADMINISTRATION_ENAALLBS_ADMINISTRATION5);
                        
               
                Administration5.click();
                WebElement BusinessService5 =  waitExplicitPresenceOfElement(CEM_ADMINISTRATION_ENAALLBS_BUSINESSSERVICE5);
                       
              
                BusinessService5.click();
                WebElement BSElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='tranDefGroup']/tbody/tr["+BSCount5+"]/td[2]/a")));
               
                BSElement.click();
                WebElement CheckBT = waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_ENAALLBS_CHECKBT);
                        
               
                CheckBT.click();
                WebElement disableBT = waitExplicitPresenceOfElementByXPath(CEM_ADMINISTRATION_ENAALLBS_DISABLEBT);
                   
                
                disableBT.click();
                try
                {
                    Thread.sleep(1000);
                }
                catch(Exception e)
                {
                    System.out.println("The Exception is"+ e);
                }
                
        }
            
            
        }
        
        public void syncronizeMonitors()
        {
                        
                        WebElement SyncMonitorsIcon =waitExplicitPresenceOfElement(CEM_SETUP);
                             
                        SyncMonitorsIcon.click();
                        try
                        {
                        Thread.sleep(10000);
                        }
                        catch(Exception e)
                        {
                            System.out.println("The Exception is"+e);
                        }
                        WebElement SyncMointors = waitExplicitPresenceOfElement(CEM_SYNCMON_SYNCMONITORS);
                        
                        
                        SyncMointors.click();
                        try
                        {
                        Thread.sleep(2000);
                        }
                        catch(Exception e)
                        {
                            System.out.println("The Exception is"+e);
                        }
              
                        
        }
        
       
        
        public void addalldataonCemUi(String BSName, String Path1, String Path2)
        {
            initializeEMandAgents();
            logintoCemAdminConsole("http://"+emHost+":8081", "cemadmin", "quality");
            addBS(BSName, "BSName", 1);
            addAllBT();
            addAllTransaction();
            addAllComponents();
            addAllPathSubComponents(Path1, Path2,1);
            enableAllBS();
            syncronizeMonitors();
        }
        
        
        public void deleteAllCemDatafromCemUi()
        {
            //loginlogoutFromCemAdminConsole();
            WebElement Administration5 =  waitExplicitPresenceOfElement(CEM_ADMINISTRATION_DELETEBS_ADMINISTRATION5);
            Administration5.click();
            WebElement BusinessService5 =  waitExplicitPresenceOfElement(CEM_ADMINISTRATION_DELETE_BUSINESSSERVICE5);
            BusinessService5.click();
            WebElement selectAll =  waitExplicitPresenceOfElement(CEM_ADMINISTRATION_DELETE_SELECTALLBS);
            selectAll.click();
            WebElement delete =  waitExplicitPresenceOfElement(CEM_ADMINISTRATION_DELETE_DELETE);
            delete.click();
            switchtoAlertandAcceptIt();
            
        }
        
        
        
        
        
        
        
        
        
        
   
    
    
    public void loginToTeamCenter()
    {
        initializeEMandAgents();
        wait = new WebDriverWait(fd,30);
        System.out.println("http://"+emHost+":8082/ApmServer/");
        fd.get("http://"+emHost+":8082/ApmServer/");
        fd.manage().window().maximize();
        oldtab = fd.getWindowHandle();
        fd.switchTo().frame("LoginFrame");
        we= waitExplicitPresenceOfElement(LOGIN_USERNAME);
        we.sendKeys("admin");
        we= waitExplicitPresenceOfElement(LOGIN_LOGINBUTTON);
        we.click();
        we =waitExplicitPresenceOfElement(POPUP_CLOSE); 
        we.click();
        
    }
    
   
 public void moveToWebView()
    {
        we =waitExplicitPresenceOfElement(LINK_WEBVIEW);
        we.click();
        handleCount = new ArrayList<String>(fd.getWindowHandles());
        System.out.println(handleCount);
        if(handleCount.size()>0)
        {
        handleCount.remove(0);
        System.out.println(handleCount);
        fd.switchTo().window(handleCount.get(0));
        }
        else
        {
            fd.switchTo().window(handleCount.get(0));
        }
        
    }
    
    
    
    
    public void logoutandLoginFromWebView()
    {
        initializeEMandAgents();
        try
        {
            Thread.sleep(30000);
        } catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        we =waitExplicitPresenceOfElementByXPath(WEBVIEW_LOGOUT); 
        mouseHover(we);
        we.click();
        fd.navigate().to("http://"+emHost+":8082/#home;tr=0");
        we= waitExplicitPresenceOfElement(LOGIN_USERNAME);
        we.sendKeys("admin");
        we= waitExplicitPresenceOfElement(LOGIN_LOGINBUTTON);
        we.click();
    }
    


}





