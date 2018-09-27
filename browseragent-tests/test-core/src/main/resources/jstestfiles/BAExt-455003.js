/**
 * CA Wily Introscope(R) Version @@@WILY_RELEASE@@@ Build @@@WILY_BUILD_NUMBER@@@
 * @@@INTROSCOPE_COPYRIGHT_STRING_PLACE_HOLDER@@@
 * @@@INTROSCOPE_REGISTERED_TRADEMARK_STRING_PLACE_HOLDER@@@
 */
try {
    var BrowserAgentExtension = {
        /** DO NOT EDIT **/
        internal : {
            createAXAEvent : function ( eventId, eventName, eventType, eventValue, timeStamp, url, responseTime,
                                        statusCode, dataIn, dataOut, countryCode, zipCode, latitude, longitude,
                                        x_AttrList ) {
                var axaEvent = {};
                if ( typeof eventId === 'number' && !isNaN(eventId) ) {
                    axaEvent.eventId = eventId;
                }
                if ( typeof eventName === 'string' ) {
                    axaEvent.eventName = eventName;
                }
                if ( typeof eventType === 'string' ) {
                    axaEvent.eventType = eventType;
                }
                if ( typeof eventValue === 'string' || typeof eventValue === 'number' ) {
                    axaEvent.eventValue = eventValue.toString();
                }
                if ( typeof timeStamp === 'number' && !isNaN(timeStamp) ) {
                    axaEvent.timeStamp = timeStamp;
                }
                if ( typeof url === 'string' ) {
                    axaEvent.url = url;
                }
                if ( typeof responseTime === 'number' && !isNaN(responseTime) ) {
                    axaEvent.responseTime = responseTime;
                }
                if ( typeof statusCode === 'number' && !isNaN(statusCode) ) {
                    axaEvent.statusCode = statusCode;
                }
                if ( typeof dataIn === 'number' && !isNaN(dataIn) ) {
                    axaEvent.dataIn = dataIn;
                }
                if ( typeof dataOut === 'number' && !isNaN(dataOut) ) {
                    axaEvent.dataOut = dataOut;
                }
                if ( typeof countryCode === 'string' ) {
                    axaEvent.countryCode = countryCode;
                }
                if ( typeof zipCode === 'string' ) {
                    axaEvent.zipCode = zipCode;
                }
                if ( typeof latitude === 'string' ) {
                    axaEvent.latitude = latitude;
                }
                if ( typeof longitude === 'string' ) {
                    axaEvent.longitude = longitude;
                }
                if ( x_AttrList && x_AttrList.length > 0 ) {
                    axaEvent.x_attributes = { x_attributeList : x_AttrList };
                }
                return axaEvent;
            },
            /**
             * Adds AXA Data to a global tracker data map as well as into the AXA events
             * @param data - AXA data object. If the data object contains trackerData sub object, then store
             *     trackerData in the global tracker data map
             */
            addAXAData : function ( data ) {
                try {
                    if ( BrowserAgent.globals.configs.BROWSERAGENTENABLED === false ) { return; }
                    if ( data === null || typeof data !== 'object' ) {
                        BrowserAgent.logger.warn("BrowserAgentExtension.internal.addAXAData: Cannot add data due to invalid input");
                        return;
                    }
                    var tkrAttrList = [], x_AttrList = [];
                    var result;
                    // Store the tracker data in the global tracker data map
                    if ( typeof data.trackerId === 'string' && data.trackerData !== null &&
                         typeof data.trackerData === 'object' ) {
                        // Prevent trackerData overwrite
                        if ( BrowserAgent.globals.trackerDataMap[data.trackerId] ) { return; }
                        // Create an X_Attr of the tracker data attributes
                        for ( var key in data.trackerData ) {
                            result = BrowserAgent.jsonUtils.createXAttribute(key, data.trackerData[key]);
                            if ( result ) { tkrAttrList.push(result); }
                        }
                        BrowserAgent.globals.trackerDataMap[data.trackerId] =
                        { x_attributes : { x_attributeList : tkrAttrList } };
                        BrowserAgent.globals.trackerDataMapSize += 1;
                    } else {
                        // No trackerID or trackerData or both
                        // Just clone current trackerData and append it to the attr list
                        for ( var td in BrowserAgent.globals.trackerDataMap ) {
                            x_AttrList =
                                x_AttrList.concat(BrowserAgent.globals.trackerDataMap[td].x_attributes.x_attributeList);
                        }
                    }
                    // If there are { key, value } pairs, then append it to the attr list
                    if ( data.attr ) {
                        for ( var key in data.attr ) {
                            result = BrowserAgent.jsonUtils.createXAttribute(key, data.attr[key]);
                            if ( result ) { x_AttrList.push(result); }
                        }
                    }
                    // Create an AXA event object out of the given data
                    result =
                        BrowserAgentExtension.internal.createAXAEvent(data.eid, data.n, data.ty, data.v, data.t, data.u,
                                                                      data.r, data.s, data.i, data.o, data.cc, data.zp,
                                                                      data.la, data.lo, x_AttrList);
                    if ( !result ) { return; }
                    var evtObj = BrowserAgent.evtUtils.getEvtObject(BrowserAgent.globals.evtTypes.AXAEXT, false,
                                                                    null);
                    if ( !evtObj ) { return; }
                    // Add an AXA EXT event to the event map of the current page
                    evtObj.d = result;
                    evtObj.isDone = true;
                } catch ( e ) {
                    if ( evtObj ) { evtObj.isDelete = true; }
                    //TODO: think about how to print here without a dependency on BrowserAgent.logger
                }
            },
            /**
             * Erases the data (marked with the given tracker ID) from the global tracker data map
             * @param trackerId - must be the same tracker ID with which the tracker data was stored
             */
            clearTracker : function ( trackerId ) {
                try {
                    if ( typeof trackerId !== 'string' || trackerId.length < 1 ) {
                        BrowserAgent.logger.warn("BrowserAgentExtension.internal.clearAXAData: Cannot clear data for tracker ID [" +
                                                 trackerId + "]");
                    }
                    // Obliterate data if present.
                    if ( BrowserAgent.globals.trackerDataMap[trackerId] ) {
                        delete BrowserAgent.globals.trackerDataMap[trackerId];
                        BrowserAgent.globals.trackerDataMapSize -= 1;
                    }
                } catch ( e ) {
                    //TODO: think about how to print here without a dependency on BrowserAgent.logger
                }
            },
            /**
             * Obtains the tracker data given an id
             * @param trackerId
             * @returns {*}
             */
            getTrackerDataById : function ( trackerId ) {
                try {
                    if ( !BrowserAgent.globals.trackerDataMap[trackerId] ) { return null; }
                    return BrowserAgent.globals.trackerDataMap[trackerId];
                } catch ( e ) {
                    return null;
                    //TODO: think about how to print here without a dependency on BrowserAgent.logger
                }
            },
            /**
             * Determines if any trackerData is present.
             * @returns {*}
             */
            isTrackerDataPresent : function () {
                try {
                    return BrowserAgent.globals.trackerDataMapSize > 0;
                } catch ( e ) {
                    return false;
                    //TODO: think about how to print here without a dependency on BrowserAgent.logger
                }
            }
        },
        /** DO NOT EDIT **/

        init : function () {

            /**
             * ADD YOUR OWN CODE HERE
             */

        },
        /**
         * Adds JS functions to be instrumented for JS function metrics.
         */
        extAddJSFuncToInstrument : function () {
            /**
             * Add JS Functions here to instrument using BrowserAgentExtension.addJSFuncToInstrument( functionName,
             * preTracerList, postTracerList ) Note: If the JS function to be instrumented is a member function inside
             * a JS object, you may need to add the keyword 'prototype'.
             *
             * preTracerList is an array of JavaScript objects, where each object describes a JavaScript function
             * to be invoked before the invocation of the instrumented function (functionName)
             *
             * postTracerList is an array of JavaScript objects, where each object describes a JavaScript function to
             * be invoked after the invocation of the instrumented function (functionName)
             *
             * Here is the JavaScript object format for preTracerList and postTracerList
             * {
             *   name: <Name of the JavaScript function to be invoked before the invocation of the instrumented
             *          function>,
             *   args: [<arg_1>, <arg_2>, <arg_3>, ....]
             * }
             *
             *
             * Example:
             * Calculate User Think time between "Add Items to Cart" and "CheckOut"
             * Assume on a page the "Add to Cart" button invokes addItemToCart(itemID) function and "CheckOut" button
             * invokes checkOut(cartID) function.
             *
             * Step 1: Have a place to store raw data
             * var myOwnAccumulator = { cartAddTimes : [], checkOutTime : null};
             *
             * Step 2: Write a preTracer for "addItemToCart" JS method
             * Let’s call this preTracer 'addItemToCartPreTracer' and nest it under the BrowserAgentExtension object.
             * BrowserAgentExtension.addItemToCartPreTracer = function() {
             *  // Browser Agent exposes an object in which a tracer can store and retrieve data at a later point.
             *  var stateObj;
             *  // Browser Agent recommends that you wrap the tracer code in its own try, catch block to avoid early
             *  // termination due to runtime errors.
             *  try {
             *      // The last argument to a tracer is always the BA state object.
             *      stateObj = arguments[arguments.length - 1];
             *      // Get the current time and store it
             *      myOwnAccumulator["cartAddTimes"].push(new Date().getTime());
             *  } catch ( e ) {
             *      BrowserAgent.logger.error("addItemPre (" + stateObj.origFunctionName + "): " + e.message);
             *  }
             * }
             *
             * Step 3: Write a postTracer for "checkOut" JS method
             * Let’s call this postTracer 'checkOutPostTracer' and nest it under the BrowserAgentExtension object.
             * Note: This postTracer also pushes data with the addExtensionJSONObject API
             * BrowserAgentExtension.checkOutPostTracer = function() {
             *  // Again, Browser Agent exposes an object in which a tracer can store and retrieve data at a later
             *  // point.
             *  var stateObj;
             *  // Again, Browser Agent recommends that you wrap the tracer code in its own try, catch block to avoid
             *  // early termination due to runtime errors.
             *  try {
             *      // Again, the last argument to a tracer is always the BA state object.
             *      stateObj = arguments[arguments.length - 1];
             *      // Get the current time and store it
             *      myOwnAccumulator["checkOutTime"] = new Date().getTime();
             *
             *      if(myOwnAccumulator["cartAddTimes"].length < 1) { return; }
             *      // Now, do the checkout duration calculation
             *      var checkoutTime = myOwnAccumulator["checkOutTime"] - myOwnAccumulator["cartAddTimes"][0];
             *      // Tell BA to report the data
             *      var metricList = [];
             *      // This is the context in which the data will be reported in APM Browser Agent
             *      var metricPath = BrowserAgent.globals.pageMetricPath + BrowserAgent.globals.pipeChar + "MISC";
             *      // Use BrowserAgentExtension.createCustomMetric API to create a new APM Browser Agent metric and
             *      add it to a list of custom metrics
             *      metricList.push(BrowserAgentExtension.createCustomMetric("CheckOut Time", "ms", 0, checkoutTime,
             * metricPath));
             *      // Use the BrowserAgentExtension.addExtensionJSONObject to construct a JSON payload for this custom
             *      // metric
             *      BrowserAgentExtension.addExtensionJSONObject(metricList);
             *      // Clear out the data; we don't want to report the same data twice
             *      myOwnAccumulator["cartAddTimes"] = [];
             *      myOwnAccumulator["checkOutTime"] = null;
             *  } catch ( e ) {
             *      BrowserAgent.logger.error("checkOutPost (" + stateObj.origFunctionName + "): " + e.message);
             *  }
             * }
             * Step 4: Use the addJSFuncToInstrument API to attach the pre and post tracers from Step 2 and 3
             * // Attach the "BrowserAgentExtension.addItemToCartPreTracer" as a PreTracer to the addItemToCart JS
             * method BrowserAgentExtension.addJSFuncToInstrument("addItemToCart",
             *                                             [{ name: "BrowserAgentExtension.addItemToCartPreTracer"}]);
             * // Attach the "BrowserAgentExtension.checkOutPostTracer" as a PostTracer to the checkOutItem JS method
             * BrowserAgentExtension.addJSFuncToInstrument("checkOutItem", null,
             *                                             [{ name: "BrowserAgentExtension.checkOutPostTracer"}]);
             **/
            BrowserAgentExtension.addJSFuncToInstrument("dummyObject.instRetryFunc", [{ name: "BrowserAgentExtension.mobyDick" }, { name: "non-Existent" }], [ {name: "BrowserAgentExtension.callMeIshmael" }, { name: "non-Existent" }]);

        },
        callMeIshmael : function() {
            console.log("callMeIshmael");
        },

        mobyDick : function() {
            console.log("I try all things, I achieve what I can - Herman Melville.");
        },

        /**
         * Adds custom page load metrics for each page.
         * @returns {Array}
         */
        extAddCustomPageMetric : function () {
            /**
             * Step 1
             * Do your work to collect metrics.
             *
             * Step 2
             * Add collected metrics for harvesting using BrowserAgentExtension.addCustomPageMetric(name, unit, type,
             * value).
             * Metric path is not needed here since it will use the page metric path by default.
             *
             * Example 1: Report DOM Depth of a Web Page
             * Note: This is not the actual implementation to calculate the DOM depth of a web page, but
             * just an example with JS random number generator.
             *
             * function getRandomNumberOneToFive() { return Math.floor(Math.random() * (5 - 1)) + 1; }
             * var domDepth = getRandomNumberOneToFive();
             * BrowserAgentExtension.addCustomPageMetric("DOM Depth", null, 0, domDepth);
             *
             * Example 2: Report JS Heap Usage in Bytes
             * Note: The window.performance.memory object is only available in Google Chrome.
             *
             * function getHeapSize() { return window.performance.memory.usedJSHeapSize; }
             * var jsHeapUsage = getHeapSize();
             * BrowserAgentExtension.addCustomPageMetric("Heap Usage", "bytes", 0, jsHeapUsage);
             */
        },
        /**
         * Adds custom JS function metrics for each JS function.
         * @returns {Array} - array of metrics
         */
        extAddCustomJSFuncMetric : function () {
            /**
             * Step 1
             * Do your work to collect metrics.
             *
             * Step 2
             * Add collected metrics for harvesting using BrowserAgentExtension.addCustomJSFuncMetric(name, unit, type,
             * value).
             * Metric path is not needed here since it will use the page metric path by default.
             *
             * Example: Report Argument Length of a JavaScript Method
             * Note: This is not the actual implementation to calculate the argument length of a JS method, but just
             * an example with JS random number generator.
             *
             * function getRandomNumberOneToFive() { return Math.floor(Math.random() * (5 - 1)) + 1; };
             * var argLength = getRandomNumberOneToFive();
             * BrowserAgentExtension.addCustomJSFuncMetric("Argument Length", null, 0, argLength);
             */
        },
        /**
         * Adds custom Ajax metrics for each Ajax call.
         * @returns {Array}
         */
        extAddCustomAjaxMetric : function () {
            /**
             * Step 1
             * Do your work to collect metrics.
             *
             * Step 2
             * Add collected metrics for harvesting using BrowserAgentExtension.addCustomAjaxMetric(name, unit, type,
             * value).
             * Metric path is not needed here since it will use the corresponding Ajax metric path by default.
             *
             * Example: Report Content Length in Bytes
             * Note: This is not the actual implementation to calculate the Content Length of an HTTP response, but
             * just an example with JS random number generator
             *
             * function getRandomNumber1Kto4K() { return Math.floor(Math.random() * (4096 - 1024)) + 1024; };
             * var contentLength = getRandomNumber1Kto4K();
             * BrowserAgentExtension.addCustomAjaxMetric("Content Length", "bytes", 0, contentLength);
             */
        },
        /**
         * Adds custom optional transaction trace properties for each transaction trace.
         * @returns {Array}
         */
        extAddCustomOptionalProperty : function () {
            /**
             * Add Transaction Trace Properties here using BrowserAgentExtension.addCustomOptionalProperty(name, value,
             * description).
             * Note: property description is optional.
             *
             * Example: Report Previous Page URL in the Trace Components
             *
             * function getPreviousPage() {
             *   var referrer = document.referrer;
             *   if ( !referrer ) { referrer = "N/A"; }
             *   return referrer;
             * }
             * var prevPageURL = getPreviousPage();
             * BrowserAgentExtension.addCustomOptionalProperty("Previous Page", prevPageURL);
             **/
        },
        /**
         * Name formatter allows you to change/group metrics by giving flexibility to change the metric path, name,
         * unit, aggregator type and value before creating the final metric.
         * Note: It's not recommended to change metric type and value. Instead, you can add custom metrics.
         * @param path - metric path as a string
         * @param name - metric name as a string
         * @param unit - metric unit as null or a string
         * @param type - metric accumulator type as a number enum -
         *        0 : INT_LONG_DURATION (These metrics are aggregated over time by taking the average of the values
         *        per interval)
         *        1 : LONG_INTERVAL_COUNTER (These metrics are aggregated over time by summing the values per interval)
         * @param value - metric value as a number
         * @returns {*|{name, unit, type, value, path}|{name: *, unit: *, type: *, value: *, path: *}}
         */
        extNameFormatter : function ( path, name, unit, type, value ) {
            /**
             *  Step 1
             *  Do your work to format the input metric.
             *  Note: It's not recommended to change metric type and value. Instead, you can add custom metrics.
             *  Metric path can be formatted URL or Business Transaction if matched on Agent.
             *
             *  Step 2
             *  Return a new metric with formatted metric info using
             *  BrowserAgentExtension.createCustomMetric(name, unit, type, value, path).
             *
             *  EXAMPLE 1: Rename Metrics
             *  Change all metrics that has metric name "Invocation Count Per Interval" to "Country Visit Count" -
             *
             *  if (name === "Invocation Count Per Interval") {
             *      name = "Country Visit Count";
             *  }
             *  return BrowserAgentExtension.createCustomMetric(name, unit, type, value, path);
             *
             *  EXAMPLE 2: Aggregate Metrics from Dynamic URLs
             *  For all metrics that have paths containing "country_#" such as:
             *  localhost/5080|/worldpop|AJAX Call|localhost/5080|/country_1/country.json
             *  localhost/5080|/worldpop|AJAX Call|localhost/5080|/country_2/country.json
             *  localhost/5080|/worldpop|AJAX Call|localhost/5080|/country_3/country.json
             *
             *  Combine all of the "localhost/5080|/worldpop|AJAX Call|localhost/5080|/country_#/country.json"
             *  into "localhost/5080|/worldpop|AJAX Call|localhost/5080|/country.json".
             *
             *  path = path.replace(/country_\d+\//g, "");
             *  return BrowserAgentExtension.createCustomMetric(name, unit, type, value, path);
             */
        },
        /**
         * Adds JavaScript function to be instrumented for JS function metrics.
         * @param functionName - JavaScript function name as a string. If the JS function to be instrumented is a
         *     member function inside a JS object, you may need to add the keyword prototype
         * @param preTracerList - An array of Javascript functions whose invocation precedes the function to be
         *     instrumented
         * @param postTracerList - An array of Javascript functions whose invocation succeeds the function to be
         *     instrumented
         */
        addJSFuncToInstrument : function ( functionName, preTracerList, postTracerList ) {
            BrowserAgent.funcUtils.addFuncToCollection(BrowserAgent.globals.extFuncMap, functionName,
                                                       preTracerList, postTracerList);
        },
        /**
         * An array that holds custom page metrics and is reset every metric harvest cycle.
         */
        extCustomPageMetricList : [],
        /**
         * Creates a custom page metric and adds it to extCustomPageMetricList.
         * @param name - metric name as a string
         * @param unit - metric unit as null or a string
         * @param type - metric accumulator type as a number enum -
         *        0 : INT_LONG_DURATION (These metrics are aggregated over time by taking the average of the values
         *        per interval)
         *        1 : LONG_INTERVAL_COUNTER (These metrics are aggregated over time by summing the values per interval)
         * @param value - metric value as a number
         */
        addCustomPageMetric : function ( name, unit, type, value ) {
            BrowserAgentExtension.extCustomPageMetricList.push(BrowserAgentExtension.createCustomMetric(name, unit,
                                                                                                        type, value));
        },
        /**
         * An array that holds custom JS function metrics and is reset every metric harvest cycle.
         */
        extCustomJSFuncMetricList : [],
        /**
         * Creates a custom JS function metric and adds it to extCustomJSFuncMetricList.
         * @param name - metric name as a string
         * @param unit - metric unit as null or a string
         * @param type - metric accumulator type as a number enum -
         *        0 : INT_LONG_DURATION (These metrics are aggregated over time by taking the average of the values
         *        per interval)
         *        1 : LONG_INTERVAL_COUNTER (These metrics are aggregated over time by summing the values per interval)
         * @param value - metric value as a number
         */
        addCustomJSFuncMetric : function ( name, unit, type, value ) {
            BrowserAgentExtension.extCustomJSFuncMetricList.push(BrowserAgentExtension.createCustomMetric(name, unit,
                                                                                                          type, value));
        },
        /**
         * An array that holds custom Ajax metrics and is reset every metric harvest cycle.
         */
        extCustomAjaxMetricList : [],
        /**
         * Creates a custom Ajax metric and adds it to extCustomAjaxMetricList.
         * @param name - metric name as a string
         * @param unit - metric unit as null or a string
         * @param type - metric accumulator type as a number enum -
         *        0 : INT_LONG_DURATION (These metrics are aggregated over time by taking the average of the values
         *        per interval)
         *        1 : LONG_INTERVAL_COUNTER (These metrics are aggregated over time by summing the values per interval)
         * @param value - metric value as a number
         */
        addCustomAjaxMetric : function ( name, unit, type, value ) {
            BrowserAgentExtension.extCustomAjaxMetricList.push(BrowserAgentExtension.createCustomMetric(name, unit,
                                                                                                        type, value));
        },
        /**
         * An array that holds custom transaction trace optional properties and is reset every metric harvest
         * cycle.
         */
        extCustomOptionalPropertyList : [],
        /**
         * Creates a custom optional transaction trace property and adds it to extCustomOptionalPropertyList.
         * @param name - name of property as a string
         * @param value - value of property as a string
         * @param description - description of property. Optional.
         * @returns {{name: *, value: *, description: *}}
         */
        addCustomOptionalProperty : function ( name, value, description ) {
            BrowserAgentExtension.extCustomOptionalPropertyList.push(
                { name : name, value : value, description : description });
        },
        /**
         * Creates an extension JSON object that follows the Browser Agent JSON schema
         * @param metricList - a list of metrics
         */
        addExtensionJSONObject : function ( metricList ) {
            try {
                if ( BrowserAgent.globals.configs.BROWSERAGENTENABLED === false ) { return; }
                if ( !metricList || metricList.length === 0 ) {
                    BrowserAgent.logger.warn("addExtensionJSONObject: Invalid metric list. Discard extension JSON object...");
                    return;
                }
                for ( var i in metricList ) {
                    var metric = metricList[i];
                    if ( !metric || !BrowserAgent.jsonUtils.validateMetric(metric.path, metric.name, metric.unit,
                                                                           metric.accumulatorType,
                                                                           metric.value) ) {
                        BrowserAgent.logger.warn("addExtensionJSONObject: Invalid metric list. Discard extension JSON object...");
                        return;
                    }
                }
                var evtObj = BrowserAgent.evtUtils.getEvtObject(BrowserAgent.globals.evtTypes.APMEXT, false, null);
                if ( !evtObj ) { return; }
                evtObj.lst = metricList;
                evtObj.isDone = true;
            } catch ( e ) {
                if ( evtObj ) { evtObj.isDelete = true; }
                //TODO: think about how to print here without a dependency on BrowserAgent.logger
            }
        },
        /**
         * Creates a custom metric without validation.
         * @param name - metric name as a string
         * @param unit - metric unit as null or a string
         * @param type - metric accumulator type as a number enum -
         *        0 : INT_LONG_DURATION (These metrics are aggregated over time by taking the average of the values
         *        per interval)
         *        1 : LONG_INTERVAL_COUNTER (These metrics are aggregated over time by summing the values per interval)
         * @param value - metric value as a number
         * @param path - metric path as a string. May be optional.
         * @returns {{name: *, unit: *, type: *, value: *, path: *}}
         */
        createCustomMetric : function ( name, unit, type, value, path ) {
            return {
                name : name,
                unit : unit,
                accumulatorType : type,
                value : value,
                path : path
            };
        }
    };
} catch ( e ) {
    if ( window.BrowserAgent && BrowserAgent.logger ) {
        BrowserAgent.logger.log("BrowserAgentExtensionError: " + e.message);
    } else if ( window && window.console ) {
        window.console.log("BrowserAgentExtensionError: " + e.message);
    }
}
