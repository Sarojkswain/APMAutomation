var system = require('system');
var page = require('webpage').create();

var scriptName = basename(system.args[0], true);
var btExportsForImport = system.args.slice(1);
var cemHostname = '${role.introscope.em_hostname}';
var cemPort = '${role.introscope.emWebPort}';
var timHostname = '${machine.gateway.hostname}';
var timIPAddress = '${testbed.machine.gateway.ip_v4}';

// common functionality - TODO move to extra file
phantom.onError = function (msg, trace) {
    var msgStack = ['PHANTOM ERROR: ' + msg];
    if (trace && trace.length) {
        msgStack.push('TRACE:');
        trace.forEach(function (t) {
            msgStack.push(' -> ' + (t.file || t.sourceURL) + ': ' + t.line + (t.function ? ' (in function ' + t.function + ')' : ''));
        });
    }
    console.error(msgStack.join('\n'));
    phantom.exit(1);
};


function initPage(page) {
    console.log('configuring page and default methods.');
    page.onConsoleMessage = function(msg) {
    	console.log('CONSOLE: ' + msg);
    };

	page.onAlert = function(msg) {
		console.log('ALERT: ' + msg);
	};
	
	page.onConfirm = function(msg) {
		 console.log('CONFIRM: ' + msg);
		 return true;
	};
	
	page.onResourceReceived = function (response) {
	    // log(response.url);
	    testCommon.resourceReceivedCount++;
	    testCommon.resourceReceived.push(response.url);
	};

	page.onResourceError = function(response) {
	    testCommon.resourceError.push(response.url);
	};

	page.onLoadFinished = function(status){
	    testCommon.loadFinished = true;
	};

	page.viewportSize = {width: 1920, height: 1080};

    page.onError = function (msg, trace) {
        console.log('Page onerror:' + msg);
        trace.forEach(function (item) {
            console.log('  ', item.file, ':', item.line);
        });
        phantom.exit(-1);
    };
}

// better to use path.basename, but path module is not available
function basename(path, removeExtension) {
	var basename = path.split(/[\\/]/).pop();
	if (removeExtension) {
		basename = new String(basename).substring(0, basename.lastIndexOf('.'));
	}
	return basename;
}


var logError = function (message) {
    console.log(formatLogMessage('ERROR ' + message));
}

var log = function (message) {
    console.log(formatLogMessage(message));
};

var formatLogMessage = function(message){

    var fromStart = (new Date() - testCommon.startTime) / 100;
    fromStart = Math.round(fromStart);
    fromStart = fromStart / 10;
    var fromStartStr = fromStart.toString();
    var i = 5;
    while(true){
        if(fromStartStr.length < i){
            fromStartStr = ' ' + fromStartStr;
        }else{
            break;
        }

    }
    return fromStartStr + 's ' + message;
}

var closePhantom = function (exitCode) {
    var t = Date.now() - testCommon.startTime;
    console.log('Text execution time: ' + t + ' msec');

    if (exitCode) {
        phantom.exit(exitCode);
    } else {
        phantom.exit(0);
    }
};

var assertEquals = function (expectedValue, currentValue, msg) {
    if (currentValue !== expectedValue) {
        var m = 'Current value ' + currentValue + ' is different from expected value ' + expectedValue + '.';
        if (msg) {
            logError(m + ' ' + msg);
        } else {
            logError(m);
        }

        closePhantom(-1);
    }
};

var assertNotNull = function (value, msg) {
    if (value === undefined || value === null) {
        var m = 'Current value is not defined.';
        if (msg) {
            logError(m + ' ' + msg);
        } else {
            logError(m);
        }

        closePhantom(-1);
    }
};

