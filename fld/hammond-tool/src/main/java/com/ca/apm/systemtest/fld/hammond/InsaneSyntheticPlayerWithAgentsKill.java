/*
 * Copyright (c) 2016 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */
package com.ca.apm.systemtest.fld.hammond;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class InsaneSyntheticPlayerWithAgentsKill {

    static class AgentRunnable implements Runnable {

        ArrayList<String> argumentList = new ArrayList<String>();
        private int groupDeadInterval;
        
        public AgentRunnable(ArrayList<String> argumentList, int groupDeadInterval) {
            this.argumentList = argumentList;
            this.groupDeadInterval = groupDeadInterval;
        }
        
        @Override
        public void run() {
            while (true) {
                try {
                    final Process process = new ProcessBuilder(argumentList).start();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            InputStream str = process.getErrorStream();
                            try {
                                while (str.read() != -1) {
                                    // nothing
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            InputStream str = process.getInputStream();
                            try {
                                while (str.read() != -1) {
                                    // nothing
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    process.waitFor();
                    Thread.sleep(groupDeadInterval);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
        
    }

    /**
     * Start as: java -cp target/hammond-99.99.metadata-SNAPSHOT-jar-with-dependencies.jar 
     * com.ca.apm.systemtest.fld.hammond.InsaneSyntheticPlayerWithAgentsKill 20 100 300000 1000000000 300000
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        int processCount = Integer.parseInt(args[0]);
        int agentsPerProcess = Integer.parseInt(args[1]);
        int groupLife = Integer.parseInt(args[2]);
        int groupDeadInterval = Integer.parseInt(args[3]);
        int startInterval = Integer.parseInt(args[4]);

        String javaHome = System.getProperty("java.home");
        final String javaLauncher = new File(javaHome, "bin/java").getPath();
        final String classPath = System.getProperty("java.class.path");
        final String currentClassName = "com.ca.apm.systemtest.fld.hammond.InsaneSyntheticPlayer";
        final ArrayList<String> argumentList = new ArrayList<String>();
        argumentList.add(javaLauncher);
        argumentList.add("-cp");
        argumentList.add(classPath);
        argumentList.add(currentClassName);
        
        for (int i = 0; i < processCount; i++) {
            ArrayList<String> argumentListCopy = new ArrayList<>(argumentList);
            argumentListCopy.add(Integer.toString(agentsPerProcess));
            argumentListCopy.add(Integer.toString(agentsPerProcess * i));
            argumentListCopy.add(Integer.toString(groupLife));
            new Thread(new AgentRunnable(argumentListCopy, groupDeadInterval)).start();
            
            Thread.sleep(startInterval);
        }

        Thread.sleep(Long.MAX_VALUE);
    }


}
