var CYCLES_NUM = 50;

function buildCharts(chromeData, firefoxData, ieData, edgeData) {
    chromeData = initDataArray(chromeData);
    firefoxData = initDataArray(firefoxData);
    ieData = initDataArray(ieData);
    edgeData = initDataArray(edgeData);

    var samplesChart = AmCharts.makeChart("samplesChartDiv", {
        "fontFamily": "\"CA Sans Web\",Tahoma",
        "fontSize": 13,
        "type": "serial",
        "theme": "dark",
        "dataDateFormat": "YYYY-MM-DD",
        "categoryField": "id",
        "startAlpha": 0,
        "dataProvider": chromeData,

        "legend": {
            "equalWidths": true,
            "useGraphSettings": true,
            "valueAlign": "left",
            "valueWidth": 120
        },
        "valueAxes": [
            {
                "id": "durationAxis",
                "axisColor": "#B0DE09",
                "axisAlpha": 1,
                "gridColor": "#a6a6a6",
                "gridAlpha": 0.5,
                "position": "left",
                "title": "Duration (seconds)",
                "unit": "s",
                "labelsEnabled": true,
                "minimum": 0,
                "stackType": "regular"
            },
            {
                "id": "memoryAxis",
                "axisColor": "#FCD202",
                "axisAlpha": 1,
                "gridAlpha": 0.1,
                "position": "right",
                "title": "Memory",
                "unit": " bytes",
                "labelsEnabled": true
            }
        ],
        "graphs": [
            {
                "animationPlayed": true,
                //"lineColor": "#fcd202",
                "lineColor" : "#b0de09",
                "alphaField": "alpha",
                "balloonText": "[[value]] s",
                "fillAlphas": 1,
                "legendPeriodValueText": "(Total: [[value.sum]] s)",
                "legendValueText": "[[value]] s",
                "title": "Chrome",
                "type": "column",
                "valueField": "duration",
                "valueAxis": "durationAxis"
            },
            {
                "animationPlayed": true,
                "lineColor": "#ff9500",
                "dataProvider": firefoxData,
                "newStack": true,
                "alphaField": "alpha",
                "balloonText": "[[value]] s",
                "fillAlphas": 1,
                "legendPeriodValueText": "(Total: [[value.sum]] s)",
                "legendValueText": "[[value]] s",
                "title": "FireFox",
                "type": "column",
                "valueField": "duration",
                "valueAxis": "durationAxis"
            },
            {
                "animationPlayed": true,
                "lineColor": "#0d8ecf",
                "dataProvider": ieData,
                "newStack": true,
                "alphaField": "alpha",
                "balloonText": "[[value]] s",
                "fillAlphas": 1,
                "legendPeriodValueText": "(Total: [[value.sum]] s)",
                "legendValueText": "[[value]] s",
                "title": "Internet Explorer",
                "type": "column",
                "valueField": "duration",
                "valueAxis": "durationAxis"
            },
            {
                "animationPlayed": true,
                "lineColor": "#c4383f",
                "dataProvider": edgeData,
                "newStack": true,
                "alphaField": "alpha",
                "balloonText": "[[value]] s",
                "fillAlphas": 1,
                "legendPeriodValueText": "(Total: [[value.sum]] s)",
                "legendValueText": "[[value]] s",
                "title": "Edge",
                "type": "column",
                "valueField": "duration",
                "valueAxis": "durationAxis"
            },
            {
                "alphaField": "alpha",
                "balloonText": "[[value]] bytes",
                "fillAlphas": 0.5,
                "legendPeriodValueText": "(Total: [[value.sum]] bytes)",
                "legendValueText": "[[value]] bytes",
                "title": "JavaScript Heap Size (Chrome)",
                "type": "smoothedLine",
                "valueField": "jsHeapSize", // "javaScriptHeap",
                "valueAxis": "memoryAxis"
            }
        ],
        "chartCursor": {
            "categoryBalloonDateFormat": "DD",
            "cursorAlpha": 0.1,
            "fullWidth": true,
            "valueBalloonsEnabled": true,
            "zoomable": false
        },
        "categoryAxis": {
            "minorGridEnabled": true,
            "autoGridCount": false,
            //"axisColor": "#555555",
            "gridAlpha": 0.3,
            "gridColor": "#FFFFFF",
            "gridCount": 50
        },
        "export": {
            "enabled": false
        }
    });

    var chromeAvgData = calcAverageData(chromeData, "Chrome");
    var firefoxAvgData = calcAverageData(firefoxData, "Firefox");
    var ieAvgData = calcAverageData(ieData, "IE");
    var edgeAvgData = calcAverageData(edgeData, "Edge");

    var averagesChart = AmCharts.makeChart("averagesChartDiv", {
        "fontFamily": "\"CA Sans Web\",Tahoma",
        "fontSize": 13,
        "type": "serial",
        "theme": "dark",
        "dataDateFormat": "YYYY-MM-DD",
        "categoryField": "id",
        "startAlpha": 0,
        "dataProvider": chromeAvgData,

        "legend": {
            "equalWidths": true,
            "useGraphSettings": true,
            "valueAlign": "left",
            "valueWidth": 120
        },
        "valueAxes": [
            {
                "id": "durationAxis",
                "axisColor": "#B0DE09",
                "axisAlpha": 1,
                "gridColor": "#a6a6a6",
                "gridAlpha": 0.5,
                "position": "left",
                "title": "Duration (seconds)",
                "unit": "s",
                "labelsEnabled": true,
                "minimum": 0,
                "stackType": "regular"
            }
        ],
        "graphs": [
            {
                "animationPlayed": true,
                "lineColor" : "#b0de09",
                "alphaField": "alpha",
                "balloonText": "[[value]] s",
                "fillAlphas": 1,
                "legendPeriodValueText": "(Total: [[value.sum]] s)",
                "legendValueText": "[[value]] s",
                "title": "Chrome",
                "type": "column",
                "valueField": "averageDuration",
                "valueAxis": "durationAxis"
            },
            {
                "animationPlayed": true,
                "lineColor": "#ff9500",
                "dataProvider": firefoxAvgData,
                "newStack": true,
                "alphaField": "alpha",
                "balloonText": "[[value]] s",
                "fillAlphas": 1,
                "legendPeriodValueText": "(Total: [[value.sum]] s)",
                "legendValueText": "[[value]] s",
                "title": "FireFox",
                "type": "column",
                "valueField": "averageDuration",
                "valueAxis": "durationAxis"
            },
            {
                "animationPlayed": true,
                "lineColor": "#0d8ecf",
                "dataProvider": ieAvgData,
                "newStack": true,
                "alphaField": "alpha",
                "balloonText": "[[value]] s",
                "fillAlphas": 1,
                "legendPeriodValueText": "(Total: [[value.sum]] s)",
                "legendValueText": "[[value]] s",
                "title": "Internet Explorer",
                "type": "column",
                "valueField": "averageDuration",
                "valueAxis": "durationAxis"
            },
            {
                "animationPlayed": true,
                "lineColor": "#c4383f",
                "dataProvider": edgeAvgData,
                "newStack": true,
                "alphaField": "alpha",
                "balloonText": "[[value]] s",
                "fillAlphas": 1,
                "legendPeriodValueText": "(Total: [[value.sum]] s)",
                "legendValueText": "[[value]] s",
                "title": "Edge",
                "type": "column",
                "valueField": "averageDuration",
                "valueAxis": "durationAxis"
            }
        ],
        "chartCursor": {
            "categoryBalloonDateFormat": "DD",
            "cursorAlpha": 0.1,
            "fullWidth": true,
            "valueBalloonsEnabled": true,
            "zoomable": false
        },
        "categoryAxis": {
            "minorGridEnabled": true,
            "autoGridCount": false,
            //"axisColor": "#555555",
            "gridAlpha": 0.3,
            "gridColor": "#FFFFFF",
            "gridCount": 3
        },
        "export": {
            "enabled": false
        }
    });


    var errorsDivElem = document.getElementById("errorsDiv");
    //var chromeErrorsCount = 0;
    //var firefoxErrorsCount = 0;
    //var ieErrorsCount = 0;
    //var edgeErrorsCount = 0;
    var chromeErrors = getErrorsFromDataArray(chromeData);
    appendErrorsSection(chromeErrors, "Chrome", "chromeBrowser", errorsDivElem);
    var firefoxErrors = getErrorsFromDataArray(firefoxData);
    appendErrorsSection(firefoxErrors, "Firefox", "firefoxBrowser", errorsDivElem);
    var ieErrors = getErrorsFromDataArray(ieData);
    appendErrorsSection(ieErrors, "Internet Explorer 11", "ieBrowser", errorsDivElem);
    var edgeErrors = getErrorsFromDataArray(edgeData);
    appendErrorsSection(edgeErrors, "Edge", "edgeBrowser", errorsDivElem);
}

