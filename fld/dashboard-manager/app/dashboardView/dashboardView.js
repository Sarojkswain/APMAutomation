'use strict';

var runContexts = {};
var MILLIS_IN_HOUR = 1000 * 60 * 60;
var MILLIS_IN_MINUTE = 1000 * 60;
var PROCESS_IMAGE_UPDATE_ITERATIONS = 2;
var LOGS_CHUNK = 30;

angular.module('fldDashboardApp.dashboardView', ['ngRoute'])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/dashboardView/:dashboardId', {
            templateUrl: 'dashboardView/dashboardView.html',
            controller: 'DashboardDetailsController',
            controllerAs: 'controller'
        })
    }])
    .controller('DashboardDetailsController', ['$sce', '$http', '$scope', '$routeParams', 'appEditStateService',
        '$interval', 'launchConfigSettingsModalService', '$location', 'modalService', '$rootScope',
        'userTaskModalService', 'stuckJobModalService', 'singleTextBoxModalService',
        function ($sce, $http, $scope, $routeParams, appEditStateService, $interval, launchConfigSettingsModalService,
                  $location, modalService, $rootScope, userTaskModalService, stuckJobModalService, singleTextBoxModalService) {
            var idParam = parseInt($routeParams.dashboardId);

            if (idParam < 1) {
                alert('Invalid dashboard Id: ' + $routeParams.dashboardId);
                return;
            }

            var self = this;

            $scope.displayLogsChunk = LOGS_CHUNK;

            var runContext = runContexts[$routeParams.dashboardId];
            if (!runContext) {
                runContext = {};
                runContext.fetchedLogEntries = [];
                runContext.fetchedLogEntriesWindow = [];
                runContext.liveLogsAccumulationStack = [];
                runContexts[$routeParams.dashboardId] = runContext;
                runContext.isLogPanelCollapsed = true;
                runContext.isLogFilterPanelCollapsed = true;
                runContext.isProcessWorkflowPanelCollapsed = true;
                //these will contain filter strings after pressing Apply
                runContext.categoryFilterString = null;
                runContext.tagFilterString = null;
                runContext.levelFilterString = 'ALL';
                //for temporal editing filter strings
                runContext.tmpDateFilter = new Date();
                runContext.tmpCategoryFilterString = null;
                runContext.tmpTagFilterString = null;
                runContext.tmpLevelFilterString = 'ALL';
                runContext.isDatePickerOpened = false;
                runContext.isDashboardInfoCollapsed = true;
                runContext.isProcessConfigurationParametersInfoCollapsed = true;
                runContext.isProcessInstanceInfoCollapsed = true;
                runContext.currentLogPageNumber = 1;
                runContext.previousLogPageNumber = 1;
                runContext.isLogPollingOn = true;
                runContext.lastBeforeId = null;
                runContext.isInfiniteScrollingMode = false;
            } else {
                if (!runContext.isLogPanelCollapsed) {
                    angular.element(document.getElementById('logPanelToggleButton')).addClass('active');
                }
                if (!runContext.isProcessWorkflowPanelCollapsed) {
                    angular.element(document.getElementById('processWorkflowPanelToggleButton')).addClass('active');
                }
                if (!runContext.isInfiniteScrollingMode) {
                    angular.element(document.getElementById('logBrowsingModeToggleButton')).addClass('active');
                }
            }

            $scope.runContext = runContext;
            $scope.runContext.processImageIteration = 0;
            $scope.runContext.disableUserTaskButton = false;
            $scope.runContext.procInstanceIdsMap = {};

            var trusted = {};

            this.getPopoverContentForWidget = function (widget) {
                var content = "<b>Name:</b>&nbsp;" + widget.name + "<br/><b>Key:</b>&nbsp;&nbsp;&nbsp;&nbsp;" + widget.key + "<br/><b>Value:</b>&nbsp;" + widget.value;
                return trusted[content] || (trusted[content] = $sce.trustAsHtml(content));
            };

            $scope.getPopoverContentForWidget = this.getPopoverContentForWidget;

            this.init = function (dashboardId) {
                var dashboard = appEditStateService.getLocalDashboardById(dashboardId);
                if (!dashboard) {
                    appEditStateService.getDashboardById(dashboardId,
                        function (data) {
                            self.initForDashboard(data);
                        },
                        function (data, status) {
                            alert('Failed to fetch dashboard with id=' + dashboardId +
                            '. Server Status Code: ' + status + ', Server Data: ' + data);
                        }
                    );
                } else {
                    self.initForDashboard(dashboard);
                }
            };

            this.initForDashboard = function (dashboard) {
                $scope.dashboard = dashboard;
                $scope.monitors = $scope.dashboard.monitors ? appEditStateService.normalizeDashboardMonitors($scope.dashboard.monitors) : [];

                $scope.elapsedTime = self.calculateElapsedTime();

                if ($scope.dashboard) {
                    if ($scope.dashboard.active) {
                        if (!self.stopFunction) {
                            self.stopFunction = self.createMonitorPollingFunction();
                        }
                    } else if (!$scope.dashboard.active) {
                        if (self.stopFunction) {
                            self.destroyIntervalFunction();
                        }
                        if (!$scope.runContext.isProcessWorkflowPanelCollapsed) {
                            self.handleProcessStoppedEvent();
                        }
                    }

                    //read process config parameter key translations
                    if ($scope.dashboard.config) {
                        var keys = [];
                        for (var i = 0; i < $scope.dashboard.config.length; i++) {
                            var configParam = $scope.dashboard.config[i];
                            keys.push(configParam.name);
                        }
                        if (keys.length > 0) {
                            appEditStateService.fetchProcessKeyLocalization(keys);
                        }
                    }
                }
            };

            this.toggleDashboardInfo = function () {
                $scope.runContext.isDashboardInfoCollapsed = !$scope.runContext.isDashboardInfoCollapsed;
            };

            this.toggleProcessConfigurationParametersInfo = function () {
                $scope.runContext.isProcessConfigurationParametersInfoCollapsed = !$scope.runContext.isProcessConfigurationParametersInfoCollapsed;
            };

            this.toggleProcessInstanceInfo = function () {
                $scope.runContext.isProcessInstanceInfoCollapsed = !$scope.runContext.isProcessInstanceInfoCollapsed;
            };

            this.setProcRun = function (procRun) {
                $scope.runContext.tmpProcRun = procRun;
            };

            this.setLiveProcRun = function (procInstId) {
                $scope.runContext.tmpProcRun = { id : procInstId, isLive: true };
            };

            this.loadHistoricalRuns = function (id) {
                var dId = $scope.dashboard ? $scope.dashboard.id : id;
                if (dId) {
                    appEditStateService.getHistoricalProcessInstances(dId,
                        function (data) {
                            $scope.runContext.procRuns = data.processInstances;
                        },
                        function (data, status) {
                        }
                    );
                }
            };

            this.setLevelFilterString = function (level) {
                $scope.runContext.tmpLevelFilterString = level;
            };

            this.getDisplayLogEntries = function () {
                return $scope.runContext.isInfiniteScrollingMode ? $scope.runContext.liveLogsAccumulationStack : runContext.fetchedLogEntriesWindow;
            };

            this.fetchScrolledLogChunk = function () {
                if ($scope.runContext.isInfiniteScrollingMode && $scope.dashboard && !$scope.isLoadSpinnerOn) {
                    var logBuffer = $scope.runContext.liveLogsAccumulationStack;
                    var beforeId = -1;
                    if (logBuffer && logBuffer.length > 0) {
                        beforeId = logBuffer[logBuffer.length - 1].id;
                    }
                    if (beforeId !== $scope.runContext.lastBeforeId) {
                        $scope.runContext.lastBeforeId = beforeId;
                        self.updateLogs($scope.dashboard.id, beforeId);
                    }
                }
            };

            this.toggleLogBrowsingMode = function () {
                if (!$scope.runContext.isInfiniteScrollingMode) {
                    $scope.runContext.fetchedLogEntriesWindow = [];
                    $scope.runContext.fetchedLogEntries = [];
                    $scope.runContext.liveLogsAccumulationStack = [];
                    $scope.runContext.lastBeforeId = null;
                } else {
                    var endInd = LOGS_CHUNK > $scope.runContext.liveLogsAccumulationStack.length ? $scope.runContext.liveLogsAccumulationStack.length : LOGS_CHUNK;
                    $scope.runContext.fetchedLogEntriesWindow = $scope.runContext.liveLogsAccumulationStack.slice(0, endInd);
                }
                $scope.runContext.isInfiniteScrollingMode = !$scope.runContext.isInfiniteScrollingMode;
            };

            this.onLogPageChanged = function () {
                var curPage = $scope.runContext.currentLogPageNumber;
                var prevPage = $scope.runContext.previousLogPageNumber;


                if (prevPage === curPage) {
                    //This on change event is caused by a change of logsCountNumber,
                    //not by navigation through pages. We would like to ignore
                    //such events.
                    return;
                }

                //Disable the paginator
                $scope.isPaginationDisabled = true;

                var isInHistoryMode =
                    ($scope.runContext.procRun && !$scope.runContext.procRun.isLive) || //we are looking at a finished run
                    curPage > 1;//we go back in history and thus would like to stop polling

                if (isInHistoryMode) {
                    if ($scope.runContext.liveLogsAccumulationStack.length > 0) {
                        //We went to historical mode from the live one, need to build pages
                        //from accumulated buffer to reuse those pages without a need to call
                        //a REST service
                        $scope.runContext.fetchedLogEntries = [];
                        self.convertLiveLogsToHistoricalPages();
                        $scope.runContext.liveLogsAccumulationStack = [];
                    }

                    //If we already have the required page then show it right away.
                    var pageArray = $scope.runContext.fetchedLogEntries[curPage];
                    if (pageArray && pageArray.length > 0) {
                        $scope.runContext.fetchedLogEntriesWindow = pageArray;
                        //Enable the paginator
                        $scope.isPaginationDisabled = false;
                    } else {
                        //Find a closest to the required page we already have
                        var closestPageNum = self.getClosestPageNumber(curPage);
                        var closestPage = $scope.runContext.fetchedLogEntries[closestPageNum];
                        var beforeId = closestPage[closestPage.length - 1].id;
                        var offset = (curPage - closestPageNum - 1) * LOGS_CHUNK;
                        self.updateLogs($scope.dashboard.id, beforeId, null, offset);
                    }
                } else {
                    $scope.runContext.fetchedLogEntriesWindow = $scope.runContext.fetchedLogEntries[1];
                    $scope.isPaginationDisabled = false;
                }

                //Set up the previous page number to the current one.
                $scope.runContext.previousLogPageNumber = curPage;

            };

            this.updateLogs = function (dashboardId, beforeId, afterId, offset) {
                if (!$scope.runContext.logsRequestJson) {
                    $scope.runContext.logsRequestJson = {};
                }

                var curPage = $scope.runContext.currentLogPageNumber;

                $scope.runContext.logsRequestJson.processInstanceId = $scope.runContext.procRun ? $scope.runContext.procRun.id : null;
                $scope.runContext.logsRequestJson.maxLogs = LOGS_CHUNK;
                $scope.runContext.logsRequestJson.categoryFilter = $scope.runContext.categoryFilterString;
                $scope.runContext.logsRequestJson.tagFilter = $scope.runContext.tagFilterString;

                if (!beforeId && !afterId) {
                    $scope.runContext.logsRequestJson.logsBeforeId = -1;
                    $scope.runContext.logsRequestJson.logsAfterId = null;
                } else {
                    $scope.runContext.logsRequestJson.logsBeforeId = beforeId;
                    $scope.runContext.logsRequestJson.logsAfterId = afterId;
                }
                if (offset) {
                    $scope.runContext.logsRequestJson.offset = offset;
                } else {
                    $scope.runContext.logsRequestJson.offset = 0;
                }
                $scope.runContext.isLoadSpinnerOn = true;
                appEditStateService.getProcessLogs(dashboardId, $scope.runContext.logsRequestJson,
                    function (logData) {
                        var logEntries = logData ? logData.logEntries : null;
                        if (logEntries && logEntries.length > 0) {
                            logEntries = logEntries.sort(function (entry1, entry2) {
                                return entry2.id - entry1.id;
                            });

                            var logsCount = (logData.logsCount && logData.logsCount >= 0) ? logData.logsCount : 0;

                            if ($scope.runContext.isInfiniteScrollingMode) {
                                if (afterId) {
                                    for (var i = logEntries.length - 1; i >= 0 ; i--) {
                                        $scope.runContext.liveLogsAccumulationStack.unshift(logEntries[i]);
                                    }
                                } else {
                                    for (var i = 0; i < logEntries.length; i++) {
                                        $scope.runContext.liveLogsAccumulationStack.push(logEntries[i]);
                                    }
                                }
                                $scope.runContext.isLoadSpinnerOn = false;
                                $scope.runContext.logsCount = logsCount;
                                return;
                            }

                            var isInHistoryMode =
                                ($scope.runContext.procRun && !$scope.runContext.procRun.isLive) || //we are looking at a finished run
                                curPage > 1;//we go back in history and thus would like to stop polling

                            if (logsCount > 0 &&
                                (!$scope.runContext.logsCount || !isInHistoryMode)) {
                                $scope.runContext.logsCount = logsCount;
                            }

                            if (!isInHistoryMode) {
                                for (var i = logEntries.length - 1; i >= 0 ; i--) {
                                    $scope.runContext.liveLogsAccumulationStack.unshift(logEntries[i]);
                                }
                                var endInd = LOGS_CHUNK > $scope.runContext.liveLogsAccumulationStack.length ? $scope.runContext.liveLogsAccumulationStack.length : LOGS_CHUNK;
                                $scope.runContext.fetchedLogEntriesWindow = $scope.runContext.liveLogsAccumulationStack.slice(0, endInd);
                            } else {
                                var pageArray = $scope.runContext.fetchedLogEntries[curPage];
                                if (pageArray && pageArray.length > 0) {
                                    var pageSize = pageArray.length + logEntries.length;
                                    if (pageSize !== LOGS_CHUNK) {
                                        console.error('Unexpected page size ' + pageSize + '; pageArray.length=' + pageArray.length +
                                            '; logEntries.length=' + logEntries.length);
                                        console.error('pageSize: ' + angular.toJson(pageSize, true));
                                        console.error('logEntries: ' + angular.toJson(logEntries, true));
                                    } else if (pageArray[pageArray.length - 1].id !== logEntries[0].id - 1) {
                                        console.error('Unexpected page block received; pageArray.length=' + pageArray.length +
                                            '; logEntries.length=' + logEntries.length);
                                        console.error('pageSize: ' + angular.toJson(pageSize, true));
                                        console.error('logEntries: ' + angular.toJson(logEntries, true));
                                    }
                                    pageArray = pageArray.concat(logEntries);

                                    if (pageArray.length > LOGS_CHUNK) {
                                        //Normally this should be a bug, but make sure we don't put more elements into page than
                                        //it should have.
                                        pageArray = pageArray.slice(0, LOGS_CHUNK);
                                    }
                                } else {
                                    pageArray = logEntries;
                                    if (pageArray.length > LOGS_CHUNK) {
                                        //Normally this should be a bug, but make sure we don't put more elements into page than
                                        //it should have.
                                        pageArray = pageArray.slice(0, LOGS_CHUNK);
                                    }
                                    $scope.runContext.fetchedLogEntries[curPage] = pageArray;
                                }
                                $scope.runContext.fetchedLogEntriesWindow = pageArray;
                            }
                        }
                        //Enable back the paginator
                        $scope.isPaginationDisabled = false;
                        $scope.runContext.isLoadSpinnerOn = false;
                    },
                    function (data, status) {
                        $scope.isPaginationDisabled = false;
                        $scope.runContext.isLoadSpinnerOn = false;
                    }
                );
            };

            /**
             * Builds up log message map with pages mapped to log entries.
             * Each page is guaranteed to have exactly LOGS_CHUNK entries.
             */
            this.convertLiveLogsToHistoricalPages = function () {
                var numberOfFullPages = parseInt($scope.runContext.liveLogsAccumulationStack.length / LOGS_CHUNK);
                console.info('Converting from live to historical..');
                console.info('numberOfFullPages: ' + numberOfFullPages);
                var numOfLogEntriesToMove = numberOfFullPages * LOGS_CHUNK;
                console.info('numOfLogEntriesToMove: ' + numOfLogEntriesToMove);

                for (var i = 0; i < numOfLogEntriesToMove; i++) {
                    var logEntry = $scope.runContext.liveLogsAccumulationStack[i];
                    var pageNum = parseInt(i / LOGS_CHUNK) + 1;
                    var pageArray = $scope.runContext.fetchedLogEntries[pageNum];
                    if (!pageArray) {
                        pageArray = [];
                    }
                    pageArray.push(logEntry);
                    $scope.runContext.fetchedLogEntries[pageNum] = pageArray;
                }
            };

            this.getClosestPageNumber = function(targetPage) {
                for (var i = targetPage - 1; i > 0; i--) {
                    var page = $scope.runContext.fetchedLogEntries[i];
                    if (page && page.length > 0) {
                        return i;
                    }
                }
                return i;
            };

            this.formatLogMessage = function (logEntry) {
                if (!logEntry.messageLines) {
                    var lines = [];
                    if (logEntry.message) {
                        lines = logEntry.message.split('\n');
                    }
                    if (logEntry.exception) {
                        lines = lines.concat(logEntry.exception.split('\n'));
                    }
                    logEntry.messageLines = lines;
                }
                return logEntry.messageLines;
            };

            this.refreshLogs = function () {
                //Disable the paginator
                $scope.isPaginationDisabled = true;

                self.clearAllLogs();
                self.updateLogs($scope.dashboard.id);
            };

            this.applyFilter = function () {
                $scope.runContext.categoryFilterString = $scope.runContext.tmpCategoryFilterString;
                $scope.runContext.tagFilterString = $scope.runContext.tmpTagFilterString;
                $scope.runContext.levelFilterString = $scope.runContext.tmpLevelFilterString;
                $scope.runContext.procRun = $scope.runContext.tmpProcRun;

                //refresh log entries here
                self.refreshLogs();
            };

            /**
             * Resets temp values which are specified in the UI but does not re-apply
             * the filtering settings themselves. To cancel filtering itself applyFilter() should
             * be also called.
             */
            this.resetFilter = function () {
                $scope.runContext.tmpCategoryFilterString = null;
                $scope.runContext.tmpTagFilterString = null;
                $scope.runContext.tmpLevelFilterString = 'ALL';
                $scope.runContext.tmpProcRun = null;
            };

            this.canApplyFilter = function () {
                var runContext = $scope.runContext;
                return !runContext.fetchedLogEntries ||
                        runContext.fetchedLogEntries.length === 0 ||
                    runContext.categoryFilterString !== runContext.tmpCategoryFilterString ||
                    runContext.tagFilterString !== runContext.tmpTagFilterString ||
                    (!runContext.procRun && runContext.tmpProcRun) ||
                    (runContext.procRun && !runContext.tmpProcRun) ||
                    (runContext.procRun && runContext.tmpProcRun && runContext.procRun.id !== runContext.tmpProcRun.id);
            };

            this.canResetFilter = function () {
                var runContext = $scope.runContext;
                return (runContext.tmpCategoryFilterString && runContext.tmpCategoryFilterString !== '') ||
                    (runContext.tmpTagFilterString && runContext.tmpTagFilterString !== '') ||
                    runContext.tmpProcRun;
            };

            this.hasLogs = function () {
                return ($scope.runContext.fetchedLogEntriesWindow && $scope.runContext.fetchedLogEntriesWindow.length > 0) ||
                    ($scope.runContext.isInfiniteScrollingMode && $scope.runContext.liveLogsAccumulationStack.length > 0);
            };

            this.clearAllLogs = function () {
                $scope.runContext.fetchedLogEntries = [];
                $scope.runContext.fetchedLogEntriesWindow = [];
                $scope.runContext.liveLogsAccumulationStack = [];
                $scope.runContext.lastBeforeId = null;
                $scope.runContext.logsCount = 0;

                $scope.runContext.currentLogPageNumber = 1;
                $scope.runContext.previousLogPageNumber = 1;
                if ($scope.runContext.logsRequestJson) {
                    $scope.runContext.logsRequestJson.logsAfterId = null;
                    $scope.runContext.logsRequestJson.logsBeforeId = null;
                }

            };

            this.toggleLogPanel = function () {
                $scope.runContext.isLogPanelCollapsed = !$scope.runContext.isLogPanelCollapsed;
            };

            this.toggleLogFilterPanel = function () {
                $scope.runContext.isLogFilterPanelCollapsed = !$scope.runContext.isLogFilterPanelCollapsed;
            };

            this.toggleProcessWorkflowPanel = function () {
                $scope.runContext.isProcessWorkflowPanelCollapsed = !$scope.runContext.isProcessWorkflowPanelCollapsed;
            };

            this.calculateElapsedTime = function () {
                return $scope.dashboard && $scope.dashboard.processStarted ? new Date().getTime() - $scope.dashboard.processStarted : null;
            };

            this.getCurrentTime = function () {
                return new Date().getTime();
            };

            this.modifyExportLink = function () {
                var anchor = document.getElementById('exportDashboardLink');
                var url = anchor.href;
                var ind = url.indexOf('?x=');
                url = (ind !== -1 ? url.substring(0, ind) : url) + '?=' + self.getCurrentTime();
                anchor.href = url;
            };

            this.calculateElapsedHours = function (timeMillis) {
                return parseInt(timeMillis / MILLIS_IN_HOUR);
            };

            this.calculateElapsedMinutes = function (timeMillis) {
                return parseInt( (timeMillis % MILLIS_IN_HOUR) / MILLIS_IN_MINUTE );
            };

            this.calculateElapsedSeconds = function (timeMillis) {
                return parseInt(( (timeMillis % MILLIS_IN_HOUR) % MILLIS_IN_MINUTE ) / 1000 );
            };

            this.getMonitorImageUrl = function (monitor) {
                if ('Unknown' === monitor.value) {
                    return 'img/yellowMonitor48x48.png';
                } else if ('Error' === monitor.value) {
                    return 'img/redMonitor48x48.png';
                } else if ('OK' === monitor.value) {
                    return 'img/greenMonitor48x48.png';
                }

                return $scope.dashboard.active ? 'img/invalidMonitor48x48.png' : 'img/blackMonitor48x48.png';
            };

            this.createMonitorPollingFunction = function () {
                var dashboardId = $scope.dashboard.id;

                $scope.everySecondTimerFunction = $interval(
                    function () {
                        if ($scope.dashboard.active) {
                            var elapsedTime = self.calculateElapsedTime();
                            if (elapsedTime) {
                                document.title = self.calculateElapsedHours(elapsedTime) + ':' +
                                self.calculateElapsedMinutes(elapsedTime) + ':' +
                                self.calculateElapsedSeconds(elapsedTime) + ' - Dashboard[' + dashboardId + ']';
                            }
                        } else if ('FLD Load Orchestrator'.localeCompare(document.title)) {
                            document.title = 'FLD Load Orchestrator';
                        }
                    },
                    1000);

                return $interval(function () {
                    var myDashboardId = dashboardId;
                    var dashboard = appEditStateService.getLocalDashboardById(dashboardId);

                    if (dashboard) {
                        if (!appEditStateService.dashboardsAreEqual(dashboard, $scope.dashboard)) {
                            self.initForDashboard(dashboard);
                            $rootScope.$broadcast('DashboardUpdate', dashboard);
                        }
                    } else {
                        appEditStateService.getDashboardById(dashboardId,
                            function (dashboard) {
                                if (!appEditStateService.dashboardsAreEqual(dashboard, $scope.dashboard)) {
                                    self.initForDashboard(dashboard);
                                    $rootScope.$broadcast('DashboardUpdate', dashboard);
                                }
                            },

                            function (data, status) {
                            }
                        );
                    }

                    appEditStateService.getWaitingUserTasks(myDashboardId,
                        function (data) {
                            var waitingUserTasks = data.waitingUserTasks;
                            if (waitingUserTasks && waitingUserTasks.length > 0) {
                                if ($scope.runContext.waitingUserTasks) {
                                    var usrTasksArray = $scope.runContext.waitingUserTasks;
                                    usrTasksArray.splice(0, usrTasksArray.length);

                                    for (var i = 0; i < waitingUserTasks.length; i++) {
                                        usrTasksArray.push(waitingUserTasks[i]);
                                    }
                                } else {
                                    $scope.runContext.waitingUserTasks = waitingUserTasks;
                                }
                            } else {
                                $scope.runContext.waitingUserTasks = null;
                            }
                        },
                        function (data, status) {
                        }
                    );

                    appEditStateService.getSubprocesses(myDashboardId,
                        function (data) {
                            var processInstances = data.processInstances;
                            if (self.hasNoProcessInformation()) {
                                $scope.runContext.procInstanceIdsMap[$scope.dashboard.processInstanceId] = { show : true,
                                    imgUrl : 'img/spinner64x64.gif', title : 'This' };
                            }

                            var resultMap = {};
                            var ownProcInstId = $scope.dashboard.processInstanceId;
                            var previousMap = $scope.runContext.procInstanceIdsMap;
                            resultMap[ownProcInstId] = previousMap[ownProcInstId];

                            for (var i = 0; i < processInstances.length; i++) {
                                var procInst = processInstances[i];
                                var proc = previousMap[procInst.id];
                                resultMap[procInst.id] = proc ? proc : { show : false, imgUrl : 'img/spinner64x64.gif', title : procInst.processDefinitionId };
                            }
                            $scope.runContext.procInstanceIdsMap = resultMap;
                        },
                        function (data, status) {
                        }
                    );

                    appEditStateService.getStuckJobs(myDashboardId,
                        function (data) {
                            var jobs = data.jobs;
                            if (jobs && jobs.length > 0) {
                                if ($scope.runContext.stuckJobs) {
                                    self.refreshStuckJobs($scope.runContext.stuckJobs, jobs);
                                } else {
                                    for (var i = 0; i < jobs.length; i++) {
                                        if (jobs[i].retries === 0) {
                                            jobs[i].retries++;
                                        }
                                    }
                                    $scope.runContext.stuckJobs = jobs;
                                }
                            } else {
                                $scope.runContext.stuckJobs = null;
                            }
                        },
                        function (data, status) {
                        }
                    );

                    if ($scope.runContext.processImageIteration === PROCESS_IMAGE_UPDATE_ITERATIONS) {
                        var procInstIdsMap = $scope.runContext.procInstanceIdsMap;
                        var procInstanceIds = Object.keys(procInstIdsMap);
                        var randomNumber = self.getRandomNumber();

                        for (var i = 0; i < procInstanceIds.length; i++) {
                            var procInstId = procInstanceIds[i];
                            var proc = procInstIdsMap[procInstId];
                            if (proc) {
                                proc.imgUrl = 'api/processImage/' + procInstId + '?i=' + randomNumber;
                            } else {
                                console.warn("Could not find process JSON for procInstId=" + procInstId);
                                console.warn("procInstIdsMap: " + angular.toJson(procInstIdsMap, true));
                                console.warn("procInstanceIds: " + angular.toJson(procInstanceIds, true));
                            }

                        }

                        $scope.runContext.processImageIteration = 0;
                    } else {
                        $scope.runContext.processImageIteration++;
                    }

                    if ($scope.runContext.isLogPollingOn && $scope.runContext.currentLogPageNumber === 1 &&
                        (!$scope.runContext.procRun || $scope.runContext.procRun.isLive) && !self.isWorkflowPaused()) {
                        $scope.isPaginationDisabled = true;
                        var afterId = $scope.runContext.liveLogsAccumulationStack.length > 0 ? $scope.runContext.liveLogsAccumulationStack[0].id : null;
                        self.updateLogs(myDashboardId, null, afterId);
                    }

                    $scope.elapsedTime = self.calculateElapsedTime();
                }, 10000);
            };

            this.refreshStuckJobs = function (oldArray, newerArray) {
                //First remove those jobs which do not exist anymore
                for (var i = 0; i < oldArray.length; i++) {
                    if (self.getJobIndexById(oldArray[i].id, newerArray) === -1) {
                        oldArray.splice(i, 1);
                    }
                }
                //Then add those new ones which we haven't known about
                for (var i = 0; i < newerArray.length; i++) {
                    if (self.getJobIndexById(newerArray[i].id, oldArray) === -1) {
                        if (newerArray[i].retries === 0) {
                            newerArray[i].retries++;
                        }
                        oldArray.push(newerArray[i]);
                    }
                }
            };

            this.getJobIndexById = function (id, jobArray) {
                for (var i = 0; i < jobArray.length; i++) {
                    if (jobArray[i].id === id) {
                        return i;
                    }
                }
                return -1;
            };

            this.hasNoProcessInformation = function () {
                var procInstIds = Object.keys($scope.runContext.procInstanceIdsMap);
                return procInstIds.length === 0;
            };

            this.openWaitingUserTasksDialogue = function () {
                var modalOptions = {
                    cancelButtonText: 'Close',
                    applyButtonText: 'Apply',
                    backButtonText: 'Back to list',
                    headerText: 'Take an action on user task'
                };

                $scope.runContext.disableUserTaskButton = true;

                userTaskModalService.show(modalOptions, $scope.dashboard.id, $scope.runContext.waitingUserTasks,
                    function (userTask) {

                    },
                    function (userTask) {

                    })
                    .then(function (result) {
                        var modifiedUserTasks = result ? result.modifiedUserTasks : [];

                        $scope.runContext.waitingUserTasks = modifiedUserTasks;
                        $scope.runContext.disableUserTaskButton = false;
                    });

            };

            this.openStuckJobsDialogue = function () {
                var modalOptions = {
                    cancelButtonText: 'Close',
                    applyButtonText: 'Re-Launch'
                };

                $scope.runContext.disableStuckJobsButton = true;

                stuckJobModalService.show(modalOptions, $scope.runContext.stuckJobs,
                    function (data) {
                    },
                    function (data, status) {
                    }
                )
                .then(function (result) {
                        var modifiedStuckJobs = result ? result.modifiedStuckJobs : [];
                        var returnCode = result ? result.returnCode : null;
                        if (returnCode && returnCode !== modalOptions.cancelButtonText) {
                            $scope.runContext.stuckJobs = modifiedStuckJobs;
                        }
                        $scope.runContext.disableStuckJobsButton = false;
                    }
                );

            };

            this.hasStuckJobs = function () {
                return self.isWorkFlowRunning() && $scope.runContext.stuckJobs && $scope.runContext.stuckJobs.length > 0;
            };

            this.hasWaitingUserTasks = function () {
                return self.isWorkFlowRunning() && $scope.runContext.waitingUserTasks && $scope.runContext.waitingUserTasks.length > 0;
            };

            this.getNextLogId = function (logEntries, isAfter) {
                //logEntries should have already been sorted by log id descendingly,
                //so just look at the first or last element depending on isAfter
                return isAfter ? logEntries[0].id : logEntries[logEntries.length - 1].id;
            };

            this.destroyIntervalFunction = function () {
                if (self.stopFunction) {
                    $interval.cancel(self.stopFunction);
                    self.stopFunction = null;
                }
                if ($scope.everySecondTimerFunction) {
                    $interval.cancel($scope.everySecondTimerFunction);
                    $scope.everySecondTimerFunction = null;
                    document.title = 'FLD Load Orchestrator';
                }
            };

            this.saveCurrentDashboard = function (callback) {
                appEditStateService.updateDashboard($scope.dashboard,
                    function (dashboard) {
                        self.initForDashboard(dashboard);
                        if (callback) {
                            callback();
                        }
                    },
                    function (data, status) {
                    }
                );
            };

            /**
             *
             * @param run
             * @param config this parameter should be used when we support launching dashboards
             *        with "one-time" configs which are sent in the body of a request and are not
             *        saved to db
             */
            this.runWorkflow = function (run, config) {
                if (run && !$scope.dashboard.active) {
                    var dashboard = null;
                    if (config) {
                        dashboard = angular.copy($scope.dashboard);
                        dashboard.config = config;
                    }
                    appEditStateService.launchProcess($scope.dashboard.id, dashboard,
                        function (dashboard) {
                            //we update the state when we get ProcessLaunched event
                            //since this coming response does not have all info up-to-date
                        },
                        function (data, status) {
                        }
                    );
                } else if (!run) {
                    appEditStateService.stopProcess($scope.dashboard.id,
                        function (data, status) {
                            //self.destroyIntervalFunction();
                            //self.initForDashboard(dashboard);
                            $scope.runContext.stuckJobs = [];
                            $scope.runContext.waitingUserTasks = [];
                        },
                        function (data, status) {
                        }
                    );
                }
            };

            this.runWithWorkflowParameters = function () {

                $scope.workflowConfig = $scope.dashboard.config;

                if ($scope.workflowConfig) {
                    var modalOptions = {
                        cancelButtonText: 'Cancel',
                        saveAndRunButtonText: 'Save & Run',
                        saveDontRunButtonText: "Save & Don't Run",
                        runDontSaveButtonText: "Run",
                        headerText: 'Specify Workflow Parameters'
                    };

                    launchConfigSettingsModalService.show(modalOptions, $scope.dashboard).then(
                        function (result) {
                            var returnCode = result ? result.returnCode : null;
                            var workflowConfiguration = result ? result.workflowConfiguration : null;
                            var hideNonReqParams = result ? result.hideNonRequiredConfigParameters : $scope.dashboard.hideNonRequiredConfigParameters;
                            if (returnCode === modalOptions.saveAndRunButtonText) {
                                $scope.dashboard.config = workflowConfiguration;
                                $scope.dashboard.hideNonRequiredConfigParameters = hideNonReqParams;
                                self.saveCurrentDashboard(function () {
                                    self.runWorkflow(true);
                                });
                            } else if (returnCode === modalOptions.saveDontRunButtonText) {
                                $scope.dashboard.config = workflowConfiguration;
                                $scope.dashboard.hideNonRequiredConfigParameters = hideNonReqParams;
                                self.saveCurrentDashboard();
                            } else if (returnCode === modalOptions.runDontSaveButtonText) {
                                self.runWorkflow(true, workflowConfiguration);
                            }
                        });
                } else {
                    //no config parameters? just run it.
                    self.runWorkflow(true);
                }

            };

            this.resumeWorkflow = function () {
                appEditStateService.resumeProcess($scope.dashboard.id,
                    function (dashboard) {
                        self.initForDashboard(dashboard);
                    },
                    function (data, status) {
                    }
                );
            };

            this.pauseWorkflow = function () {
                appEditStateService.pauseProcess($scope.dashboard.id,
                    function (dashboard) {
                        self.initForDashboard(dashboard);
                    },
                    function (data, status) {
                    }
                );
            };

            this.launchWorkflow = function (run) {
                //first we need to check we have all workflow parameters
                //provided to us
                if (run) {
                    self.runWithWorkflowParameters();
                    return;
                }

                self.runWorkflow(run);
            };

            this.getProgressBarType = function () {
                if (self.isWorkflowPaused()) {
                    return 'warning';
                }
                return 'info';
            };

            this.isWorkFlowRunning = function () {
                return $scope.dashboard && $scope.dashboard.active;
            };

            this.isWorkflowPaused = function () {
                return $scope.dashboard && $scope.dashboard.suspended;
            };

            this.isWorkflowRunnable = function () {
                return $scope.dashboard && $scope.dashboard.processKey;
            };

            this.deleteDashboard = function (id) {
                var modalOptions = {
                    cancelButtonText: 'Cancel',
                    discardAndLeaveButtonText: null,
                    saveAndLeaveButtonText: 'Yes',
                    saveAndStayButtonText: null,
                    headerText: 'Delete dashboard',
                    bodyText: 'Are you sure you want to delete this dashboard?'
                };

                modalService.show(modalOptions).then(function (result) {
                    if (result === modalOptions.saveAndLeaveButtonText) {
                        appEditStateService.deleteDashboardById(id,
                            function (data, status) {
                                appEditStateService.setAlert('success', 'Successfully deleted dashboard by id = ' + id +
                                ': server status=' + status + ', data=' + angular.toJson(data));
                                $location.path('/');
                            },
                            function (data, status) {
                                appEditStateService.setAlert('danger', 'Failed to delete dashboard by id = ' + id +
                                ': server status=' + status + ', data=' + angular.toJson(data));
                            });
                    }
                });

            };

            this.getMediumIconFileName = function (dashboard) {
                if (!dashboard) {
                    return 'questionFile48x48.png';
                }
                if (!dashboard.iconName) {
                    dashboard.iconName = 'questionFile';
                }
                return dashboard.iconName + '48x48.png';
            };

            this.getRandomNumber = function () {
                return parseInt(Math.random()*1000);
            };

            this.handleProcessStoppedEvent = function () {
                $scope.runContext.waitingUserTasks = null;
                $scope.runContext.procInstanceIdsMap = {};
                $scope.runContext.isProcessWorkflowPanelCollapsed = true;
                angular.element(document.getElementById('processWorkflowPanelToggleButton')).removeClass('active');
            };

            this.openDatePicker = function ($event) {
                $event.preventDefault();
                $event.stopPropagation();
                $scope.runContext.isDatePickerOpened = true;
            };

            this.cloneDashboard = function(dashboardId) {
                var modalOptions = {
                    label: 'Dashboard name',
                    cancelButtonText: 'Cancel',
                    okButtonText: 'OK',
                    headerText: 'Clone Dashboard',
                    placeholder: 'Dashboard Name (optional)',
                    isRequired: false
                };

                singleTextBoxModalService.show(modalOptions).then(
                    function (result) {
                        if (result !== modalOptions.cancelButtonText) {
                            appEditStateService.cloneDashboard(dashboardId, result,
                                function (dashboard) {
                                },
                                function () {
                                }
                            );
                        }
                    }
                );

            };

            this.onSelectConfigParamRow = function (id) {
                var selectorId = '#' + id;
                $(selectorId).select();
                if (document.body.createTextRange) {
                    var range = document.body.createTextRange();
                    range.moveToElementText($(selectorId).get(0));
                    range.select();
                } else if (window.getSelection) {
                    var selection = window.getSelection();
                    var range = document.createRange();
                    range.selectNodeContents($(selectorId).get(0));
                    selection.removeAllRanges();
                    selection.addRange(range);
                }
            };

            $scope.$on('ProcessLaunched', function (event, dashboard) {
                appEditStateService.getDashboardById(dashboard.id,
                    function (refreshedDashboard) {
                        $scope.runContext.processImageIteration = 0;
                        self.initForDashboard(refreshedDashboard);
                    },
                    function (data, status) {
                    }
                );
            });

            $scope.$on('ProcessStopped', function (event) {
                self.destroyIntervalFunction();
                self.handleProcessStoppedEvent();
            });

            $scope.$on('DashboardsUpdate', function (event, dashboards) {
                for (var i = 0; dashboards && i < dashboards.length; i++) {
                    var dashboard = dashboards[i];
                    if ($scope.dashboard &&
                        dashboard.id === $scope.dashboard.id &&
                        !appEditStateService.dashboardsAreEqual(dashboard, $scope.dashboard)) {
                        console.log('dashboards are not equal');
                        self.initForDashboard(dashboard);
                        break;
                    }
                }
            });

            $scope.$on('$destroy', function () {
                // Make sure that the interval is destroyed too
                self.destroyIntervalFunction();
            });

            self.init(idParam);

            self.loadHistoricalRuns(idParam);

            jQuery(document).ready( function() {
                $('#selectableConfigItems').dblclick( function() {
                    $(this).select();
                    if (document.body.createTextRange) {
                        var range = document.body.createTextRange();
                        range.moveToElementText(this);
                        range.select();
                    } else if (window.getSelection) {
                        var selection = window.getSelection();
                        var range = document.createRange();
                        range.selectNodeContents(this);
                        selection.removeAllRanges();
                        selection.addRange(range);
                    }
                });
            });
        }]);