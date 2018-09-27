package com.ca.apm.commons.coda.common;


/**
 * This class is used for constants and commands  for smart store tools module in apm base.
 * @author vadsr03
 *
 */
public class SSToolsConstants {

    public static String platform = System.getProperty("os.name");
    
    public static  String ssTools="";
  
	 private SSToolsConstants(){ 
	 }
	
	 public static String SmartStorToolsBasedOnOSType()
	 {
	     if(platform.contains("Windows"))
	         ssTools="SmartStorTools.bat";
	     else
	         ssTools="./SmartStorTools.sh";
	     return ssTools;
	 }

	 /** Help Remove metricS  command */
	 public static final String[] HELP_CMD_REMOVE_METRICS = { SmartStorToolsBasedOnOSType(), "remove_metrics", "-help" };

	 /** Help Test Regex Command */
	 public static final String[] HELP_CMD_TEST_REGEX ={ SmartStorToolsBasedOnOSType(), "test_regex", "-help" };

    /**  */
	 public static final String[] HELP_CMD_TEST_REMOVE_AGENTS ={ SmartStorToolsBasedOnOSType(), "remove_agents", "-help" };
	 
	 /**Help Prune Command */
	 public static final String[] HELP_CMD_PRUNE ={ SmartStorToolsBasedOnOSType(), "prune", "-help" };
	 
	 /**Help Keep Agents Command */
	 public static final String[] HELP_CMD_KEEP_AGENTS ={ SmartStorToolsBasedOnOSType(), "keep_agents", "-help" };
	 
	 /**Help Merge Command */
	 public static final String[] HELP_CMD_MERGE ={ SmartStorToolsBasedOnOSType(), "merge", "-help" };
	 
	 /** */
	 public static final String[] INVALID_AGENT_CMD_TEST_REGEX ={SmartStorToolsBasedOnOSType(), "test_regex", "-agents", ".*ABCD.*" , "-src", "DYNAMIC"};
	 
	 /**  */
	 //Example: SmartStorTools.bat test_regex -agents ".*ABCD.*" -src C:\APM9.1\data
	 public static final String[] INVALID_CMD_AGENTS ={SmartStorToolsBasedOnOSType(), "test_agents", "-agents", "DYAGENT" , "-src", "DYNAMIC", "-dest", "DYNAMIC"};
	 
	 /** */
	 //Example: SmartStorTools.bat prune -src C:\APM9.1\data -dest C:\Destination1
	 public static final String[] INVALID_CMD_PRUNE_SRC ={SmartStorToolsBasedOnOSType(), "prune", "-src", "DYNAMIC" , "-dest", "DYNAMIC"};
	 
	
	 /** */
	 //Example: SmartStorTools.bat keep_agents -agents ".*WebLogic.*" -dest C:\APM9.1\data -src C:\Destination1
	 public static final String[] INVALID_CMD_AGENT_DIRS ={SmartStorToolsBasedOnOSType(), "keep_agents", "-agents", "DYAGENT", "-src", "DYNAMIC" , "-dest", "DYNAMIC"};
	 
	 /** */
	 //Example: SmartStorTools.bat remove_agents -agents ".*WebLogic.*" -dest C:\APM9.1\data -src C:\Destination1
	 public static final String[] INVALID_CMD_REMOVE_AGENT_DIRS ={SmartStorToolsBasedOnOSType(), "remove_agents", "-agents", "DYAGENT", "-src", "DYNAMIC" , "-dest", "DYNAMIC"};
	 
	 /** Command to test the regex functionality with agents and metrics expression */
	 //Example: SmartStorTools.bat test_regex -agents ".*WebLogic.*" -src C:\APM9.1\EM\data
	 public static final String[] TEST_REGEX_AGENT_FUN = { SmartStorToolsBasedOnOSType(), "test_regex", "-agents", "DYAGENT", "-src", "DYNAMIC" };
	 
