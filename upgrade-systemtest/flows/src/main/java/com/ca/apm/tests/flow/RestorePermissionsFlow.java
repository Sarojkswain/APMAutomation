package com.ca.apm.tests.flow;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.commandline.Execution;
import org.apache.commons.lang.StringUtils;
import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This flow restores Linux file permissions from DCU into installed EM and Hammond
 *
 * @author jirji01
 */
@Flow
public class RestorePermissionsFlow extends FlowBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestorePermissionsFlow.class);
    private static final String SET_PERMISSION_SH = "setPermission.sh";

    @FlowContext
    private RestorePermissionsFlowContext flowContext;

    private FileRecord emPid;
    private FileRecord emExe;

    @Override
    public void run() throws Exception {

        List<FileRecord> files = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(flowContext.data.emFileListing))) {

            String folder = "/";

            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("/")) {
                    folder = line.substring(0, line.lastIndexOf(':'));
                    br.readLine();
                    br.readLine();
                    br.readLine();
                }
                else if (! line.startsWith("l")){
                    FileRecord fr = parseFileRecord(folder, line);
                    if (fr != null) {
                        files.add(fr);
                    }
                }
            }
        }

        String currentUser = System.getProperty("user.name");
        String customerUser = null;

        // add group into system
        String group = null;
        if (emPid != null) {
            customerUser = emPid.owner;
            group = emPid.group;
        } else if (emExe != null) {
            customerUser = emExe.owner;
            group = emExe.group;
        }

        if (StringUtils.isNotBlank(group) && StringUtils.isNotBlank(customerUser)) {

            if (currentUser.equalsIgnoreCase("root")) {
                new Execution.Builder("groupadd", LOGGER)
                        .args(Collections.singletonList(group))
                        .workDir(Paths.get(flowContext.data.emFileListing).getParent().toFile())
                        .build()
                        .go();

                new Execution.Builder("usermod", LOGGER)
                        .args(Arrays.asList("-a", "-G", group, currentUser))
                        .workDir(Paths.get(flowContext.data.emFileListing).getParent().toFile())
                        .build()
                        .go();

                prepareBatFile(files, currentUser, customerUser);

                new Execution.Builder("./" + SET_PERMISSION_SH, LOGGER)
                        .workDir(Paths.get(flowContext.data.emFileListing).getParent().toFile())
                        .build()
                        .go();
            } else {
                new Execution.Builder("sudo", LOGGER)
                        .args(Arrays.asList("groupadd", group))
                        .workDir(Paths.get(flowContext.data.emFileListing).getParent().toFile())
                        .build()
                        .go();

                new Execution.Builder("sudo", LOGGER)
                        .args(Arrays.asList("usermod", "-a", "-G", group, currentUser))
                        .workDir(Paths.get(flowContext.data.emFileListing).getParent().toFile())
                        .build()
                        .go();

                prepareBatFile(files, currentUser, customerUser);

                new Execution.Builder("sudo", LOGGER)
                        .args(Collections.singletonList("./" + SET_PERMISSION_SH))
                        .workDir(Paths.get(flowContext.data.emFileListing).getParent().toFile())
                        .build()
                        .go();
            }
        }
    }

    private void prepareBatFile(List<FileRecord> files, String currentUser, String customerUser) {
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(Paths.get(flowContext.data.emFileListing).getParent().toAbsolutePath().toString(), SET_PERMISSION_SH))){

            bw.write("#!/bin/sh");
            bw.newLine();

            for (FileRecord fr : files) {

                String file = fr.path.toAbsolutePath().toString();

                bw.write("chmod ");
                bw.write(fr.mode);
                bw.write(" ");
                bw.write(file);
                bw.newLine();

                bw.write("chown ");
                bw.write(fr.owner.equals(customerUser) ? currentUser : fr.owner);
                bw.write(" ");
                bw.write(file);
                bw.newLine();

                bw.write("chgrp ");
                bw.write(fr.group);
                bw.write(" ");
                bw.write(file);
                bw.newLine();

                bw.newLine();
            }

            new Execution.Builder("chmod", LOGGER)
                    .args(Arrays.asList("a+x", SET_PERMISSION_SH))
                    .workDir(Paths.get(flowContext.data.emFileListing).getParent().toFile())
                    .build()
                    .go();

        } catch (IOException e) {
            LOGGER.error("Cannot write shell script", e);
        } catch (InterruptedException e) {
            LOGGER.error("Cannot change shell script permissions", e);
        }
    }

    private FileRecord parseFileRecord(String folder, String line) {
        String[] split = line.split(" +");

        if (split.length < 8) { return null; }

        FileRecord record = new FileRecord();

        record.mode = parseMode(split[0]);
        record.owner = split[2];
        record.group = split[3];

        StringBuilder fileName = new StringBuilder();
        for (int i = 8; i < split.length; i++) {

            if (i > 8) {
                fileName.append(" ");
            }
            fileName.append(split[i]);
        }

        if (StringUtils.isNotBlank(flowContext.data.baseFolder)) {
            record.path = Paths.get(flowContext.data.baseFolder, folder, fileName.toString());
        } else {
            record.path = Paths.get(folder, fileName.toString());
        }


        if (fileName.toString().equals("em.pid")) {
            emPid = record;
        } else if (fileName.toString().equals("Introscope_Enterprise_Manager")) {
            emExe = record;
        }

        return record;
    }

    private String parseMode(String mode) {
        Args.check(mode.length() == 10 || mode.length() == 11, "invalid mode string");

        int sticky = 0;
        int owner = 0;
        int group = 0;
        int other = 0;

        if (mode.charAt(1) == 'r') {
            owner |= 4;
        }
        if (mode.charAt(2) == 'w') {
            owner |= 2;
        }
        if (mode.charAt(3) == 'x') {
            owner |= 1;
        } else if (mode.charAt(3) == 'S') {
            sticky |=4;
        } else if (mode.charAt(3) == 's') {
            owner |= 1;
            sticky |=4;
        }
        if (mode.charAt(4) == 'r') {
            group |= 4;
        }
        if (mode.charAt(5) == 'w') {
            group |= 2;
        }
        if (mode.charAt(6) == 'x') {
            group |= 1;
        } else if (mode.charAt(6) == 'S') {
            sticky |=2;
        } else if (mode.charAt(6) == 's') {
            group |= 1;
            sticky |=2;
        }
        if (mode.charAt(7) == 'r') {
            other |= 4;
        }
        if (mode.charAt(8) == 'w') {
            other |= 2;
        }
        if (mode.charAt(9) == 'x') {
            other |= 1;
        } else if (mode.charAt(9) == 'T') {
            sticky |=1;
        } else if (mode.charAt(9) == 't') {
            other |= 1;
            sticky |=1;
        }

        return Integer.toString(sticky) + Integer.toString(owner) + Integer.toString(group) + Integer.toString(other);
    }

    private class FileRecord {
        String mode;
        String owner;
        String group;
        Path path;
    }

    public static void main(String[] args) throws Exception {
        RestorePermissionsFlow rp = new RestorePermissionsFlow();

        RestorePermissionsFlowContext.Data data = new RestorePermissionsFlowContext.Data();

        data.baseFolder = "/";
        data.emFileListing = "c:\\em-file-listing.txt";

        rp.flowContext = new RestorePermissionsFlowContext(data);
        rp.run();
    }
}
