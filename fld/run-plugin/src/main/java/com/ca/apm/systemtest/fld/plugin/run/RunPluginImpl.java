package com.ca.apm.systemtest.fld.plugin.run;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.ProcessUtils;
import com.ca.apm.systemtest.fld.plugin.AbstractPluginImpl;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Plugin to control external processes.
 *
 * @author tavpa01
 */
public class RunPluginImpl extends AbstractPluginImpl implements RunPlugin {
    private static final String LOG_POSTFIX = ".log";
    private static final int NL_OFFSET = 200;

    private static final Logger log = LoggerFactory.getLogger(RunPluginImpl.class);
    private static final PropertyPlaceholderHelper resolver = new PropertyPlaceholderHelper("${",
        "}");
    private static final StandardEvaluationContext context = new StandardEvaluationContext();
    private static final PlaceholderResolver spelResolver = new SpelPlaceholderResolver();
    public static final int KIBIBYTE_BYTES = 1024;

    private List<CommandConfig> config;
    private Map<String, CommandConfig> processes = new HashMap<>(20);
    private HashMap<String, String> logFiles;
    private File logDir;

    // default is 4 hours
    @Value("${command.log.retain.millis:14400000}") //TODO - DM - fix with "4L*60*60*1000"
    long retainLogsMillis;

    @Value("${command.config.logfile:logfiles.txt}")
    String logsFileName;

    @Value("${command.config.file:commands.yaml}")
    Resource commandConfigFile;

    @Value("${command.config.logdir:.}")
    String logDirStr;


    @SuppressWarnings("unchecked")
    @Override
    @ExposeMethod(description = "Start new process")
    public String runProcess(String commandName, Map<String, Object> params) {
        log.debug("runProcess({}, {})", commandName, params);
        if (params == null) {
            params = new HashMap<>(1);
        }
        Environment curEnv = getEnv();
        CommandConfig selConfig = null;
        for (CommandConfig cfg : config) {
            if (commandName.equals(cfg.getName())
                && (cfg.getEnv() == null
                || cfg.getEnv() == curEnv)) {
                selConfig = cfg;
                break;
            }
        }

        String retVal = "ERROR: Command '" + commandName + "' does NOT exist";
        if (selConfig != null) {
            params.put("env", System.getenv());
            params.put("props", System.getProperties());
            context.setVariables(params);

            // Process selected configuration through SpringResolver

            List<String> originCmdLine = selConfig.getCmdLine();
            List<String> realCmdLine = new ArrayList<>(originCmdLine.size());
            for (String anOriginCmdLine : originCmdLine) {
                String propName = getCleanPlaceholderName(anOriginCmdLine);
                Object propVal = params.get(propName);
                if (propVal != null) {
                    if (propVal instanceof Collection) {
                        Collection<String> optList = (Collection<String>) propVal;
                        realCmdLine.addAll(optList);
                        continue;
                    } else if (propVal instanceof String[]) {
                        String[] optArray = (String[]) propVal;
                        realCmdLine.addAll(Arrays.asList(optArray));
                        continue;
                    }
                }
                realCmdLine.add(resolver.replacePlaceholders(anOriginCmdLine, spelResolver));
            }

            CommandConfig execCmd = new CommandConfig();
            execCmd.setName(selConfig.getName());
            execCmd.setCmdLine(realCmdLine);
            String realWorkingDir = resolver
                .replacePlaceholders(selConfig.getWorkingDir(), spelResolver);
            execCmd.setWorkingDir(realWorkingDir);
            if (selConfig.getExtractScript() != null) {
                String script = resolver
                    .replacePlaceholders(selConfig.getExtractScript(), spelResolver);
                execCmd.setExtractScript(script);
            }

            try {
                if (selConfig.getExtractScript() != null) {
                    extractScript(execCmd);
                }

                File logFile = File.createTempFile(commandName, LOG_POSTFIX, logDir);
                String uniqName = logFile.getName();
                uniqName = uniqName.substring(0, uniqName.length() - LOG_POSTFIX.length());
                ProcessBuilder pb = new ProcessBuilder()
                    .directory(new File(realWorkingDir))
                    .command(realCmdLine)
                    .redirectErrorStream(true)
                    .redirectOutput(logFile);
                execCmd.proc = ProcessUtils.startProcess(pb);
                processes.put(uniqName, execCmd);
                saveLogFiles();
                logFiles.put(uniqName, logFile.getPath());
                log.info("Command '{}' instance '{}' started", commandName, uniqName);
                retVal = uniqName;
            } catch (Exception e) {
                throw ErrorUtils
                    .logExceptionAndWrapFmt(log, e, "Error executing command {1} with error: {0}",
                        commandName);
            }

        }
        trySaveLogFiles();
        log.debug("runProcess() :: {}", retVal);
        return retVal;
    }

    private String getCleanPlaceholderName(CharSequence prop) {
        StringBuilder buffer = new StringBuilder(prop != null ? prop.length() : 1);
        for (int i = 0; prop != null && i < prop.length(); i++) {
            char ch = prop.charAt(i);
            if (ch != '$' && ch != '{' && ch != '}' && ch != '#' && ch != ' ') {
                buffer.append(ch);
            }
        }
        String result = buffer.toString();
        return result.trim();
    }