	 /** Command to test the regex functionality with agents and metrics expression */
	 //Example: SmartStorTools.bat test_regex -agents ".*WebLogic.*" -src C:\APM9.1\EM\data
	 public static final String[] TEST_REGEX_METRICS_FUN = { SmartStorToolsBasedOnOSType(), "test_regex", "-metrics", "DYAGENT", "-src", "DYNAMIC" };
	 
	 /** */
	 //
	 public static final String[] MERGE_FUN_SRC_AND_DEST_NOT_EXISTS_SRC = { SmartStorToolsBasedOnOSType(), "merge", "DYNAMIC", "DYNAMIC" };
	 
	 public static final String[] MERGE_FUN_SRC_AND_DEST_NOT_EXISTS_DEST = { SmartStorToolsBasedOnOSType(), "merge", "DYNAMIC", "DYNAMIC" };
	 
	 
	 /**Prune functionality with valid parameters */
	 public static final String[] PRUNE_FUN_VALID_PARAM = {SmartStorToolsBasedOnOSType(), "Prune", "-dest", "DYNAMIC", "-src", "DYNAMIC"};
	 
	 /** Remove Agents with valid parameters*/
	 public static final String[] REMOVE_AGENTS_VALID_PARAMS = {SmartStorToolsBasedOnOSType(), "remove_agents", "-agents",  "DYAGENT", "-src", "DYNAMIC" , "-dest", "DYNAMIC"};
	 
	 public static final String[] REMOVE_AGENTS_CHECK_SRC ={SmartStorToolsBasedOnOSType(), "test_regex", "-agents", "DYAGENT", "-src", "DYNAMIC"};
	 
	 public static final String[] REMOVE_AGENTS_CHECK_DEST ={SmartStorToolsBasedOnOSType(), "test_regex", "-agents", "DYAGENT", "-src", "DYNAMIC"};
	 
	 
	 /** Remove Metrics with valid parameters */
	 public static final String[] REMOVE_METRICS_CHECK_SRC = {SmartStorToolsBasedOnOSType(), "test_regex", "-metrics", "DYAGENT", "-src", "DYNAMIC"};
	 
	 public static final String[] REMOVE_METRICS_VALID_PARAMS = {SmartStorToolsBasedOnOSType(), "remove_metrics", "-metrics",  "DYAGENT", "-src", "DYNAMIC" , "-dest", "DYNAMIC"};
	 
	 public static final String[] REMOVE_METRICS_CHECK_DEST = {SmartStorToolsBasedOnOSType(), "test_regex", "-metrics", "DYAGENT", "-src", "DYNAMIC"};
	 
	 
	 /** Test_Regex functionality with valid parameters **/
	 public static final String[] TEST_REGEX_FUN_VALID_PARAMS_METRICS = {SmartStorToolsBasedOnOSType(), "test_regex", "-metrics", "DYAGENT", "-src", "DYNAMIC"};
	 
	 public static final String[] TEST_REGEX_FUN_VALID_PARAMS_AGENTS = {SmartStorToolsBasedOnOSType(), "test_regex", "-agents", "DYAGENT", "-src", "DYNAMIC"};
	 
	 public static final String[] INVALID_CMD_PRUNE_DEST ={SmartStorToolsBasedOnOSType(), "prune", "-src", "DYNAMIC" , "-dest", "DYNAMIC"}; 
	 
	 public static final String[] INVALID_SRC_CMD_TEST_REGEX ={SmartStorToolsBasedOnOSType(), "test_regex", "-agents", ".*ABCD.*" , "-src", "DYNAMIC"};
	 
	 public static final String[] INVALID_CMD_REMOVE_AGENT = {SmartStorToolsBasedOnOSType(), "remove_agents", "-agents", "DYAGENT", "-src", "DYNAMIC" , "-dest", "DYNAMIC"};
	 