function appendErrorsSection(browserErrors, browserName, browserSpecificClassName, errorsDivElem) {
    var pEl = document.createElement("p");
    var browserNameHeader = document.createElement("h4");
    browserNameHeader.className = "errorBrowserHeader " + browserSpecificClassName;
    browserNameHeader.innerHTML = browserName;
    pEl.appendChild(browserNameHeader);

    if (browserErrors.length > 0) {
        for (var i = 0; i < browserErrors.length; i++) {
            var iErr = browserErrors[i];
            var err = iErr.errorMessage;
            var screenshot = iErr.screenshot;
            var divEl = document.createElement("div");
            divEl.className += browserSpecificClassName;
            divEl.innerHTML = i + ". " + err;
            pEl.appendChild(divEl);
            if (screenshot) {
                var aEl = document.createElement("a");
                aEl.className = "errorScreenshot " + browserSpecificClassName;
                var screenshotFileName = screenshot + ".png";
                aEl.setAttribute("href", screenshotFileName);
                aEl.setAttribute("target", "_blank");
                aEl.innerHTML = "Screenshot: " + screenshotFileName;
                pEl.appendChild(aEl);
                pEl.appendChild(document.createElement("br"));
            }
            pEl.appendChild(document.createElement("br"));
        }
    } else {
        var divEl = document.createElement("div");
        divEl.className += browserSpecificClassName;
        divEl.innerHTML += "0";
        pEl.appendChild(divEl);
        pEl.appendChild(document.createElement("br"));
    }

    errorsDivElem.appendChild(pEl);
}

