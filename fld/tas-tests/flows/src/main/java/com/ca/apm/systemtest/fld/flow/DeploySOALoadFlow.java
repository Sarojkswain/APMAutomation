/**
 * 
 */
package com.ca.apm.systemtest.fld.flow;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;

/**
 * @author keyja01
 *
 */
@Flow
public class DeploySOALoadFlow extends FlowBase implements IAutomationFlow {
    /** Wurlitzer base install directory */
    private File baseDir;
    /** ${wurlitzer}/scripts */
    private File scriptsDir;
    private File groovyDir;
    /** ${wurlitzer}/scripts/config */
    private File configDir;
    /** ${wurlitzer}/scripts/testcase/sample */
    private File testcaseDir;
    
    @FlowContext
    private DeploySOALoadFlowContext ctx;

    /* (non-Javadoc)
     * @see com.ca.apm.automation.action.core.IAutomationFlow#run()
     */
    @Override
    public void run() throws Exception {
        baseDir = new File(ctx.wurlitzerBaseDir);
        scriptsDir = new File(baseDir, "scripts");
        groovyDir = new File(scriptsDir, "groovy");
        configDir = new File(groovyDir, "config");
        File tc = new File(groovyDir, "testcase");
        testcaseDir = new File(tc, "sample");
        
        createSoaConfig();
        installJaxGroovyFiles();
        installBatchFile();
        installStopBatchFile();
        installBuildFile();
        // create the jax_*.groovy files

    }
    
    
    private void installBuildFile() throws Exception {
        InputStream in = getClass().getResourceAsStream("/wls-soa-load/soa-load.xml");
        FileUtils.copyInputStreamToFile(in, new File(groovyDir, "soa-load.xml"));
    }


    private void installBatchFile() throws Exception {
        InputStream in = getClass().getResourceAsStream("/wls-soa-load/soa.bat");
        File dest = new File(groovyDir, "soa.bat");
        Map<String, String> mods = new HashMap<String, String>();
        mods.put("%%JAVA_HOME%%", ctx.javaHome);
        mods.put("%%GROOVY_HOME%%", ctx.groovyHome);
        mods.put("%TITLE%", ctx.batchFileTitle);
        mods.put("%KILL_FILE%", ctx.killFile);
        deployFile(in, dest, mods);
    }


    private void installStopBatchFile() throws Exception {
        InputStream in = getClass().getResourceAsStream("/wls-soa-load/soa-stop.bat");
        File dest = new File(groovyDir, "soa-stop.bat");
        Map<String, String> mods = new HashMap<String, String>();
        mods.put("%KILL_FILE%", ctx.killFile);
        deployFile(in, dest, mods);
    }


    private void installJaxGroovyFiles() throws Exception {
        String[] src = new String[] {"jax_1.groovy", "jax_2.groovy", "jax_3.groovy"};
        for (String filename: src) {
            InputStream in = getClass().getResourceAsStream("/wls-soa-load/" + filename);
            File outputFile = new File(testcaseDir, filename);
            FileUtils.copyInputStreamToFile(in, outputFile);
        }
    }
    

    private void createSoaConfig() throws Exception {
        InputStream in = getClass().getResourceAsStream("/wls-soa-load/soa.groovy");
        logger.info("Using resource for SOA config: " + in);
        File outputFile = new File(configDir, "soa.groovy");
        Map<String, String> mods = new HashMap<String, String>();
        mods.put("%WURLITZER1%", ctx.wurlitzer1HostName + ":" + ctx.wurlitzer1Port);
        mods.put("%WURLITZER2%", ctx.wurlitzer2HostName + ":" + ctx.wurlitzer2Port);
        deployFile(in, outputFile, mods);
    }

    
    private void deployFile(InputStream in, File outputFile, Map<String, String> mods) throws Exception {
        if (mods == null) {
            mods = Collections.emptyMap();
        }
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(in));
        String line = null;
        PrintWriter out = new PrintWriter(outputFile);
        while ((line = reader.readLine()) != null) {
            for (Entry<String, String> entry: mods.entrySet()) {
                line = line.replace(entry.getKey(), entry.getValue());
            }
            out.println(line);
        }
        out.flush();
        out.close();
    }
    
    
    public static void main(String[] args) {
        String line = "set JAVA_HOME=%%JAVA_HOME%%";
        String regexp = "%%JAVA_HOME%%";
        String value = "c:\\java\\jdk1.7\\jre";
        line = line.replace(regexp, value);
        System.out.println(line);
    }
}