    private String extractScript(CommandConfig config) throws IOException {
        String exScript = null;
        String extractScript = config.getExtractScript();
        extractScript = extractScript.startsWith(File.separator) || extractScript.startsWith("/")
            ? extractScript : File.separator + extractScript;
        if (extractScript.length() > 0) {
            exScript = config.getWorkingDir() + File.separatorChar + (new File(extractScript))
                .getName();
            try (InputStream scStream = this.getClass().getResourceAsStream(extractScript);
                 OutputStream fos = new BufferedOutputStream(new FileOutputStream(exScript))) {
                if (scStream != null) {
                    IOUtils.copyLarge(scStream, fos);
                } else {
                    throw new IOException(
                        "Script '" + extractScript + "' not available on classpath");
                }
            }
            File scFile = new File(exScript);
            if (scFile.exists()) {
                scFile.setExecutable(true);
            }
        }
        return exScript;
    }

    @Override
    @ExposeMethod(description = "Stop specified process")
    public void stopProcess(String procId) {
        stopProcessInternal(procId, true);
    }

    @Override
    @ExposeMethod(description = "Return exit value of command or RunPlugin.STILL_RUNNING"
        + " when not finished or RunPlugin.NO_SUCH_PROCESS when process can not be identified")
    public long exitValue(String procId) {
        CommandConfig process = processes.get(procId);
        long retVal;
        if (process != null) {
            try {
                retVal = process.proc.exitValue();
                log.info("process {} exit with value {}", procId, retVal);
            } catch (IllegalThreadStateException itste) {
                retVal = RunPlugin.STILL_RUNNING;
            }
        } else {
            retVal = RunPlugin.NO_SUCH_PROCESS;
        }
        return retVal;
    }

    @Override
    @ExposeMethod(description = "Get latest N KiB of log file")
    public String getLog(String procId, int sizeKiB) {
        String logFileLoc = logFiles.get(procId);
        String retVal = "ERROR: log file for command instance '" + procId + "' does NOT exist";
        if (logFileLoc != null) {
            File logFile = new File(logFileLoc);
            if (!logFile.exists()) {
                retVal = "ERROR: Log file '" + logFile.getAbsolutePath() + "' does NOT exist";
            } else if (!logFile.isFile() || !logFile.canRead()) {
                retVal = "ERROR: Log file '" + logFile.getAbsolutePath() + "' is not readable file";
            } else {
                try (FileReader fReader = new FileReader(logFile)) {
                    long length = logFile.length();
                    int realSize = (sizeKiB * KIBIBYTE_BYTES) + NL_OFFSET;
                    long start = length - realSize;
                    if (start > 0) {
                        fReader.skip(start);
                        for (int i = 0; i < NL_OFFSET; i++) {
                            if (fReader.read() == '\n') {
                                break;
                            }
                        }
                    }
                    try (BufferedReader readLiner = new BufferedReader(fReader)) {
                        retVal = "";
                        String lastRead = readLiner.readLine();
                        while (retVal.length() < realSize && lastRead != null) {
                            retVal += lastRead + "\n";
                            lastRead = readLiner.readLine();
                        }
                    }
                } catch (IOException e) {
                    retVal = "ERROR: Error reading log file '" + logFile.getAbsolutePath() + "'";
                    log.error(retVal, e);
                }
            }
        }
        trySaveLogFiles();
        log.debug("getLog() :: {}", retVal);
        return retVal;
    }

    @Override
    @ExposeMethod(description = "List all actually running processes")
    public List<String> listRunningProcesses() {
        trySaveLogFiles();
        return new ArrayList<>(processes.keySet());
    }

    @Override
    @ExposeMethod(description = "Stop all processes")
    public void stopAllProcesses() {
        for (Iterator<String> procKeyIter = processes.keySet().iterator();
            procKeyIter.hasNext(); ) {
            String procKey = procKeyIter.next();
            stopProcessInternal(procKey, false);
            procKeyIter.remove();
        }
        trySaveLogFiles();
    }

    private void stopProcessInternal(String procId, boolean removeProcessId) {
        CommandConfig process = processes.get(procId);
        if (process != null) {
            process.proc.destroy();
            if (removeProcessId) {
                processes.remove(procId);
            }
            log.info("Command '{}' instance '{}' stopped", process.getName(), procId);
        } else {
            log.error("Command instance {} does NOT exist", procId);
        }

    }

    @PostConstruct
    void loadConfiguration() {
        loadConfiguration(commandConfigFile);
    }