	 public static final String[] INVALID_CMD_REMOVE_METRIC = {SmartStorToolsBasedOnOSType(), "remove_metrics", "-metrics",  "DYAGENT", "-src", "DYNAMIC" , "-dest", "DYNAMIC"};
	 
	 public static final String[] CMD_SOCKET_METRIC ={SmartStorToolsBasedOnOSType(), "test_metrics", "-socket",  "DYAGENT", "-src", "DYNAMIC" , "-dest", "DYNAMIC"};
	 
	 public static final String[] REMOVE_METRICS_INVALID_PARAMS = {SmartStorToolsBasedOnOSType(), "remove_metrics", "-metrics",  "DYAGENT", "-src", "DYNAMIC" , "-dest", "DYNAMIC"};
	 
	 public static final String[] REMOVE_CUSTOM_METRICS_PARAMS = {SmartStorToolsBasedOnOSType(), "remove_metrics", "-metrics",  "DYAGENT", "-src", "DYNAMIC" , "-dest", "DYNAMIC"};
	 
	 public static final String[] KEEP_AGENTS_VALID_PARAMS ={SmartStorToolsBasedOnOSType(), "keep_agents", "-agents", "DYAGENT", "-src", "DYNAMIC" , "-dest", "DYNAMIC"};
	 
	 public static final String[] REMOVE_METRICS_MOM_PARAMS = {SmartStorToolsBasedOnOSType(), "remove_metrics", "-metrics",  "DYAGENT", "-src", "DYNAMIC" , "-dest", "DYNAMIC"};
	 
	 public static final String[] REMOVE_AGENTS_MOM_PARAMS = {SmartStorToolsBasedOnOSType(), "remove_agents", "-agents",  "DYAGENT", "-src", "DYNAMIC" , "-dest", "DYNAMIC"};
		 
	 public static final String[] PRUNE_FUN_MOM_PARAMS = {SmartStorToolsBasedOnOSType(), "Prune", "-dest", "DYNAMIC", "-src", "DYNAMIC"};
		 
	 public static final String[] KEEP_AGENTS_MOM_PARAMS ={SmartStorToolsBasedOnOSType(), "keep_agents", "-agents", "DYAGENT", "-src", "DYNAMIC" , "-dest", "DYNAMIC"};
		 
	 public static final String[] TEST_REGEX_FUN_MOM_PARAMS_METRICS = {SmartStorToolsBasedOnOSType(), "test_regex", "-metrics", "DYAGENT", "-src", "DYNAMIC"};	
	 
	 public static final String[] INVALID_KEEP_AGENTS ={SmartStorToolsBasedOnOSType(), "keep_agents", "-agents", "ABCD" , "-src", "DYNAMIC", "-dest", "DYNAMIC"};
		 
	 public static final String[] INVALID_REMOVE_AGENTS ={SmartStorToolsBasedOnOSType(), "remove_agents", "-agents", "ABCD", "-src", "DYNAMIC" , "-dest", "DYNAMIC"};
	 
	 public static final String[] TEST_REGEX_METRICS_MOM = {SmartStorToolsBasedOnOSType(), "test_regex", "-metrics", "DYAGENT", "-src", "DYNAMIC"};
	 
	 public static final String[] REMOVE_AGENTS_MOM={SmartStorToolsBasedOnOSType(), "remove_agents", "-agents", "DYAGENT", "-src", "DYNAMIC", "-dest", "DYNAMIC"};

     public static final String[] TEST_REGEX_METRICS_REDIRECT_FILE = {SmartStorToolsBasedOnOSType(), "test_regex", "-metrics", "DYAGENT", "-src", "DYNAMIC", ">", "DYNAMIC"};

     public static final String[] TEST_MERGE_FUN_MOM_COLLECTOR = {SmartStorToolsBasedOnOSType(), "merge", "DYNAMIC", "DYNAMIC"};

     public static final String[] LIST_AGENTS = {SmartStorToolsBasedOnOSType(), "list_agents", "-agents", "DYAGENT", "-src", "DYNAMIC"};

}
