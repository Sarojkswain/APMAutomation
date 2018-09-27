package com.ca.apm.commons.coda.common;

// API to Hit the webpage provided the URL
// Author : RAJASHREE(nanra04)
// Date   : 4/1/2011 

import com.meterware.httpunit.Button;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.SubmitButton;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebImage;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebResponse;
public class AppInvoke {
    
    private WebResponse response;
    private WebConversation wc;
    private WebForm form=null;
    private WebLink link=null;
    private WebImage  image;
/** 
 * Method use to test application without help of browser 
 * Returns  The   response to last request and shows the corresponding code of application
 * @param url contains the url of application
 * @param type contain String of object types of application
 * @param name contain String of name of object
 * @param value corresponds to object
 */
    public WebResponse invokeTestApp(String url, String type, String name, String value)
    {

        try {
            if (url.contains("http://")) 
            {
                
                hit(url);               
            }
            this.performAction(type,name,value);
            System.out.println(response.getText());
            
            } catch (Exception e) 
               {
                 e.printStackTrace();
               }
        return response;
     }
/**
 * TO hit the corresponding url 
 * @param url Name of url
 */
    public boolean hit(String url)
    {
        try {
        
             wc = new WebConversation();
             HttpUnitOptions.setScriptingEnabled(false);
        
             HttpUnitOptions.setExceptionsThrownOnScriptError(false);
             HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);
             HttpUnitOptions.getScriptErrorMessages();
             response=wc.getResponse(url);
             if(response.getResponseCode()==200){
                 return true;
             }else{
                 return false;
             }
    
             }catch(Exception e)
                 {
                  e.printStackTrace();
                  return false;
                  }
             }
    
    /**
     * TO hit the corresponding url and returns the web response.
     * 
     * @param url - url to be hit
     *            
     */
    public WebResponse hitUrl(String url) {
        try {

            System.out.println(" ");
            wc = getWebConversation();
            HttpUnitOptions.setScriptingEnabled(false);
            HttpUnitOptions.setExceptionsThrownOnScriptError(false);
            HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);
            HttpUnitOptions.getScriptErrorMessages();
            response = wc.getResponse(url);

        } catch (Exception e) {
            e.printStackTrace();

        }

        return response;
    }
    
    /**
     * TO hit the corresponding url with Scripting Enabled and returns the web response.
     * 
     * @param url
     *            - url to be hit
     * 
     */
    public WebResponse hitUrlWithScriptingPropertyEnabled(String url)
    {
        try
        {

            System.out.println(" ");
            wc = getWebConversation();
            HttpUnitOptions.setScriptingEnabled(true);
            HttpUnitOptions.setExceptionsThrownOnScriptError(false);
            HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);
            HttpUnitOptions.getScriptErrorMessages();
            response = wc.getResponse(url);

        } catch (Exception e)
        {
            e.printStackTrace();

        }

        return response;
    }

    /**
     * Getter to get the response
     * @return the response
     */
    public WebResponse getResponse()
    {
        return response;
    }

    /**
     * This private method returns the web conversation object if wc is null
     * 
     * @return WebConversation
     */
    public WebConversation getWebConversation() {
        if (null == wc) {
            wc = new WebConversation();
        }
        return wc;
    }
    
/**
 * Test with  various objects of application    
 * @param type  Object Types in application
 * @param name  name of objects
 * @param value corresponding to object
 */
    public void performAction(String type,String name,String value) 
    {
        String[] types = type.split(";;");
        String[] names = name.split(";;");
        String[] values = value.split(";;");
        
        boolean b=true; 
        try 
        { 
            for(int i=0;i<types.length;i++)
            {
                if(types[i].equals("link")) 
                { 
                    WebLink link1 = response.getLinkWith(values[i]);
                    response = link1.click();
                }
                if(types[i].equals("linkImage"))
                { 
                    
                  
                    WebLink link = response.getLinkWithImageText(values[i]);
                    
                //  WebLink wl[]= response.getLinks();
                    
                //  for(int x=0;x<wl.length ;x++)
                //  {
                //      //&&(wl[x].getText()).equals(values[i])
                //      System.out.println(wl[x].getText());
                //  }
                    response = link.click();
                    
                
                    
                }
                if(types[i].equals("form") && names[i].equalsIgnoreCase("ID"))
                {
                    WebForm formarr[]=response.getForms();
                    form=formarr[Integer.parseInt(values[i])-1];                                
                }
                if(types[i].equals("form") && names[i].equalsIgnoreCase("name"))
                {
                    form=response.getFormWithName(values[i]);
                }
                if(types[i].equals("T"))
                {
                    form.setParameter(names[i], values[i]);
                }
                if(types[i].equals("C"))
                {   if(names[i].equalsIgnoreCase("name"))
                    {
                    form.setCheckbox(values[i], b);
                    }
                else
                    form.setCheckbox(names[i], values[i],b);
                }
                if(types[i].equals("R"))
                {
                    form.setParameter(names[i], values[i]);
                }
                if(types[i].equals("B"))
                {   
                            form.setParameter(names[i], values[i]);
                }
                if(types[i].equals("select"))
                {
                    String s[]=form.getOptions(names[i]);
                    String s1[]=form.getOptionValues(names[i]);
                    for(int count=0;count<s1.length;count++)
                    {
                        if(s[count].equals(values[i]))
                        {
                            form.setParameter(names[i],s1[count]);
                        }
                    }                           
                }
                if (types[i].equalsIgnoreCase("submit"))
                {
                    if((names.length == types.length) && names[i].equalsIgnoreCase("ID"))
                    {
                        if (names[i].equalsIgnoreCase("ID"))
                        {
                            SubmitButton[] submitButtons = form
                                    .getSubmitButtons();
                            for (SubmitButton submitButton : submitButtons)
                            {
                                if (submitButton.getID()
                                        .equalsIgnoreCase(values[i]))
                                {
                                    submitButton.click();
                                    response = wc.getCurrentPage();
                                }
                            }
                        }
                    }
                    else{
                        if (values[i] != null & !values[i].contains("null"))
                        {
                            SubmitButton sb = form.getSubmitButton(values[i]);
                            response = form.submit(sb);
                        } else
                        {
                            response = form.submit();
                        }
                    }
                }
                if (types[i].equalsIgnoreCase("button"))
                {
                    if (names[i].equalsIgnoreCase("name"))
                    {
                        Button[] buttons = form.getButtons();
                        for (Button button : buttons)
                        {
                            if (button.getName().equalsIgnoreCase(values[i]))
                            {
                                button.click();
                                response = wc.getCurrentPage();
                            }
                        }
                    }
                    if (names[i].equalsIgnoreCase("value"))
                    {
                        Button[] buttons = form.getButtons();
                        for (Button button : buttons)
                        {
                            if (button.getValue().equalsIgnoreCase(values[i]))
                            {
                                button.click();
                                response = wc.getCurrentPage();
                            }
                        }
                    }
                }
            }   
        }catch(IllegalStateException i)
            {
            i.getMessage();
            System.out.println("exception is caught");
            }
        catch (Exception e) 
        {
            e.printStackTrace();
        }           
     }
}