var testCommon = {};
(function () {

    testCommon.getHashUrl = function (page) {
        var currentUrl = page.evaluate(function () {
            return location.hash;
        });
        return decodeURIComponent(currentUrl);
    };

    testCommon.startTime = Date.now();

    testCommon.fireEvent = function (ElementId, EventName) {
        if (document.getElementById(ElementId) != null) {
            if (document.getElementById(ElementId).fireEvent) {
                document.getElementById(ElementId).fireEvent('on' + EventName);
            } else {
                var evObj = document.createEvent('Events');
                evObj.initEvent(EventName, true, false);
                document.getElementById(ElementId).dispatchEvent(evObj);
            }
        }
    };

    testCommon.currentStep = -1;

    testCommon.repeatCurrentStep = false;

    testCommon.currentStepStartTime = 0;

    // max time is 60 seconds for one step
    testCommon.defaultMaxTimToExecutionStep = 60000;

    testCommon.createNextTask = function (steps, delay) {
        if(!testCommon.repeatCurrentStep){
            testCommon.currentStep++;
            testCommon.currentStepStartTime = new Date();
        } else {
            var t = new Date();
            var maxTime = testCommon.defaultMaxTimToExecutionStep;
            if(steps.maxTimeToExecutionStep){
                maxTime = steps.maxTimeToExecutionStep;
            }

            if((t - testCommon.currentStepStartTime) > maxTime){
                logError('Timeout execution step. Max time was: ' + maxTime +' ms. Test failed.');
                closePhantom(-1);
                return;
            }
        }


        setTimeout(function () {
            var msg = 'STEP ' + testCommon.currentStep;

            if (steps[testCommon.currentStep].description) {
                msg += ': ' + steps[testCommon.currentStep].description;
            }
            log(msg);
            
            page.render(scriptName + '_step' + testCommon.currentStep + '-begin.png');
            steps[testCommon.currentStep].fn();
            page.render(scriptName + '_step' + testCommon.currentStep + '-end.png');
            if (testCommon.currentStep + 1 < steps.length) {
                testCommon.createNextTask(steps, steps[testCommon.currentStep].delay);
            }

        }, delay);
    };

    testCommon.startTest = function (steps) {
        testCommon.createNextTask(steps, 0);
    };

    testCommon.waitForLoadingResource = function(resource){
            if(testCommon.startTaskTime == null){
                testCommon.startTaskTime = new Date();
            }

            if(testCommon.checkResourceError(resource)){
                logError("Reading resource file " + resource + " failed.");
                closePhantom(-1);
            }

            // check if service was loaded that means we can continue
            if(testCommon.checkResourceLoaded(resource)) {
                var t = new Date();
                log('Loading page took ' + (t - testCommon.startTaskTime) + 'ms');
                testCommon.startTaskTime = null;
                testCommon.repeatCurrentStep = false;
            }else{
                testCommon.repeatCurrentStep = true;
            }
    };
    testCommon.startTaskTime = null;

    testCommon.resourceReceivedCount = 0;
    testCommon.resourceReceived = [];
    testCommon.resourceError = [];
    testCommon.loadFinished = false;

    testCommon.clearResourceLogs = function(){
        testCommon.resourceError = [];
        testCommon.resourceReceived = [];
        testCommon.resourceReceivedCount = 0;
    };

    testCommon.checkResourceLoaded = function(str){
        for(var i = 0; i < testCommon.resourceReceived.length; i++){
            if(testCommon.resourceReceived[i].indexOf(str) > -1){
                return true;
            }
        }
        return false;
    };

    testCommon.checkResourceError = function(str){

        for(var i =0; i < testCommon.resourceError.length; i++){
            if(testCommon.resourceError[i].indexOf(str) > -1){
                return true;
            }
        }
        return false;
    }

    testCommon.loadPageByRepetition = function(){
        if(testCommon.startTaskTime == null) {
            testCommon.startTaskTime = new Date();
        }

        if(testCommon.loadFinished) {
            var t = new Date();
            log('Loading login page took ' + (t- testCommon.startTaskTime) + 'ms');
            testCommon.startTaskTime = null;
            testCommon.repeatCurrentStep = false;
            testCommon.loadFinished = false;
        } else {
            testCommon.repeatCurrentStep = true;
        }

    };
    
})();
// end of common functionality

initPage(page);

