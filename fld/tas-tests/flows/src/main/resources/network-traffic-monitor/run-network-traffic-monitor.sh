#!/bin/bash

WORK_DIR=`dirname $0`
RESULTS_DIR=$WORK_DIR/csv

TCPDUMP_OPTS='' # i.e. '-c 10'
#TCPDUMP_FILTER='(host tas-cz-fld-nb8) and (tcp port 5001 or tcp port 5443)'
#TCPDUMP_FILTER='tcp port 5001 or tcp port 5443'
TCPDUMP_FILTER=%%TCPDUMP_FILTER%%
#INTERVAL_DURATION=5000 # milliseconds
INTERVAL_DURATION=%%INTERVAL_DURATION%%

cd $WORK_DIR
rm -rf $RESULTS_DIR/*
mkdir -p $RESULTS_DIR

tcpdump -e -i any -K -l -N -p -tt $TCPDUMP_OPTS $TCPDUMP_FILTER 2>/dev/null | \
  awk '
    BEGIN {
      OFS = ",";

      _IN = "In";
      _OUT = "Out";

      resultsDir = "'"$RESULTS_DIR"'";

      intervalDuration = "'"$INTERVAL_DURATION"'";

      startTime = -1; # uninitialized
      intervalEndTime = -1; # uninitialized

      iterationNumber = 0; # = total count of packets processed
      intervalNumber = 0;
    }

    {
      iterationNumber++;

      currentTime = substr($1, 0, 14) * 1000; # milliseconds, i.e. 1468487259.977 * 1000

      # initialize startTime, intervalEndTime
      if (startTime < 0) {
        startTime = currentTime;
        intervalEndTime = startTime + intervalDuration;
      }

      # close elapsed sampling time interval
      if (currentTime > intervalEndTime) {
        closeTimeInterval();
      }

      # incerase byte counters
#      currentBytes = substr($10, 0, length($10) - 1);
      currentBytes = $NF; # last field
      if (currentBytes > 0) {

        direction = $2;

        # resolve remote host
        if (direction == _IN) {
          remoteHost = $11;
        } else if (direction == _OUT) {
          remoteHost = $13;
        }
        remoteHost = substr(remoteHost, 0, index(remoteHost, ".") - 1);

        if (!(remoteHost in remoteHosts)) {
          # remember remote host within a set
          remoteHosts[remoteHost] = remoteHost;

          # init byte counters
          bytesPerInterval_in[remoteHost] = 0;
          bytesPerInterval_out[remoteHost] = 0;
        }

        if (direction == _IN) {
          bytesPerInterval_in[remoteHost] += currentBytes;
        } else if (direction == _OUT) {
          bytesPerInterval_out[remoteHost] += currentBytes;
        }
      }

    }

    END {
      iterationNumber++;
      closeTimeInterval();
    }

    function closeTimeInterval() {
      intervalNumber++;
      for (remoteHost in remoteHosts) {
        totalBytes_in[remoteHost] += bytesPerInterval_in[remoteHost];
        totalBytes_out[remoteHost] += bytesPerInterval_out[remoteHost];
      }

      writeResult();

      intervalEndTime += intervalDuration;

      # reset byte counters
      for (remoteHost in remoteHosts) {
        bytesPerInterval_in[remoteHost] = 0;
        bytesPerInterval_out[remoteHost] = 0;
      }
    }

    function writeResult() {
      durationFromStart = intervalEndTime - startTime;
      for (remoteHost in remoteHosts) {
        totalBytesPerInterval = bytesPerInterval_in[remoteHost] + bytesPerInterval_out[remoteHost];
        totalBytes = totalBytes_in[remoteHost] + totalBytes_out[remoteHost];

#        resultsFile = resultsDir"/network-traffic_"remoteHost".txt";
#        resultsFile = resultsDir"/"remoteHost;
        resultsFile = resultsDir"/"remoteHost".csv";

#        print intervalEndTime, durationFromStart, iterationNumber, intervalNumber, remoteHost, \
#          bytesPerInterval_in[remoteHost], bytesPerInterval_out[remoteHost], totalBytesPerInterval, \
#          totalBytes_in[remoteHost], totalBytes_out[remoteHost], totalBytes >> resultsFile;
        printf("%d','%d','%d','%d','%s','%d','%d','%d','%d','%d','%d\n", \
          intervalEndTime, durationFromStart, iterationNumber, intervalNumber, remoteHost, \
          bytesPerInterval_in[remoteHost], bytesPerInterval_out[remoteHost], totalBytesPerInterval, \
          totalBytes_in[remoteHost], totalBytes_out[remoteHost], totalBytes) >> resultsFile;
        fflush(resultsFile);
      }
    }
  '
