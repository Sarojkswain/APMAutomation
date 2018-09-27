package com.ca.apm.commons.coda.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * This is Utility Class used for APM base - smart stor tools module, which contains re-usable methods used for SmartStore module.
 * @author vadsr03
 *
 */
public class SSToolsUtil {
    

	
 /**
     *  This method to is to execute the command and  compare the console messages against the passed list
     *  that would be checked against List of compareStrings and issue with control giving back to console
     *  which uses Thread.
     * @param commands - Array of input arguments to construct a smart stor tools command.
     * @param dirLoc - location of data directory
     * @param compareStrings -  compares the list of strings which used to check the message in the console.   
     * @throws IOException
     */
	public static int executeSmartCommand(String[] commands, String dirLoc, List<String> compareStrings) throws IOException{
		
		int found = 0;
		BufferedReader reader = null;
		Process process =  ApmbaseUtil.getProcess(commands, dirLoc);
		
		if(process == null) return found;
		try{
		reader=new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		if(compareStrings == null || compareStrings.isEmpty()){
			while ((line = reader.readLine()) != null) {
				if(line.contains("Usage") || line.contains("-dest") || line.contains("-help") || line.contains("-metrics") || line.contains("src")|| line.contains("data")) {
				    System.out.println(line);
					found = 1;
					break;
				}
			}	
		}else{
			line = reader.readLine();
			while ((line) != null) {
				
				  for (String errorMsg : compareStrings) {
					if(line.contains(errorMsg)){
					 compareStrings.remove(errorMsg);
					 break;
					 }
				}
				  line = reader.readLine();
				  System.out.println(line);
                  System.out.println(compareStrings);
			}
			if(compareStrings.size() == 0) found = 1;
		}
		
		}finally{
			if(reader!=null) reader.close();
			process.destroy();
		}
		return found;
	}
	
}
