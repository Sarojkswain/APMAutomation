package com.ca.apm.tests.utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class OutputConsoleThread implements Runnable
{

    private InputStream src;
    private OutputStream dest;
    private int msgType;
    public boolean isCompleted = false;
	
 /**
     * Constructor.
     * @param src
     * @param dest
     */
	 
    public OutputConsoleThread(InputStream src, OutputStream dest){
        this.src = src;
        this.dest = dest;
        
    }
	
 /**
     * Over ride method, which reads input from console and write to a file. 
     */
    @Override
    public void run()
    {
        try{
        int ret = -1;  
        while ((ret = src.read()) != -1) {
           // System.out.println(ret);
            dest.write(ret);
            dest.flush();
        }
       
        }catch (Exception e) {
           e.printStackTrace();
        }finally{
            try
            {
                dest.close();
                src.close();
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        }
        
    }

}