var steps = [
 {
	 // CEM login
     'fn': function () {
    	 var pageUrl = "http://"+cemHostname+":"+cemPort+"/wily/cem/tess/app/login.html";
    	 log("opening page:"+pageUrl);
         
    	 page.open(pageUrl, function(status) {
    		 assertEquals('success', status, 'FAIL to load the Login Page');
    	 });
     },
     'delay': 100,
     'description': 'Opening CEM Login Page'
 },
 {
     fn: testCommon.loadPageByRepetition,
     'delay': 100,
     'description': 'Waiting for loading login page',
     'maxTimeToExecutionStep' : 15000
 },
 {
     'fn': function () {
         // Enter Credentials
         page.evaluate(function () {
             var formElement = document.forms.namedItem("loginForm");
             console.log('Filling credentials.');
             formElement.elements["loginForm:loginId_userName"].value = "Admin";
             formElement.elements["loginForm:loginId_passWord"].value = "";
             formElement.submit();
         });
     },
     'delay': 500,
     'description': 'Writing login credentials'
 },
 {
     fn: testCommon.loadPageByRepetition,
     'delay': 100,
     'description': 'Waiting for loading home page',
     'maxTimeToExecutionStep' : 15000
 },
 {
     'fn': function () {
    	 var pageUrl = "http://"+cemHostname+":"+cemPort+"/wily/cem/tess/app/admin/tranDefGroupNew.html";
    	 log("opening page:"+pageUrl);
    	 
    	 page.open(pageUrl, function(status) {
    		 assertEquals('success', status, 'FAIL to load the New Business Service Page');
    	 });
     },
     'delay': 100,
     'description': 'Opening form for new Business Service in CEM'
 },
 {
     fn: testCommon.loadPageByRepetition,
     'delay': 100,
     'description': 'Waiting for loading new Business Service form in CEM',
     'maxTimeToExecutionStep' : 15000
 },
 {
     'fn': function () {
         page.evaluate(function () {
        	 var formElement = document.forms.namedItem("main");
        	 console.log('Filling form for new Business Service.');
             formElement.elements["name"].value = "Trading Service";
             formElement.elements["description"].value = "Service for testing purposes";
             formElement.elements["_finish"].click();
         });
     },
     'delay': 500,
     'description': 'Creating "Trading Service" Business Service'
 },
 {
     fn: testCommon.loadPageByRepetition,
     'delay': 100,
     'description': 'Waiting for loading Business Service list in CEM',
     'maxTimeToExecutionStep' : 15000
 },
 {
     'fn': function () {
    	 var tradingServiceLinkElementId = page.evaluate(function () {
    		// TODO: it would be nice to use jQuery
    		// var tradingServiceLinkElement = $("a:contains('Trading Service')");
	    	return [].filter.call(document.querySelectorAll('#treeDiv a'), function(aEl) {
	    		  return aEl.textContent == 'Trading Service';
	    		})[0].id; 
    	 });
    	 
    	 assertNotNull(tradingServiceLinkElementId, 'Required Business Service has not been created!');
    	 
         page.evaluate(function (linkElementId) {
        	 var linkElement = document.getElementById(linkElementId);
             console.log('Following link for Business Service:' + linkElement.href);
             
             function fireEvent(element, EventName) {
                 if (element.fireEvent) {

                     element.fireEvent('on' + EventName);
                     console.log('element.fireEvent');
                 }
                 else {
                     var evObj = document.createEvent('Events');
                     evObj.initEvent(EventName, true, false);
                     element.dispatchEvent(evObj);
                     console.log('element.dispatchEvent');
                 }
             };
             fireEvent(linkElement, 'click');
         }, tradingServiceLinkElementId);
     },
     'delay': 500,
     'description': 'Opening detail of "Trading Service" Business Service in CEM'
 },
 {
     fn: testCommon.loadPageByRepetition,
     'delay': 100,
     'description': 'Waiting for loading Business Service detail in CEM',
     'maxTimeToExecutionStep' : 15000
 },
 {
     'fn': function () {
         page.evaluate(function () {
        	 var formElement = document.forms.namedItem("main");
             console.log('Following link to import Business Transaction definition');
             
             formElement.elements["import"].click();
         });
     },
     'delay': 500,
     'description': 'Opening form to import Business Transaction definition in CEM'
 },
 {
     fn: testCommon.loadPageByRepetition,
     'delay': 100,
     'description': 'Waiting for loading Business Service import form in CEM',
     'maxTimeToExecutionStep' : 15000
 },
 {
     'fn': function () {
    	 page.uploadFile('input[name=importFile]', btExportsForImport[0]);
         page.evaluate(function () {
        	 var formElement = document.forms.namedItem("main");
        	 console.log('Filling form for import of Business Transactions.');
             formElement.elements["import"].click();
         });
     },
     'delay': 500,
     'description': 'Importing BT definition for "Trading Service" Business Service'
 },
 {
     fn: testCommon.loadPageByRepetition,
     'delay': 100,
     'description': 'Waiting for importing BT definitions in CEM',
     'maxTimeToExecutionStep' : 30000
 },
 {
     'fn': function () {
         var successfullyImported = page.evaluate(function () {
        	 var allMessagesSpanElements = document.querySelectorAll('div#caMessagesDiv span');
        	 var succImportedPattern = new RegExp("^Successfully imported .*");
        	 return [].some.call(allMessagesSpanElements, function(messagesSpanElement) {
        		 return succImportedPattern.test(messagesSpanElement.textContent);
        	 });
         });
         assertEquals(true, successfullyImported, 'Definition of Business Transactions not been successfully imported!');
     },
     'delay': 100,
     'description': 'Assert Business Transactions has been successfully imported'
 },
 {
     'fn': function () {
    	 var pageUrl = "http://"+cemHostname+":"+cemPort+"/wily/cem/tess/app/admin/monitorNew.html";
    	 log("opening page:"+pageUrl);
    	 
    	 page.open(pageUrl, function(status) {
    		 assertEquals('success', status, 'FAIL to load the New Monitor Page');
    	 });
     },
     'delay': 100,
     'description': 'Opening form for new Monitor in CEM'
 },
 {
     fn: testCommon.loadPageByRepetition,
     'delay': 100,
     'description': 'Waiting for loading new Monitor form in CEM',
     'maxTimeToExecutionStep' : 15000
 },
 {
     'fn': function () {
         page.evaluate(function (hostname, ipAddress) {
        	 var formElement = document.forms.namedItem("main");
             console.log('Filling form for new Business Service.');
             formElement.elements["name"].value = hostname;
             formElement.elements["ipAddressAsString"].value = ipAddress;
             formElement.elements["_finish"].click();
         }, timHostname, timIPAddress);
     },
     'delay': 500,
     'description': 'Creating TIM Monitor'
 },
 {
     fn: testCommon.loadPageByRepetition,
     'delay': 100,
     'description': 'Waiting for loading Monitor list in CEM',
     'maxTimeToExecutionStep' : 15000
 },
 {
     'fn': function () {
         page.evaluate(function (monitorName) {
        	 var monitorTableRowElement = [].filter.call(document.querySelectorAll('table#monitor tbody tr'), function(trEl) {
        		  return trEl.querySelector('tr a').textContent == monitorName;
        		})[0];
        	 
             console.log('Checking monitor:' + monitorTableRowElement.querySelector('tr a').textContent);
             monitorTableRowElement.querySelector('tr input[type=checkbox]').click();
             
             var formElement = document.forms.namedItem("main");
             formElement.elements["enable"].click();
         }, timHostname);
     },
     'delay': 500,
     'description': 'Enabling new TIM Monitor created in CEM'
 },
 {
     fn: testCommon.loadPageByRepetition,
     'delay': 100,
     'description': 'Waiting for enabling required Monitor in CEM',
     'maxTimeToExecutionStep' : 30000
 },
 {
     'fn': function () {
         var successfullyImported = page.evaluate(function () {
        	 var allMessagesSpanElements = document.querySelectorAll('div#caMessagesDiv span');
        	 var succImportedPattern = new RegExp("^Successfully enabled .*");
        	 return [].some.call(allMessagesSpanElements, function(messagesSpanElement) {
        		 return succImportedPattern.test(messagesSpanElement.textContent);
        	 });
         });
         assertEquals(true, successfullyImported, 'Create TIM Monitor has not been successfully enabled!');
     },
     'delay': 100,
     'description': 'Assert Monitor has been successfully enabled'
 },
 {
     'fn': function () {
         page.evaluate(function () {
             var formElement = document.forms.namedItem("main");
             formElement.elements["synchronizeMonitors"].click();
         });
     },
     'delay': 500,
     'description': 'Synchronizing all TIM Monitors in CEM'
 },
 {
     fn: testCommon.loadPageByRepetition,
     'delay': 100,
     'description': 'Waiting for synchronizing all TIM Monitors in CEM',
     'maxTimeToExecutionStep' : 15000
 },
 {
     'fn': function () {
         var monitorsSynchronized = page.evaluate(function () {
        	 var allMessagesSpanElements = document.querySelectorAll('div#navTrailDiv span');
        	 var synchronizedPattern = new RegExp("^Monitors are synchronized");
        	 return [].some.call(allMessagesSpanElements, function(messagesSpanElement) {
        		 return synchronizedPattern.test(messagesSpanElement.textContent);
        	 });
         });
         assertEquals(true, monitorsSynchronized, 'TIM Monitors are not synchronized!');
     },
     'delay': 100,
     'description': 'Assert Monitors are synchronized'
 },
 {
	'fn': function () {
		closePhantom(0);
	},
	'delay': 0,
	'description': 'TEST PASSED. Exiting phantomjs.'
 }
];

// start test
testCommon.startTest(steps);