    void loadConfiguration(Resource commandConfigFile) {
        if (log.isDebugEnabled()) {
            log.debug(">>>>>>>>>>>>>>>>>");
            log.debug("    JVM CWD: {}", (new File(".")).getAbsolutePath());
            Properties sysProps = System.getProperties();
            for (Object prop : new TreeSet<>(sysProps.keySet())) {
                log.debug("    {} = {}", prop, sysProps.getProperty((String) prop));
            }
            log.debug(">>>>>>>>>>>>>>>>>");
        }

        String commandConfigFileName = commandConfigFile.getFilename();
        JsonFactory factory = commandConfigFileName
            .endsWith(".yaml") ? new YAMLFactory() : new JsonFactory();
        factory.enable(Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        factory.enable(Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
        factory.enable(Feature.ALLOW_SINGLE_QUOTES);
        factory.enable(Feature.ALLOW_NUMERIC_LEADING_ZEROS);
        factory.enable(Feature.ALLOW_COMMENTS);
        factory.enable(Feature.ALLOW_YAML_COMMENTS);
        ObjectMapper mapper = new ObjectMapper(factory);
        TypeReference<List<CommandConfig>> typeRef = new ListTypeReference();
        try {
            config = mapper.readValue(commandConfigFile.getInputStream(), typeRef);
        } catch (Exception e) {
            log.error("Error reading configuration", e);
            config = new ArrayList<>();
        }

        readLogFiles();
    }

    private static class SpelPlaceholderResolver implements PlaceholderResolver {
        private static final ExpressionParser parser = new SpelExpressionParser();
        private static final Logger log = LoggerFactory.getLogger(SpelPlaceholderResolver.class);

        public SpelPlaceholderResolver() {
        }

        @Override
        public String resolvePlaceholder(String name) {
            Expression expression = parser.parseExpression(name);
            try {
                Object value = expression.getValue(context);
                return value != null ? value.toString() : null;
            } catch (Exception ex) {
                final String msg = MessageFormat
                    .format("Failed to resolve {0}. Exception: {1}", name, ex.getMessage());
                log.error(msg, ex);
                return null;
            }
        }

    }

    private synchronized void readLogFiles() {
        if (logFiles != null) {
            // Can be initialized only once
            return;
        }

        logFiles = new HashMap<>(10);
        File lFile = new File(logsFileName);
        if (lFile.isFile() && lFile.canRead()) {
            try (BufferedReader br = new BufferedReader(new FileReader(lFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    int eqOffset = line.indexOf('=');
                    String name = line.substring(0, eqOffset).trim();
                    String path = line.substring(eqOffset + 1).trim();
                    logFiles.put(name, path);
                }
                br.close();
                saveLogFiles();
            } catch (IOException e) {
                log.error("Error reading file '{}'", lFile.getAbsolutePath(), e);
            }
        } else {
            log.info("File '{}' can not be read - creating new one", lFile.getAbsolutePath());
        }

        // Align log directory
        logDir = new File(logDirStr);
        if (!logDir.isDirectory() || !logDir.canWrite()) {
            if (!logDir.mkdirs()) {
                log.error("Can not create log directory '{}' - using CWD",
                    logDir.getAbsolutePath());
                logDir = new File(".");
            }
        }
        log.info("Using log dir '{}'", logDir.getAbsolutePath());
    }

    long lastSave = 0;

    private void trySaveLogFiles() {
        if (lastSave < System.currentTimeMillis() - (retainLogsMillis / 2)) {
            saveLogFiles();
        }
    }

    private synchronized void saveLogFiles() {
        lastSave = System.currentTimeMillis();
        File lFile = new File(logsFileName);

        if ((lFile.isFile() && lFile.canWrite()) || !lFile.exists()) {
            try (FileWriter fileWriter = new FileWriter(lFile);
                 BufferedWriter bw = new BufferedWriter(fileWriter)) {
                long now = System.currentTimeMillis();
                Collection<String> removeKeys = new ArrayList<>(logFiles.size());
                for (String key : logFiles.keySet()) {
                    File logFile = new File(logFiles.get(key));
                    if (logFile.isFile() && logFile.canRead()) {
                        if (logFile.lastModified() < now - retainLogsMillis) {
                            if (logFile.delete()) {
                                removeKeys.add(key);
                                log.debug("Removing log file for command instance '{}'", key);
                            } else {
                                log.error("Can not remove retained log file '{}'",
                                    logFile.getAbsolutePath());
                            }
                        } else {
                            bw.write(key + "=" + logFiles.get(key) + "\n");
                        }
                    } else {
                        log.error("Log file '{}' was removed by somebody else",
                            logFile.getAbsolutePath());
                        removeKeys.add(key);
                    }
                }
                for (String key : removeKeys) {
                    logFiles.remove(key);
                }
            } catch (IOException e) {
                log.error("Error writing to file '{}'. Exception: {}", lFile.getAbsolutePath(),
                    e.getMessage());
            }
        } else {
            log.error("File '{}' can not be written", lFile.getAbsolutePath());
        }
    }

    private static Environment curEnv = null;

    private Environment getEnv() {
        if (curEnv == null) {
            if (SystemUtils.IS_OS_UNIX) {
                curEnv = Environment.Unix;
            } else if (SystemUtils.IS_OS_WINDOWS) {
                curEnv = Environment.Windows;
            } else {
                curEnv = Environment.Unknown;
            }
        }
        return curEnv;
    }

    private static class ListTypeReference extends TypeReference<List<CommandConfig>> {
    }
}