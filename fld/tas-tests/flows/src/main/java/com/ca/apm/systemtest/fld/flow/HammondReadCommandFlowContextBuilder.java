package com.ca.apm.systemtest.fld.flow;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.util.Args;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.tas.builder.BuilderBase;

/**
 * Builder which prepares a {@link RunCommandFlowContext run command flow context} for a Hammond read command. 
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class HammondReadCommandFlowContextBuilder extends BuilderBase<HammondReadCommandFlowContextBuilder, RunCommandFlowContext> {
    public static final String DEFAULT_MAIN_CLASS = "com.ca.apm.systemtest.fld.hammond.SmartstorReader";
    public static final String DEFAULT_INSTALL_JAR_FILE_NAME = "hammond.jar"; 
    public static final String DEFAULT_READ_TERMINATE_ON_MATCH = "Data loaded in";
    public static final String DEFAULT_HAMMOND_READ_PROCESS_NAME = "hammondReader";
    public static final String DEFAULT_XMX_VAL = "4g";
    public static final String DEFAULT_JAVA_CMD = "java";
    
    /**
     * Default constructor.
     */
    public HammondReadCommandFlowContextBuilder() {
        super();
    }

    protected String javaCmd = DEFAULT_JAVA_CMD;
    protected String commandCtxName = DEFAULT_HAMMOND_READ_PROCESS_NAME;
    protected String installPath;
    protected long fromMillis;
    protected long toMillis;
    protected boolean rocksDb = true;
    protected boolean tracesOnly = false;
    protected String heapMemory = DEFAULT_XMX_VAL;
    protected String inputFolderPath;
    protected String outputFolderPath;
    protected String inputSmartstorFolderName;
    protected String databaseFolderName;
    protected String transactionTracesFolderName;
    
    protected String hammondJarFileName = DEFAULT_INSTALL_JAR_FILE_NAME;
    protected String terminateReadingOnString = DEFAULT_READ_TERMINATE_ON_MATCH;
    protected String mainClass = DEFAULT_MAIN_CLASS; 
    protected List<String> additionalOpts = new LinkedList<>();
    
    @Override
    public RunCommandFlowContext build() {
        return getInstance();
    }

    /**
     * Sets the home path where Hammond is installed (folder in which the Hammond jar should be found). 
     * 
     * @param     path  Hammond root installation path
     * @return          this builder object
     */
    public HammondReadCommandFlowContextBuilder installDir(String path) {
        Args.notBlank(path, "Install dir");
        this.installPath = path;
        return builder();
    }

    /**
     * Sets <code>"-Xmx"</code> option for the launched JVM process.
     * 
     * @param     memory   max heap size value for the launched java process
     * @return             this builder object
     */
    public HammondReadCommandFlowContextBuilder heapMemory(String memory) {
        Args.notBlank(memory, "Heap memory");
        this.heapMemory = memory;
        return builder();
    }

    /**
     * Sets a name for the installed Hammond distribution jar file. Full path to the jar is then obtained 
     * concatenating {@link #installDir(String) the install path} with this jar name.  
     * 
     * @param    hammondJarFileName  name for the Hammond jar file
     * @return                       this builder object
     */
    public HammondReadCommandFlowContextBuilder hammondJarFileName(String hammondJarFileName) {
        Args.notBlank(hammondJarFileName, "Hammond Jar file name");
        this.hammondJarFileName = hammondJarFileName;
        return builder();
    }
    
    /**
     * Instructs Hammond to not try to read metadata from RocksDB.
     *  
     * <p/>
     * Corresponds to setting <code>--rocksdb</code> or <code>-rdb</code> command line option.
     *  
     * @return this builder object
     */
    public HammondReadCommandFlowContextBuilder noRocksDB() {
        this.rocksDb = false;
        return builder();
    }

    /**
     * Sets to read data from EM starting from time specified as milliseconds.
     *  
     * <p/>
     * Corresponds to setting <code>--from</code> or <code>-f</code> command line option.
     *  
     * @param fromMillis    read start time in milliseconds
     * @return              this builder object
     */
    public HammondReadCommandFlowContextBuilder fromMillis(long fromMillis) {
        this.fromMillis = fromMillis;
        return builder();
    }

    /**
     * Sets to read data from EM ending at time specified as milliseconds.
     * 
     * <p/>
     * Corresponds to setting <code>--to</code> or <code>-t</code> command line option.
     *  
     * @param toMillis     read end time in milliseconds
     * @return             this builder object
     */
    public HammondReadCommandFlowContextBuilder toMillis(long toMillis) {
        this.toMillis = toMillis;
        return builder();
    }
    
    /**
     * Adds an option to the Java process. This way you can pass JVM options 
     * (<code>"-XX:MaxHeapFreeRatio=30"</code>) or system properties (<code>"-DroleId=ROLE_123"</code>).
     *  
     * @param    option   additional option
     * @return            this builder object
     */
    public HammondReadCommandFlowContextBuilder option(String option) {
        this.additionalOpts.add(option);
        return builder();
    }

    /**
     * Sets command to launch Java. Default is <code>"java"</code>. 
     * 
     * @param    javaCmd   command to launch JVM process
     * @return             this builder object
     */
    public HammondReadCommandFlowContextBuilder javaCmd(String javaCmd) {
        Args.notBlank(javaCmd, "Java command");
        this.javaCmd = javaCmd;
        return builder();
    }

    /**
     * Sets custom name for the resultant {@link RunCommandFlowContext run command context}. 
     * 
     * @param commandCtxName     command context name
     * @return                   this builder object
     */
    public HammondReadCommandFlowContextBuilder commandCtxName(String commandCtxName) {
        Args.notBlank(commandCtxName, "Command context name");
        this.commandCtxName = commandCtxName;
        return builder();
    }

    /**
     * Sets the input folder for the Hammond's read command.
     * 
     * <p/>
     * Corresponds to setting <code>--input</code> or <code>-i</code> command line option. 
     * 
     * @param    inputFolderPath   input folder
     * @return                     this builder object
     */
    public HammondReadCommandFlowContextBuilder inputFolderPath(String inputFolderPath) {
        Args.notBlank(inputFolderPath, "Hammond input folder path");
        this.inputFolderPath = inputFolderPath;
        return builder();
    }

    /**
     * Sets the output folder for the Hammond's read command.
     * 
     * <p/>
     * Corresponds to setting <code>--output</code> or <code>-o</code> command line option. 
     * 
     * @param   outputFolderPath   output folder
     * @return                     this builder object
     */
    public HammondReadCommandFlowContextBuilder outputFolderPath(String outputFolderPath) {
        Args.notBlank(outputFolderPath, "Hammond output folder path");
        this.outputFolderPath = outputFolderPath;
        return builder();
    }

    /**
     * Sets if only the transaction traces should be extracted. By default, this option is set to <code>false</code>, so 
     * Hammond all relevant data.
     * 
     * <p/>
     * Corresponds to setting <code>--tracesonly</code> or <code>-tro</code> command line option.
     *  
     * @param   tracesOnly  <code>true</code> to extract only transaction traces, 
     *                      <code>false</code> to extract all data   
     * @return              this builder object
     */
    public HammondReadCommandFlowContextBuilder tracesOnly(boolean tracesOnly) {
        this.tracesOnly = tracesOnly;
        return builder();
    }

    /**
     * Sets smartstor folder name to search for. When this property is not set up explicitly, default 
     * value of <code>"data"</code> is used. 
     * 
     * <p/>
     * Corresponds to setting <code>--smartstor</code> or <code>-ss</code> command line option.
     * 
     * @param    inputSmartstorFolderName  smartstor database folder name
     * @return                             this builder object
     */
    public HammondReadCommandFlowContextBuilder smartstorFolderName(String inputSmartstorFolderName) {
        this.inputSmartstorFolderName = inputSmartstorFolderName;
        return builder();
    }

    /**
     * Sets Postgres database folder name to search for. When this property is not set up explicitly, default 
     * value of <code>"database"</code> is used.
     * 
     * @param       databaseFolderName    Postgres database folder name
     * @return                            this builder object
     */
    public HammondReadCommandFlowContextBuilder databaseFolderName(String databaseFolderName) {
        this.databaseFolderName = databaseFolderName;
        return builder();
    }

    /**
     * Sets transaction traces folder name to search for. When this property is not set up explicitly, default 
     * value of <code>"traces"</code> is used.
     * 
     * @param     transactionTracesFolderName  transaction traces folder name
     * @return                                 this builder object
     */
    public HammondReadCommandFlowContextBuilder transactionTracesFolderName(String transactionTracesFolderName) {
        this.transactionTracesFolderName = transactionTracesFolderName;
        return builder();
    }

    @Override
    protected HammondReadCommandFlowContextBuilder builder() {
        return this;
    }

    @Override
    protected RunCommandFlowContext getInstance() {
        if (installPath == null) {
            throw new IllegalStateException("Hammond install path can not be null!");
        }

        String hammondJar = concatPaths(installPath, hammondJarFileName);

        List<String> args = new ArrayList<>();
        args.add("-cp");
        args.add(hammondJar);
        args.add("-Xmx" + heapMemory);
        
        for (String opt : additionalOpts) {
            args.add(opt);
        }

        args.add(mainClass);
        args.add("-i");
        args.add(inputFolderPath);
        args.add("-o");
        args.add(outputFolderPath);
        args.add("--from");
        args.add(String.valueOf(fromMillis));
        args.add("--to");
        args.add(String.valueOf(toMillis));

        if (transactionTracesFolderName != null) {
            args.add("--traces");
            args.add(transactionTracesFolderName);
        }
        
        if (inputSmartstorFolderName != null) {
            args.add("--smartstor");
            args.add(inputSmartstorFolderName);
        }
        
        if (databaseFolderName != null) {
            args.add("--database");
            args.add(databaseFolderName);
        }
        
        if (tracesOnly) {
            args.add("--tracesonly");
        }

        if (rocksDb) {
            args.add("--rocksdb");
        }
        
        RunCommandFlowContext runHammondReadCommandFlowContext =
            new RunCommandFlowContext.Builder(javaCmd)
                .args(args)
                .workDir(installPath)
                .name(commandCtxName)
                .doNotPrependWorkingDirectory()
                .dontUseWindowsShell()
                .terminateOnMatch(terminateReadingOnString)
                .build();

        return runHammondReadCommandFlowContext;
    }

}
