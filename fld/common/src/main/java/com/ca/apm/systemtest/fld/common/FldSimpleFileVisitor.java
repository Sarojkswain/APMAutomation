package com.ca.apm.systemtest.fld.common;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FldSimpleFileVisitor extends SimpleFileVisitor<Path> {
    
    private static final Logger logger = LoggerFactory.getLogger(FldSimpleFileVisitor.class);
    
    PathMatcher matcherTXT;
    PathMatcher matcherXML;
    PathMatcher matcherProperties;
    PathMatcher matcher_file_agent_host_names_txt;
    String searchForFile;

    static final String AGENT_HOST_NAMES_TXT_FILE = "agent-host-names.txt";
    
    
    public FldSimpleFileVisitor(String searchForFile) {
        this.searchForFile = searchForFile;
        matcherTXT = FileSystems.getDefault().getPathMatcher("glob:*.txt");
        matcherXML = FileSystems.getDefault().getPathMatcher("glob:*.xml");
        matcherProperties = FileSystems.getDefault().getPathMatcher("glob:*.properties");
    }


    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (matcherTXT.matches(file.getFileName())) {
            if (!attrs.isDirectory()) {
                logger.debug("Found text file: {}", file);
                changeTxtContent(file.toFile());
            }
        }

        return FileVisitResult.CONTINUE;
    }
    

    
    /**
     * General method to do anything with specific found file
     * 
     * @param file 
     */
    public void changeTxtContent(File file) {

        if (file.getName().equals(searchForFile)) {
              switch (searchForFile) {
                case AGENT_HOST_NAMES_TXT_FILE:
                    changeAgentHostNamesContent(file);
                    break;
                default:
                    break;
            }
        }
    }
    
    
    
    /**
     * Replace content in 'agent-host-names.txt' file
     * 
     * @param agentHostName
     */
    public void changeAgentHostNamesContent(File agentHostName) {
        StringBuilder content = new StringBuilder(256);
        
        for (int i = 1; i < 11; i++) {
            content.append("host");
            content.append(i);
            content.append(".ca.com");
            content.append(System.getProperty("line.separator"));
        }
        
        try {
            //Files.write(agentHostName.toPath(), content.toString().getBytes(), StandardOpenOption.CREATE);
            RandomAccessFile accessFile = new RandomAccessFile(agentHostName, "rw");
            accessFile.getChannel().truncate(0);
            accessFile.write(content.toString().getBytes(), 0, content.length());
            accessFile.close();

            logger.debug("Changed content in: {}", agentHostName);

        } catch (IOException e) {
            ErrorUtils.logExceptionFmt(logger, e, "Error in write file: {1}. Exception: {0}",
                agentHostName);
        } 
    }
    
    
    
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
//       if (matcherTXT.matches(dir.getFileName())){
//        if(dir.getFileName().equals(searchFile)){
//            return FileVisitResult.SKIP_SIBLINGS;
//        }
//      } 
       return FileVisitResult.CONTINUE;
    }

    
    
    public String getSearchForFile() {
        return searchForFile;
    }

    public void setSearchForFile(String searchForFile) {
        this.searchForFile = searchForFile;
    }
}
