package com.ca.apm.systemtest.fld.agent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.Scheduled;

public class AgentUpdateCleaner {

    @Scheduled(initialDelay = 10000, fixedDelay = 2500000000L)
    public void cleanup() throws IOException {
        File rootFolder = new File(".");
        String[] zip = {"zip"};

        for (File file : FileUtils.listFiles(rootFolder, zip, false)) {
            file.delete();
        }

        ArrayList<File> dirs = new ArrayList<>();
        File[] files = rootFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() && file.getName().startsWith("agent-") && !file.getName()
                    .equalsIgnoreCase("agent-current")) {
                    dirs.add(file);
                }
            }
        }

        Collections.sort(dirs);

        for (int i = 0; i < dirs.size() - 1; i++) {
            FileUtils.deleteDirectory(dirs.get(i));
        }
    }
}