function getErrorsFromDataArray(dataArray) {
    var errors = [];
    for (var i = 0; i < dataArray.length; i++) {
        var data = dataArray[i];
        if (data.errorMessage) {
            errors.push({ "errorMessage" : data.errorMessage, "screenshot" : data.screenshotName });
        }
    }
    return errors;
}

function calcAverageData(dataArray, browser) {
    var max = 0;
    var min = 0;
    var sum = 0;
    for (var i = 0; i < dataArray.length; i++) {
        var iDuration = dataArray[i].duration;
        sum += iDuration;
        max = Math.max(max, iDuration);
        min = Math.min(min, iDuration);
    }

    var result = [{
        id: 1,
        averageDuration: Math.floor(sum / dataArray.length),
        maxDuration: max,
        minDuration: min,
        browser: browser
    }];
    return result;
}

function initDataArray(dataArray) {
    if (!dataArray) {
        dataArray = [];
    }

    for (var i = 0; i < CYCLES_NUM; i++) {
        if (i > dataArray.length - 1) {
            dataArray.push(
                {
                    "id": i + 1,
                    "duration": 0,
                    "jsHeapSize": 0
                }
            );
        } else {
            var iData = dataArray[i];
            if (!iData.id) {
                iData.id = "" + (i + 1);
            }
        }
    }

    return dataArray;
}