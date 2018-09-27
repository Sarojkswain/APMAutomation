package com.ca.apm.tests.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class FetchPerformamanceMetrics {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public ArrayList<Double> vmList = new ArrayList<Double>();
    public ArrayList<Double> rssList = new ArrayList<Double>();
    public ArrayList<Double> cpuList = new ArrayList<Double>();
    public ArrayList<Double> memList = new ArrayList<Double>();
    public ArrayList<String> dateList = new ArrayList<String>();



    private final String user = "root";
    private final String password = "Lister@123";
    private String processRegex = "Introscope_Enterprise_Manager";
    private String processId;
    private int TEST_DURATION; // IN SECONDS, perf metrics
                               // collection runs for
    // this duration
    private final String RESULTS_LOCATION = "/opt/Results/";
    private BufferedWriter bw;
    private FileWriter fw;



    public void performanceMetricsCollection(String machine, String machineHeader, int testDuration)
        throws IOException {
        this.TEST_DURATION = testDuration;
        log.info(" Collecting Perf Metrics from " + machineHeader + "(" + machine + ") ...");
        SimpleDateFormat format = new SimpleDateFormat("M-d_HHmmss");
        File file =
            new File(RESULTS_LOCATION + machineHeader.replace(" ", "") + "_PerformanceTestResults_"
                + format.format(Calendar.getInstance().getTime()) + ".log");

        try {


            fw = new FileWriter(file, true);

            bw = new BufferedWriter(fw);
            bw.write("\n " + machineHeader + " Machine Details  \n");

            bw.write("--------------------------------\n");

            bw.write("|");

            bw.write("--> OS Details\n");

            String osDetailsCommand =
                "uname -a | awk '{print \"OS : \" $1,\"\\nMachine : \"$2, \"\\nKernel : \"$3}'";

            jschMetricsConnection(machine, user, password, osDetailsCommand, "MachineDetails");
            bw.write("|");

            bw.write("--> CPU Details\n");


            String cpuDetailsCommand = "lscpu | egrep 'Thread|Core|Socket|^CPU\\('";
            jschMetricsConnection(machine, user, password, cpuDetailsCommand, "MachineDetails");

            bw.write("|\n");

            bw.write("--> Disk Details\n");

            String diskCommand = "lsblk -l";
            jschMetricsConnection(machine, user, password, diskCommand, "MachineDetails");
            bw.write("|\n");

            bw.write("--> Disk Space Details \n");

            String freeDiskSpaceCommand =
                "df -h | awk '!$2{getline x;$0=$0 x}{printf \"%-35s %10s %6s %6s %4s %s\\n\",$1,$2,$3,$4,$5,$6}'";
            jschMetricsConnection(machine, user, password, freeDiskSpaceCommand, "MachineDetails");

            bw.write("|\n");

            bw.write("--> Physical Memory Details \n");

            String physicalMemoryDetailsCommand = "vmstat -s -S M | grep memory";
            jschMetricsConnection(machine, user, password, physicalMemoryDetailsCommand,
                "MachineDetails");


            // $5 VM , $6 RSS , $9 %CPU , $10 %mem, $11 time

            String processCommand =
                "ps aux | grep " + processRegex + " | grep -v \"grep\" | awk '{print $2}'";



            jschMetricsConnection(machine, user, password, processCommand, "ProcessId");


            String command =
                "top -n1 -b | grep " + processId + " | awk '{print $5, $6 ,$9, $10, $11}'";

            // Basic Performance Metrics Collection STARTS
            // to increase test duration change 100 (loop count) ~ 100 seconds
            String loopCommand =
                "rm -rf " + new String(RESULTS_LOCATION.replace("lts/", "lts")) + "; mkdir "
                    + RESULTS_LOCATION + "; cd " + RESULTS_LOCATION + "; for i in {1.."
                    + TEST_DURATION + "}; do " + command + " ; done > top.log & ";

            jschMetricsConnection(machine, user, password, loopCommand, "BasicPerfomaceMetrics");
            // Basic Performance Metrics Collection ENDS

            // Page Faults Metrics collection STARTS
            final String pageFaultsCommand =
                "cd " + RESULTS_LOCATION + "; sar -B 1 " + TEST_DURATION
                    + " | grep Average | awk '{print  $2,  $3,  $4,  $5}' > pagefaults.log &";

            jschMetricsConnection(machine, user, password, pageFaultsCommand, "PageFaults");
            // Page Faults Metrics collection ENDS

            // Disk IO Metrics collection STARTS
            String processIODiskRateCommand =
                "cd " + RESULTS_LOCATION + "; pidstat -p " + processId + " -dl  1 " + TEST_DURATION
                    + " | grep Average | awk '{print $3,$4,$5}' > pidstat.log & ";



            jschMetricsConnection(machine, user, password, processIODiskRateCommand, "DiskIO");
            // Disk IO Metrics collection ENDS

            Thread.sleep(TEST_DURATION * 1000 + 30000);



            // Basic Performance metrics writing STARTS
            bw.write("\n\n  BASIC PERFORMANCE METRICS  \n");
            metricLine(119); // +30
            bw.write("\n|\tVirtual Memory\t|\tRSS\t|\tAvg. CPU\t|\tAvg. Mem\t|\n");
            metricLine(119);
            jschMetricsConnection(machine, user, password, "readFiles", "top");
            bw.write("\n");

            bw.write("|\t" + (double) Math.round(getAvgVirtualMemoryUtilization() * 100) / 100
                + " MB\t|" + "    " + (double) Math.round(getAvgRssUtilization() * 100) / 100
                + " MB     |" + "\t" + (double) Math.round(getAvgCPUUtilization() * 100) / 100
                + "%\t\t|" + "\t" + (double) Math.round(getAvgMemoryUtilization() * 100) / 100
                + "%\t\t|\n");
            metricLine(119);

            // Basic Performance metrics writing ENDS

            // Page Faults Metrics Writing STARTS
            bw.write(" \n\n  SYSTEM PAGE FAULTS METRICS  \n");
            metricLine(150); // +45
            bw.write("\n|\tKBs PagedIn/sec\t|  KBs PagedOut/sec\t|\tFaults/sec\t|\tMajor Faults/sec\t|\n");
            metricLine(150);
            jschMetricsConnection(machine, user, password, "readFiles", "pagefaults");

            bw.write("\n");
            metricLine(150);
            // Page Faults Metrics Writing ENDS

            // Disk IO Metrics STARTS
            bw.write(" \n\n  PROCESS AVERAGE IO DISK METRICS  \n");
            metricLine(108); // 27
            bw.write("\n|\tKBs Reads/sec\t|  KBs Writes/sec\t|\tWrites Cancelled/sec\t|\n");
            metricLine(108);
            bw.write("\n");
            jschMetricsConnection(machine, user, password, "readFiles", "pidstat");
            bw.write("\n");
            metricLine(108);

            // Disk IO Metrics ENDS

            bw.close();


        } catch (IOException e) {

            e.printStackTrace();
        }

        catch (Exception e) {

            e.printStackTrace();
        }


    }


    public void metricLine(int length) throws IOException {
        for (int i = 0; i < length; i++) {
            bw.write("-");

        }

    }

    public void jschMetricsConnection(String machine, String user, String password, String command,
        String perfMetric) {
        try {
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, machine, 22);
            session.setPassword(password);
            session.setConfig(config);
            session.connect();


            if (command.equals("readFiles")) {
                ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
                sftp.connect();
                InputStream stream = sftp.get(RESULTS_LOCATION + perfMetric + ".log");
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                    String str;
                    while ((str = reader.readLine()) != null) {
                        switch (perfMetric) {
                            case "top":
                                String[] metrics = str.split("\\s+");
                                vmList.add(Double.valueOf(checkForMbGb(metrics[0])));
                                rssList.add(Double.valueOf(checkForMbGb(metrics[1])));
                                cpuList.add(Double.valueOf(metrics[2]));
                                memList.add(Double.valueOf(metrics[3]));
                                dateList.add(metrics[4]);
                                break;
                            case "pidstat":
                                // bw.write("\n");
                                String[] diskMetrics = str.split("\\s+");

                                for (int i = 0; i < diskMetrics.length; i++) {
                                    bw.write("|\t"
                                        + (double) Math.round((Double.valueOf(diskMetrics[i]) * 100) / 100)
                                        + "\t\t");
                                }
                                bw.write("\t|");
                                break;
                            case "pagefaults":
                                bw.write("\n");
                                String[] pageMetrics = str.split("\\s+");
                                for (int i = 0; i < pageMetrics.length; i++) {
                                    if (i == 0)
                                        bw.write("|\t\t"
                                            + (double) Math.round((Double.valueOf(pageMetrics[i]) * 100) / 100)
                                            + "\t\t");
                                    else
                                        bw.write("|\t"
                                            + (double) Math.round((Double.valueOf(pageMetrics[i]) * 100) / 100)
                                            + "\t\t");
                                }
                                bw.write("\t|");
                                break;
                        }
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                finally {
                    stream.close();
                }
            } else {
                Channel channel = session.openChannel("exec");
                ((ChannelExec) channel).setCommand(command);
                channel.setInputStream(null);
                ((ChannelExec) channel).setErrStream(System.err);
                InputStream in = channel.getInputStream();
                channel.connect();
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String s;
                while ((s = br.readLine()) != null) {

                    switch (perfMetric) {
                        case "ProcessId":
                            processId = s.trim();
                            break;

                        case "MachineDetails":
                            bw.write("    |_ " + s.trim() + "\n");
                            break;
                        case "StandaloneHammondLoad":
                            log.info("Standalone Hammond load started ...");
                            break;

                        case "CollectorHammondLoad":
                            log.info("Collector Hammond load started ...");
                            break;
                        case "JmeterLoad":
                            log.info("Jmeter load started ...");
                            break;

                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    public String checkForMbGb(String string) {
        if (string.contains("m")) return string.replace("m", "");
        if (string.contains("g")) return (Double.valueOf(string.replace("g", "")) * 1024) + "";
        return string;
    }

    public double calculateAverage(ArrayList<Double> list) {
        double sum = 0;
        if (!list.isEmpty()) {
            for (Double mark : list) {
                sum += mark;
            }
            return sum / list.size();
        }
        return sum;
    }

    public double getAvgVirtualMemoryUtilization() {
        return calculateAverage(vmList);
    }

    public double getAvgRssUtilization() {

        return calculateAverage(rssList);
    }

    public double getAvgCPUUtilization() {

        return calculateAverage(cpuList);
    }

    public double getAvgMemoryUtilization() {

        return calculateAverage(memList);
    }



}
