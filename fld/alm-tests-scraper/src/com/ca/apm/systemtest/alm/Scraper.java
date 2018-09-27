package com.ca.apm.systemtest.alm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.TreeSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.spi.RootLogger;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.alm.data.ALMContext;
import com.ca.apm.systemtest.alm.data.ALMEntity;
import com.ca.apm.systemtest.alm.data.utility.ALMUtilities;
import com.ca.testing.almclient.AuthHeadersRequestFilter;
import com.ca.testing.almclient.RestWrapper;
import com.ca.testing.almclient.api.AlmClientApi;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.query.QueryFactory;
import com.googlecode.cqengine.resultset.ResultSet;

/**
 * ALM scraper.
 */
public class Scraper {
    static Logger log = LoggerFactory.getLogger(Scraper.class);

    static void logEntity(ALMEntity entity) {
        Map<String, String> fields = entity.getFields();
        String name = null;
        if ((name = fields.get("name")) != null) {
            log.info("name: {}", name);
        }
        Collection<String> names = new TreeSet<>(fields.keySet());
        names.remove("name");
        for (String key : names) {
            log.info("{}: {}", key, fields.get(key));
        }
    }


    static Collection<ALMEntity> getAllSubTestFolders(ALMContext context, int rootFolderId) {
        Collection<ALMEntity> subFolders = new TreeSet<>(new ALMEntity.CompareByField("id"));
        Queue<ALMEntity> queue = new LinkedList<>();
        IndexedCollection<ALMEntity> folders = context.getTestFoldersCol();
        ResultSet<ALMEntity> res = folders.retrieve(QueryFactory.equal(ALMEntity.ID, rootFolderId));
        queue.add(res.uniqueResult());
        res.close();
        while (!queue.isEmpty()) {
            ALMEntity folder = queue.remove();
            Integer folderId = folder.getFieldIntValue("id");
            subFolders.add(folder);
            ResultSet<ALMEntity> nextLevel = folders
                .retrieve(QueryFactory.equal(ALMEntity.PARENT_ID, folderId));
            Iterables.addAll(queue, nextLevel);
            nextLevel.close();
        }

        return subFolders;
    }


    static Collection<ALMEntity> getAllTestsFromFolders(ALMContext context,
        Iterable<ALMEntity> folders) {
        Collection<ALMEntity> tests = new TreeSet<>(new ALMEntity.CompareByField("id"));
        IndexedCollection<ALMEntity> testsCol = context.getTestsCol();
        for (ALMEntity folder : folders) {
            Integer folderId = folder.getFieldIntValue("id");
            Iterable<ALMEntity> folderTests = testsCol
                .retrieve(QueryFactory.equal(ALMEntity.PARENT_ID, folderId));
            Iterables.addAll(tests, folderTests);
        }

        return tests;
    }


    static Collection<ALMEntity> getTestsFromSubFolders(ALMContext context, int rootFolderId) {
        Collection<ALMEntity> subFolders = getAllSubTestFolders(context, rootFolderId);
        return getAllTestsFromFolders(context, subFolders);
    }


    private String almUser = System.getProperty("user.name", System.getenv("USERNAME"));
    private String almHost = "alm11.ca.com";
    private String almUserPassword = "";
    private String almDomain = "APM";
    private String almProject = "APM";
    private Collection<String> dataFiles = Collections.emptyList();
    private String outputFile = "result.json";

    private CommandLine commandLine;
    private ALMContext context;


    /**
     * Command line parameters parsing setup.
     *
     * @return prepared options parser
     */
    private Options prepareOptionsParser() {
        Options cliOpts = new Options();

        Option optHelp = OptionBuilder
            .withDescription("This help")
            .isRequired(false)
            .withLongOpt("help")
            .create('?');
        cliOpts.addOption(optHelp);

        Option optHost = OptionBuilder
            .withDescription("ALM host name [" + almHost + "]")
            .isRequired(false)
            .hasArg()
            .withArgName("EM host")
            .withLongOpt("host")
            .create('h');
        cliOpts.addOption(optHost);

        Option optUser = OptionBuilder
            .withDescription("ALM user name [" + almUser + "]")
            .isRequired(false)
            .hasArg()
            .withArgName("user name")
            .withLongOpt("user")
            .create('u');
        cliOpts.addOption(optUser);

        Option optPassword = OptionBuilder
            .withDescription("ALM user password [" + almUserPassword + "]")
            .isRequired(false)
            .hasArg()
            .withArgName("password phrase")
            .withLongOpt("password")
            .create('s');
        cliOpts.addOption(optPassword);

        Option optDomain = OptionBuilder
            .withDescription("ALM domain [" + almDomain + "]")
            .isRequired(false)
            .hasArg()
            .withArgName("domain")
            .withLongOpt("domain")
            .create('d');
        cliOpts.addOption(optDomain);

        Option optProject = OptionBuilder
            .withDescription("ALM project [" + almProject + "]")
            .isRequired(false)
            .hasArg()
            .withArgName("project")
            .withLongOpt("project")
            .create('p');
        cliOpts.addOption(optProject);

        Option optFile = OptionBuilder
            .withDescription("JSON data file")
            .isRequired(false)
            .hasArgs()
            .withArgName("file")
            .withLongOpt("file")
            .create('f');
        cliOpts.addOption(optFile);

        Option optOutputFile = OptionBuilder
            .withDescription("JSON output file")
            .isRequired(false)
            .hasArgs()
            .withArgName("file")
            .withLongOpt("output-file")
            .create('o');
        cliOpts.addOption(optFile);

        Option optLoggingConfiguration = OptionBuilder
            .withDescription("Logging configuration file")
            .isRequired(false)
            .hasArg()
            .withArgName("file")
            .withLongOpt("logging-configuration")
            .create('L');
        cliOpts.addOption(optLoggingConfiguration);

        Option optGetAllSubfolderTests = OptionBuilder
            .withDescription("Get all tests of under given folder and subfolders")
            .isRequired(false)
            .withLongOpt("get-all-subfolder-tests")
            .create();
        cliOpts.addOption(optLoggingConfiguration);

        Option optVerbose = OptionBuilder
            .withDescription("Verbose output")
            .isRequired(false)
            .withLongOpt("verbose")
            .create('v');
        cliOpts.addOption(optVerbose);

        return cliOpts;
    }


