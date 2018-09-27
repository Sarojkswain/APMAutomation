function execute(metricData, javascriptResultSetHelper) {

  // for each input agent, compute a calculated metrics
  var i = 0;  					// binding iterator
  var btAvgList = {};			// array of BT metric tree name
  var btList = {};			// array of BT metric tree name
  var aggResponseMetric = {};		// array of Total Transactions metric
  var aggErrorMetric = {};   // array of Total errors metric
  var aggStallMetric = {};   // array of Total stalls metric
  var aggConcurrentMetric = {};   // array of Total concurrent invocations metric

  // Needed to calculate Weighted Average Response Time (WART)
  var avgValue = {};
  var respValue = {};
  var countValue = {};
  var minValue = {};
  var maxValue = {};
  var avgAccumulator = {};
  var countAccumulator = {};
  var avgTotalRespValue = {};
  var minOfavg = {};
  var maxOfavg = {};

  // Needed to calculate Weighted Average Response Time (WART)
  var btAvgList95 = {};     // array of BT metric tree name
  var avgValue95 = {};
  var countValue95 = {};
  var minValue95 = {};
  var maxValue95 = {};
  var avgAccumulator95 = {};
  var countAccumulator95 = {};
  var avgTotalRespValue95 = {};
  var minOfavg95 = {};
  var maxOfavg95 = {};

  var cmAgent = "SuperDomain|Custom Metric Host (Virtual)\|Custom Metric Process (Virtual)\|Custom Business Application Agent (Virtual)";
  var count, frequency, btTreeName, btSubTreeName, metricTree;

  // Collects Agent, Metric, and Values from the Input
  for (i = 0; i < metricData.length; i++) {
    var metric = metricData[i].agentMetric.attributeURL;
    var agent = metricData[i].agentName.processURL;
    var value = metricData[i].timeslicedValue.value;
    var min = metricData[i].timeslicedValue.getMinimumAsLong();
    var max = metricData[i].timeslicedValue.getMaximumAsLong();
    count = metricData[i].timeslicedValue.dataPointCount;
    frequency = metricData[i].frequency;
    var isAbsent = metricData[i].timeslicedValue.dataIsAbsent();

    // Only interested in metrics with the following metric names
    if (((metric.indexOf(":Average Response Time (ms)") > -1) ||
        (metric.indexOf(":Average Response Time 95th Percentile (ms)") > -1) ||
        (metric.indexOf(":Responses Per Interval") > -1)) ||
        (metric.indexOf(":Errors Per Interval") > -1) ||
        (metric.indexOf(":Concurrent Invocations") > -1) ||
        (metric.indexOf(":Stall Count") > -1) &&
        (isAbsent == false)) {			// Skip if no data is coming in. Will change to gray.

      //log.info("*** evaluating metric " + agent + "\|" + metric);

      btSubTreeName = metric.substring(metric.indexOf("\|") + 1);	// Remove the string "Business Segment|"
      var firstPipe = btSubTreeName.indexOf("\|");
      var bsName = btSubTreeName.substring(0, firstPipe); // extract BS name
      var btSubTreeName2 = btSubTreeName.substring(firstPipe + 1);
      var secondPipe = btSubTreeName2.indexOf("\|");
      var btName = btSubTreeName2.substring(0, secondPipe); // extract BT name
      var btSubTreeName3 = btSubTreeName2.substring(secondPipe + 1);
      var btcName = btSubTreeName3.substring(0, btSubTreeName3.indexOf(":")); // extract BTC name
      var metricName = btSubTreeName3.substring(btSubTreeName3.indexOf(":") + 1);

      // Build full metric name with the Custom Metric Agent
      btTreeName = cmAgent + "\|By Business Service\|" + bsName + "\|" + btName + "\|" + btcName + ":" + metricName;

      // if we haven't seen this btList yet add it to the list of known btList
      if (btList[btTreeName] == null) {
        btList[btTreeName] = btTreeName;
        aggResponseMetric[btTreeName] = 0;
        aggErrorMetric[btTreeName] = 0;
        aggStallMetric[btTreeName] = 0;
        aggConcurrentMetric[btTreeName] = 0;
      }

      // if we haven't seen this btAvgList yet add it to the list of known btList
      // this section is to calculate Weighted Average Response Time
      metricTree = agent + "|" + metric.substring(0, metric.indexOf(":"));	// Get the metric tree w/o the metric name
      if (btAvgList[metricTree] == null) {
        btAvgList[metricTree] = metricTree;
        avgValue[metricTree] = 0;
        respValue[metricTree] = 0;
        countValue[metricTree] = 0;
        minValue[metricTree] = 0;
        maxValue[metricTree] = 0;
      }

      if (btAvgList95[metricTree] == null) {
        btAvgList95[metricTree] = metricTree;
        avgValue95[metricTree] = 0;
        countValue95[metricTree] = 0;
        minValue95[metricTree] = 0;
        maxValue95[metricTree] = 0;
      }

      // store data to calculate WART after reading all the input
      if (metric.indexOf(":Average Response Time (ms)") > -1) {
        avgValue[metricTree] = value;
        countValue[metricTree] = count;
        minValue[metricTree] = min;
        maxValue[metricTree] = max;
      }

      else if (metric.indexOf(":Average Response Time 95th Percentile (ms)") > -1) {
        avgValue95[metricTree] = value;
        countValue95[metricTree] = count;
        minValue95[metricTree] = min;
        maxValue95[metricTree] = max;
      }

      // Accumulate the Responses Per Interval values for the same BT
      else if (metric.indexOf(":Responses Per Interval") > -1) {
        aggResponseMetric[btTreeName] += value;
        respValue[metricTree] = value;	// store data to calculate WART after reading all the input first
        //log.info("***   respValue        [" + metricTree + "] = " + respValue[metricTree]);
      }

      // Accumulate the Errors Per Interval values for the same BT
      else if (metric.indexOf(":Errors Per Interval") > -1) {
        aggErrorMetric[btTreeName] += value;
      }

      // Accumulate the Errors Per Interval values for the same BT
      else if (metric.indexOf(":Concurrent Invocations") > -1) {
        aggConcurrentMetric[btTreeName] += value;
      }

      // Accumulate the Errors Per Interval values for the same BT
      else if (metric.indexOf(":Stall Count") > -1) {
        aggStallMetric[btTreeName] += value;
      }
    }
  }

  // prepare for the WART calculation
  for (metricTree in btAvgList) {
    //log.info("*** calculating WART for metric " + metricTree);
    btSubTreeName = metricTree.substring(metricTree.indexOf("Business Segment\|") + 17);	// Remove the string "Business Segment|"
    //btTreeName = cmAgent + "\|By Business Service\|" + btSubTreeName + "\|Health:Average Response Time \(ms\)";	// Build full metric name with the Agent
    btTreeName = cmAgent + "\|By Business Service\|" + btSubTreeName + ":Average Response Time \(ms\)";	// Build full metric name with the Agent

    //log.info("***   btTreeName = " + btTreeName);

    if (avgAccumulator[btTreeName] == null) {
      avgAccumulator[btTreeName] = 0;
    }
    if (avgTotalRespValue[btTreeName] == null) {
      avgTotalRespValue[btTreeName] = 0;
    }
    if (countAccumulator[btTreeName] == null) {
      countAccumulator[btTreeName] = 0;
    }
    if (minOfavg[btTreeName] == null) {
      minOfavg[btTreeName] = 0;
    }
    if (maxOfavg[btTreeName] == null) {
      maxOfavg[btTreeName] = 0;
    }

    // accumulate values for the final WART calculation in the next FOR loop just before adding the metric
    avgAccumulator[btTreeName] += avgValue[metricTree] * respValue[metricTree];
    avgTotalRespValue[btTreeName] += respValue[metricTree];
    countAccumulator[btTreeName] += respValue[metricTree];

    //log.info("***   avgValue         [" + metricTree + "] = " + avgValue[metricTree]);
    //log.info("***   avgAccumulator   [" + btTreeName + "] = " + avgAccumulator[btTreeName]);
    //log.info("***   respValue        [" + metricTree + "] = " + respValue[metricTree]);
    //log.info("***   avgTotalRespValue[" + btTreeName + "] = " + avgTotalRespValue[btTreeName]);

    //countAccumulator[btTreeName] += countValue[metricTree];
    if (((minValue[metricTree] < minOfavg[btTreeName]) && (minValue[metricTree] > 0)) || ((minOfavg[btTreeName] < 1) && (minValue[metricTree] > 1))) {
      minOfavg[btTreeName] = minValue[metricTree];
    }
    if ((maxValue[metricTree] > maxOfavg[btTreeName])) {
      maxOfavg[btTreeName] = maxValue[metricTree];
    }
  }

  // prepare for the WART calculation for 95th Percentile ART
  for (metricTree in btAvgList95) {
    //log.info("*** calculating WART for metric " + metricTree);
    btSubTreeName = metricTree.substring(metricTree.indexOf("Business Segment\|") + 17);  // Remove the string "Business Segment|"
    //btTreeName = cmAgent + "\|By Business Service\|" + btSubTreeName + "\|Health:Average Response Time \(ms\)"; // Build full metric name with the Agent
    btTreeName = cmAgent + "\|By Business Service\|" + btSubTreeName + ":Average Response Time 95th Percentile \(ms\)"; // Build full metric name with the Agent

    //log.info("***   btTreeName = " + btTreeName);

    if (avgAccumulator95[btTreeName] == null) {
      avgAccumulator95[btTreeName] = 0;
    }
    if (avgTotalRespValue95[btTreeName] == null) {
      avgTotalRespValue95[btTreeName] = 0;
    }
    if (countAccumulator95[btTreeName] == null) {
      countAccumulator95[btTreeName] = 0;
    }
    if (minOfavg95[btTreeName] == null) {
      minOfavg95[btTreeName] = 0;
    }
    if (maxOfavg95[btTreeName] == null) {
      maxOfavg95[btTreeName] = 0;
    }

    avgAccumulator95[btTreeName] += avgValue95[metricTree] * respValue[metricTree];
    avgTotalRespValue95[btTreeName] += respValue[metricTree];
    countAccumulator95[btTreeName] += respValue[metricTree];

    if (((minValue95[metricTree] < minOfavg95[btTreeName]) && (minValue95[metricTree] > 0)) || ((minOfavg95[btTreeName] < 1) && (minValue95[metricTree] > 1))) {
      minOfavg95[btTreeName] = minValue95[metricTree];
    }
    if ((maxValue95[metricTree] > maxOfavg95[btTreeName])) {
      maxOfavg95[btTreeName] = maxValue95[metricTree];
    }
  }

  // now iterate found agents and report calculated metrics
  for (btTreeName in btList) {

    //log.info("*** writing metric " + btTreeName);

    // added to use correct metric type
    var metricType = Packages.com.wily.introscope.spec.metric.MetricTypes.kLongIntervalCounter;
    var finalValue = new java.lang.Long(0);

    // Take care of Average Response Time (ms) metric
    if (btTreeName.indexOf(":Average Response Time (ms)") > -1) {
      metricType = Packages.com.wily.introscope.spec.metric.MetricTypes.kLongDuration;

      if (avgTotalRespValue[btTreeName] > 0) {
        var aValue = avgAccumulator[btTreeName] / avgTotalRespValue[btTreeName];
        var rValue = Math.round(aValue);
        finalValue = new java.lang.Long(rValue);
        count = countAccumulator[btTreeName];
      }
    } else if (btTreeName.indexOf(":Average Response Time 95th Percentile (ms)") > -1) {
        metricType = Packages.com.wily.introscope.spec.metric.MetricTypes.kLongDuration;

        if (avgTotalRespValue95[btTreeName] > 0) {
          var aValue = avgAccumulator95[btTreeName] / avgTotalRespValue95[btTreeName];
          var rValue = Math.round(aValue);
          finalValue = new java.lang.Long(rValue);
          count = countAccumulator95[btTreeName];
        }
    } else if (btTreeName.indexOf(":Responses Per Interval") > -1) {
      finalValue = new java.lang.Long(aggResponseMetric[btTreeName]);
    } else if (btTreeName.indexOf(":Errors Per Interval") > -1) {
      finalValue = new java.lang.Long(aggErrorMetric[btTreeName]);
    } else if (btTreeName.indexOf(":Concurrent Invocations") > -1) {
      finalValue = new java.lang.Long(aggConcurrentMetric[btTreeName]);
    } else if (btTreeName.indexOf(":Stall Count") > -1) {
      finalValue = new java.lang.Long(aggStallMetric[btTreeName]);
    }

    // add the calculated value to the result set
    // using metricType variable as defined above(Changed)
    // determine which addMetric to use
    if (metricType == Packages.com.wily.introscope.spec.metric.MetricTypes.kLongIntervalCounter) {
      //addMetric(metricName, count, value, min, max, metricType, frequency)
      javascriptResultSetHelper.addMetric(btTreeName, finalValue, finalValue, 0, finalValue, metricType, frequency);
    } else {

      if (btTreeName.indexOf(":Average Response Time (ms)") > -1) {
        if (finalValue == 0 && minOfavg[btTreeName] == 0 && maxOfavg[btTreeName] == 0) {
          count = 0;
        }
        javascriptResultSetHelper.addMetric(btTreeName, count, finalValue, minOfavg[btTreeName], maxOfavg[btTreeName], metricType, frequency);
      }

      if (btTreeName.indexOf(":Average Response Time 95th Percentile (ms)") > -1) {
        if (finalValue == 0 && minOfavg95[btTreeName] == 0 && maxOfavg95[btTreeName] == 0) {
          count = 0;
        }
        javascriptResultSetHelper.addMetric(btTreeName, count, finalValue, minOfavg95[btTreeName], maxOfavg95[btTreeName], metricType, frequency);
      }
    }
  } //end of for loop

  // return the result set
  return javascriptResultSetHelper;

}


// run on all agents (could restrict to Java only)
function getAgentRegex() {
  return "(.*)\\|(.*)\\|(.*)";
}


function getMetricRegex() {
  // no browser agent metrics
  return "Business Segment\\|[^\|]+\\|[^\|]+\\|(?!Browser)[^\|]+";
}


// must return a multiple of default system frequency (currently 15 seconds)
function getFrequency() {
  return 1 * Packages.com.wily.introscope.spec.metric.Frequency.kDefaultSystemFrequencyInSeconds;
}


// Return false if the script should not run on the MOM.
// Scripts that create metrics on agents other than the Custom Metric Agent
// should not run on the MOM because the agents exist only in the Collectors.
// Default is true.
function runOnMOM() {
  return true;
}
