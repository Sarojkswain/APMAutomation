package com.ca.apm.systemtest.fld.plugin.powerpack.common;


import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.compress.archivers.ArchiveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import com.ca.apm.systemtest.fld.common.ACFileUtils;
import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.common.ProcessUtils2;
import com.ca.apm.systemtest.fld.plugin.AbstractPluginImpl;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.downloadMethod.ArtifactoryLiteDownloadMethod;
import com.ca.apm.systemtest.fld.plugin.downloadMethod.HttpDownloadMethod;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactManagerException;

/**
 * Default implementation for {@link PowerPackCommonPlugin}.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
@PluginAnnotationComponent(pluginType = PowerPackCommonPlugin.POWER_PACK_COMMON_PLUGIN_ID)
public class PowerPackCommonPluginImpl extends AbstractPluginImpl implements PowerPackCommonPlugin {
    private static final String RECREATE_SQL_SCRIPT_FILE_NAME = "stocktrader_trade6_tradedb_dbscript.sql";
    private static final String RECREATE_SQL_SCRIPT_ARCHIVE_NAME = "stocktrader_trade6_tradedb_dbscript.zip";
    private static final Logger LOGGER = LoggerFactory.getLogger(PowerPackCommonPluginImpl.class);
    
    @Autowired
    private HttpDownloadMethod httpDownloadMethod;

    @Autowired
    private ArtifactoryLiteDownloadMethod artifactoryLiteDownloadMethod;

    @ExposeMethod(description = "Re-creates Trade database which is used by such sample applications as StockTrader, Konakart, Trade6")
    @Override
    public int reCreateTradeDb(String sqlScriptUrl) {
        boolean download = false;
        File downloadDir = new File(PowerPackConstants.AGENT_DOWNLOAD_DIR_NAME);
        if (!downloadDir.exists()) {
            if (!downloadDir.mkdirs()) {
                String msg = MessageFormat.format("Exception while re-creating Trade Database: could not create download directory at ''{0}''", 
                    downloadDir);
                error(msg);
                throw new PowerPackPluginException(msg, 
                    PowerPackPluginException.ERR_RECREATE_TRADE_DB_FAILED);
            }
            download = true;
        }
        
        File sqlScriptFile = new File(downloadDir, RECREATE_SQL_SCRIPT_FILE_NAME);
        if (!download) {
            download = !sqlScriptFile.exists();
        }
        if (download) {
            //SQL script file is missing. First, check if we've got a zip archive left.
            File sqlScriptArchiveFile = new File(downloadDir, RECREATE_SQL_SCRIPT_ARCHIVE_NAME);
            if (sqlScriptArchiveFile.exists()) {
                download = false;
                //We've got the zip. Just extract it into the download folder.
                try {
                    ACFileUtils.unpackFile(sqlScriptArchiveFile, downloadDir);
                } catch (ArchiveException | IOException e) {
                    String msg = "Exception while re-creating Trade Database: failed to unpack archive with SQL script file";
                    error(msg, e);
                    throw new PowerPackPluginException(msg, e, 
                        PowerPackPluginException.ERR_RECREATE_TRADE_DB_FAILED);
                }
            }
        }
        
        if (download) {
            ArtifactFetchResult fetchResult;
            try {
                fetchResult = httpDownloadMethod.fetch(sqlScriptUrl, downloadDir, true);
            } catch (ArtifactManagerException e) {
                String msg = "Exception while re-creating Trade Database: failed to download SQL script archive artifact";
                error(msg, e);
                throw new PowerPackPluginException(msg, e, 
                    PowerPackPluginException.ERR_RECREATE_TRADE_DB_FAILED);
            }
            
            try {
                ACFileUtils.unpackFile(fetchResult.getFile(), downloadDir);
            } catch (ArchiveException | IOException e) {
                String msg = "Exception while re-creating Trade Database: failed to unpack archive with SQL script file";
                error(msg, e);
                throw new PowerPackPluginException(msg, e, PowerPackPluginException.ERR_RECREATE_TRADE_DB_FAILED);
            }
        }
        
        //sqlplus.exe TRADE/TRADE@tradedb @stocktrader_daytrader_export
        List<String> command = new ArrayList<>();
        command.add("sqlplus.exe");
        command.add("TRADE/TRADE@tradedb");
        command.add("@" + sqlScriptFile.getAbsolutePath());
        
        
        info(MessageFormat.format("Executing command: {0}", Arrays.toString(command.toArray())));
        
        ProcessExecutor pb = ProcessUtils2.newProcessExecutor(Slf4jStream.ofCaller().asDebug(), 
            Slf4jStream.ofCaller().asError())
            .command(command);
        StartedProcess process = ProcessUtils2.startProcess(pb);
        int exitCode = ProcessUtils2.waitForProcess(process, 30, TimeUnit.MINUTES, false);
        
        info(MessageFormat.format("sqlplus returned exit code: {0}", exitCode));
        info("Going to restart the oracle sql service ");
        restartTradeDB();
        return exitCode;
    }

    private void restartTradeDB() {
        String srvcName = "OracleServiceTRADEDB";
        String srvcNameTNSListnr = "OracleOraDb11g_home1TNSListener";

        info("Stopping services {0} {1}",srvcName,srvcNameTNSListnr);
        List<String> cmdDBServiceStop = new ArrayList<>();
        cmdDBServiceStop.add("net");
        cmdDBServiceStop.add("stop");
        cmdDBServiceStop.add(srvcName);
        processRestart(cmdDBServiceStop);

        List<String> cmdLstnrServiceStop = new ArrayList<>();
        cmdLstnrServiceStop.add("net");
        cmdLstnrServiceStop.add("stop");
        cmdLstnrServiceStop.add(srvcNameTNSListnr);
        processRestart(cmdLstnrServiceStop);

        info("Starting services {0} {1}",srvcName,srvcNameTNSListnr);
        List<String> cmdDBService = new ArrayList<>();
        cmdDBService.add("net");
        cmdDBService.add("start");
        cmdDBService.add(srvcName);
        processRestart(cmdDBService);

        List<String> cmdLstnrService = new ArrayList<>();
        cmdLstnrService.add("net");
        cmdLstnrService.add("start");
        cmdLstnrService.add(srvcNameTNSListnr);
        processRestart(cmdLstnrService);
    }

    private void processRestart(List<String> cmdDBService) {
        info(MessageFormat.format("Executing command: {0}", Arrays.toString(cmdDBService.toArray())));
        ProcessExecutor pb = ProcessUtils2.newProcessExecutor(Slf4jStream.ofCaller().asDebug(),
                Slf4jStream.ofCaller().asError())
                .command(cmdDBService);
        StartedProcess process = ProcessUtils2.startProcess(pb);
        int exitCode = ProcessUtils2.waitForProcess(process, 90, TimeUnit.SECONDS, false);
        info(MessageFormat.format("Service Restart exited with {0}", exitCode));
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    
}