    private void evaluateCommandLine(String[] args) throws ParseException {
        CommandLineParser parser = new GnuParser();
        Options cliOpts = prepareOptionsParser();
        try {
            commandLine = parser.parse(cliOpts, args);
            assert commandLine != null;
        } catch (ParseException e) {
            log.error("command line parsing error", e);
            System.exit(2);
        }

        if (commandLine.hasOption('?')) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("scraper -h host -u user -s password -p project -d domain [...]",
                cliOpts);
            System.exit(0);
        }

        almHost = commandLine.getOptionValue("host", almHost);
        almUser = commandLine.getOptionValue("user", almUser);
        almUserPassword = commandLine.getOptionValue("password", almUserPassword);
        outputFile = commandLine.getOptionValue("output-file", outputFile);

        if (commandLine.hasOption("file")) {
            dataFiles = new ArrayList<>(Arrays.asList(commandLine.getOptionValues("file")));
            log.debug("data files to read: {}", dataFiles);
        }

        if (commandLine.hasOption("logging-configuration")) {
            PropertyConfigurator.configure(commandLine.getOptionValue("logging-configuration"));
        }
    }


    void readDataFiles() throws IOException {
        int entityCount = 0;
        for (String file : dataFiles) {
            Collection<ALMEntity> loadedEntities = ALMUtilities.readEntitiesFromJsonFile(file);
            for (ALMEntity entity : loadedEntities) {
                context.addEntity(entity);
            }
            log.info("{} entities read back from {}", loadedEntities.size(), file);
            entityCount += loadedEntities.size();
        }
        log.info("{} entities loaded in total from {} file(s)", entityCount, dataFiles.size());
    }


    public Scraper() {
    }


    void run(String[] args) throws Exception {
        BasicConfigurator.configure();

        evaluateCommandLine(args);

        RollingFileAppender rfa = new RollingFileAppender();
        rfa.setFile(Scraper.class.getSimpleName() + ".log");
        rfa.setMaxFileSize("10MB");
        rfa.setMaxBackupIndex(10);
        rfa.setLayout(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c:%L - %m%n"));
        rfa.activateOptions();
        RootLogger.getRootLogger().addAppender(rfa);
        org.apache.log4j.Logger wire = org.apache.log4j.Logger.getLogger("org.apache.http.wire");
        wire.setLevel(Level.INFO);

        if (!dataFiles.isEmpty()) {
            context = new ALMContext();
            readDataFiles();
        } else {
            ResteasyClient resteasyClient = new ResteasyClientBuilder().build();
            resteasyClient.register(new AuthHeadersRequestFilter(almUser, almUserPassword));
            ResteasyWebTarget target = resteasyClient.target("http://" + almHost + "/qcbin");
            RestWrapper wrapper = new RestWrapper(target.proxy(AlmClientApi.class), almUser,
                almDomain, almProject);

            context = new ALMContext(wrapper);
            context.fetch();

            IndexedCollection<ALMEntity> testFolders = context.getTestFoldersCol();
            ALMUtilities.writeEntitiesIntoJsonFile("testFolders.json", testFolders);

            IndexedCollection<ALMEntity> tests = context.getTestsCol();
            ALMUtilities.writeEntitiesIntoJsonFile("tests.json", tests);
        }

        ALMEntity webViewFolder = context.getTestFoldersCol()
            .retrieve(QueryFactory.equal(ALMEntity.NAME, "WebView - New Thin UI")).uniqueResult();

        Integer webViewFolderId = webViewFolder.getFieldIntValue("id");
        Collection<ALMEntity> result = getTestsFromSubFolders(context, webViewFolderId);
        result = Collections2.filter(result, new Predicate<ALMEntity>() {
            @Override
            public boolean apply(ALMEntity input) {
                return input.getFieldValue("user-01").equalsIgnoreCase("AUTOMATED");
            }
        });
        ALMUtilities.writeEntitiesIntoJsonFile(outputFile, result);
        log.debug("written {} entities into {} file", result.size(), outputFile);
    }

    public static void main(String[] args) throws Exception {
        new Scraper().run(args);
        System.exit(0);
    }
}
