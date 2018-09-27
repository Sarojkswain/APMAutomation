var system = require('system');
var page = require('webpage').create();
var address;



//common functionality - TODO move to extra file
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
    page.onConsoleMessage = function (msg) {
        log('CONSOLE: ' + msg);
    };

    page.viewportSize = {width: 1920, height: 1080};

    page.onError = function (msg, trace) {
        log('Page onerror:' + msg);
        trace.forEach(function (item) {
            console.log('  ', item.file, ':', item.line);
        });
        phantom.exit(-1);
    };
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


var testCommon = {};
(function () {


    testCommon.getBusinessTransactionNodesCount = function (page, text) {

        var nodes = page.evaluate(function (t) {
            var count = 0;
            var nodeList = document.querySelectorAll('#graphCanvas tspan[data-type="BUSINESSTRANSACTION"]');
            if(t) {
                for (var i = 0; i < nodeList.length; ++i) {
                    if (nodeList[i].textContent === t) {
                        count++;
                    }
                }
            }else{
                count = nodeList.length;
            }
            return count;
        }, text);

        return nodes;
    };


    testCommon.businessTransactionNodeInvokeMouseOver = function (page, text) {

        var position = page.evaluate(function (t) {

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


            var count = 0;
            var nodeList = document.querySelectorAll('#graphCanvas tspan[data-type="BUSINESSTRANSACTION"]');
            if (nodeList.length > 0) {

                var el;
                for (var i = 0; i < nodeList.length; ++i) {
                    if (nodeList[i].textContent === t) {
                        el = nodeList[i];
                    }
                }

                if (el) {
                    return {left: el.getClientRects()[0].left, top: el.getClientRects()[0].top};
                }

            }


        }, text);


        page.sendEvent('mousemove', position.left, position.top);

        return position;
    };


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
            }
            else {
                var evObj = document.createEvent('Events');
                evObj.initEvent(EventName, true, false);
                document.getElementById(ElementId).dispatchEvent(evObj);
            }
        }
    };

    testCommon.currentStep = -1;

    testCommon.repeatCurrentStep = false;

    testCommon.currentStepStartTime = 0;

    //max time is 60 seconds for one step
    testCommon.defaultMaxTimToExecutionStep = 60000;

    testCommon.createNextTask = function (steps, delay) {
        if(!testCommon.repeatCurrentStep){
            testCommon.currentStep++;
            testCommon.currentStepStartTime = new Date();
        }else{
            var t = new Date();
            var maxTime = testCommon.defaultMaxTimToExecutionStep;
            if (steps[testCommon.currentStep].maxTimeToExecutionStep) {
                maxTime = steps[testCommon.currentStep].maxTimeToExecutionStep;
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

            steps[testCommon.currentStep].fn();
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

            //check if service was loaded that means we can continue
            if(testCommon.checkResourceLoaded(resource)) {
                var t = new Date();
                log('Loading resource "' + resource + '" took ' + (t - testCommon.startTaskTime) + 'ms');
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

})();
//end of common functionality


initPage(page);

address = system.args.length > 1 ? system.args[1] : "http://${machine.endUserMachine.hostname}:${role.introscope.wvPort}/ApmServer";

page.onResourceReceived = function (response) {
    //log(response.url);
    testCommon.resourceReceivedCount++;
    testCommon.resourceReceived.push(response.url);
};

page.onResourceError = function(response) {
    testCommon.resourceError.push(response.url);
};


page.onLoadFinished = function(status){
    testCommon.loadFinished = true;
};

//parameters of the tests

var bt = 'Balances';
var bsUrl = 'Trading Service';
var bs = 'Trading Service';

var steps = [
    {
        'fn': function () {
            //Load Login Page
            log('opening ' + address);

            testCommon.loadFinished = false;

            page.open(address, function (status) {
                if (status !== 'success') {
                    logError('FAIL to load the address');
                }
            });


        },
        'delay': 100,
        'description': 'Opening login page'
    },
    {
        fn: function(){
            if(testCommon.startTaskTime == null){
                testCommon.startTaskTime = new Date();
            }

            if(testCommon.loadFinished){
                var t = new Date();
                log('Loading login page took ' + (t- testCommon.startTaskTime) + 'ms');
                testCommon.startTaskTime = null;
                testCommon.repeatCurrentStep = false;
            }else{
                testCommon.repeatCurrentStep = true;
            }

        },
        'delay': 100,
        'description': 'Waiting for loading login page',
        'maxTimeToExecutionStep' : 15000
    },
    {
        'fn': function () {
            //Enter Credentials
            page.render('screenshot1.png');
            var submitted = page.evaluate(function () {
                var iframe = document.getElementById("LoginFrame");

                if (iframe && (iframe.contentDocument !== null)) {
                    var arr = iframe.contentDocument.getElementsByName("loginForm");
                    var i;

                    console.log('Form elements count: ' + arr.length);

                    for (i = 0; i < arr.length; i++) {
                        if (arr[i].getAttribute('action') == "j_security_check") {
                            console.log('Filling credentials.');
                            arr[i].elements["j_username"].value = "Admin";
                            arr[i].elements["j_passWord"].value = "";
                            arr[i].submit();

                            return true;
                        }
                    }
                }
                return false;
            });
            page.render('screenshot2.png');
            testCommon.loadFinished = false;
            testCommon.repeatCurrentStep = !submitted;
        },
        'delay': 500,
        'description': 'Writing login credentials',
        'maxTimeToExecutionStep' : 4000
    },
    {
        fn: function(){

            if(testCommon.startTaskTime == null){
                testCommon.startTaskTime = new Date();
            }

            if(testCommon.loadFinished){
                testCommon.waitForLoadingResource('/api/service');

            }else{
                testCommon.repeatCurrentStep = true;
            }

        },
        'delay': 500,
        'description': 'Waiting for loading ApmServer/#/services page',
        'maxTimeToExecutionStep' : 20000
    },
    {
        'fn': function () {
            page.render('screenshot3.png');

            var menuItemVisible = page.evaluate(function () {
                return $('#menuItemMapId').css('display') === 'none';
            });

            assertEquals(false, menuItemVisible, 'Menu item Map is visible and should not be before selecting application.');


            //check if service exists
            var businessServiceExists = page.evaluate(function (bs) {
                //select service
                var el = $('#businessServiceTrendCardId').find('[header="' + bs + '"]');
                //check if exists
                if (el == null){
                    return false;
                }
                el.click();
                return true;
            }, bs);

            assertEquals(true, businessServiceExists, 'Business Service "' + bs +'" on trend view card does not exists.');

            testCommon.clearResourceLogs();

            page.evaluate(function (bs) {
                //go to map trough menu button map
                $('#menuItemMapId').click();
            }, bs);

        },
        'delay': 100,
        'description': 'Finding business transaction and make click on it.'
    },
    {
         fn: function(){
             //check if url was changed
             log('Current HashUrl: ' + testCommon.getHashUrl(page));
             testCommon.repeatCurrentStep = '#/map?bsf=["' + bsUrl + '"]' != testCommon.getHashUrl(page);
         },
         'delay': 500,
         'description': 'Checking if URL was changed.',
         'maxTimeToExecutionStep' : 20000
     },
    /*{
        fn: function(){
            testCommon.waitForLoadingResource('/api/graph');

        },
        'delay': 100,
        'description': 'Waiting for loading resource /api/graph service.',
        'maxTimeToExecutionStep' : 15000
    },*/
    {
        'fn': function () {
            var nodesCount = testCommon.getBusinessTransactionNodesCount(page);
            if(nodesCount == 0){
                testCommon.repeatCurrentStep = true;
            }else{
                testCommon.repeatCurrentStep = false;
            }

        },
        'delay': 500,
        'description': 'Waiting for map loading.',
        'maxTimeToExecutionStep' : 15000
    },
    {
        'fn': function () {

            page.render('screenshot4.png');

            var nodesCount = testCommon.getBusinessTransactionNodesCount(page, bt);

            assertEquals(1, nodesCount);

            testCommon.businessTransactionNodeInvokeMouseOver(page, bt);
        },
        'delay': 1000,
        'description': 'Finding node in map. Do mouse over the map.'
    },
    {
        'fn': function () {
            page.render('screenshot5.png');
        }, 'delay': 50,
        'description': 'Capturing screenshot with highlighted path.'
    },
    {
        'fn': function () {
            closePhantom(0);
        },
        'delay': 0,
        'description': 'TEST PASSED. Exiting phantomjs.'
    }
];

//start test
testCommon.startTest(steps);
