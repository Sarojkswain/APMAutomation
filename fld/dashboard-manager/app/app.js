'use strict';

var serverRequestInProgress = false;
var requestPool = [];
var dashboardSyncRequestIsInProgress = false;

// Declare app level module which depends on views, and components
angular.module('fldDashboardApp', [
    'ngRoute',
    'ui.bootstrap',
    'ui.codemirror',
    'angularFileUpload',
    'infinite-scroll',
    'fldDashboardApp.launchConfigurationSettingsViewDirective',
    'fldDashboardApp.modalService',
    'fldDashboardApp.launchConfigurationSettingsModalService',
    'fldDashboardApp.dashboardView',
    'fldDashboardApp.dashboardEditView',
    'fldDashboardApp.homeView',
    'fldDashboardApp.notFoundView',
    'fldDashboardApp.version',
    'fldDashboardApp.dashboardListNavigationDirective',
    'fldDashboardApp.nodeListViewDirective',
    'fldDashboardApp.singleTextBoxModalService',
    'fldDashboardApp.twoTextBoxesModalService',
    'fldDashboardApp.focusDirective',
    'fldDashboardApp.pictoGalleryDirective',
    'fldDashboardApp.userTaskDirective',
    'fldDashboardApp.uploadDirective',
    'fldDashboardApp.userTaskModalService',
    'fldDashboardApp.nodeListView',
    'fldDashboardApp.uploadProcessView',
    'fldDashboardApp.uploadAgentView',
    'fldDashboardApp.importDashboardModalService',
    'fldDashboardApp.stuckJobModalService',
    'fldDashboardApp.processKeyDropDownDirective',
    'fldDashboardApp.dashboardListView',
    'fldDashboardApp.groovyConsoleView',
    'fldDashboardApp.settingsView'
]).
    config(['$routeProvider', function ($routeProvider) {
        $routeProvider.otherwise({redirectTo: '/notFound'});
    }]).
    controller('FLDApplicationController', function ($scope, appEditStateService, $interval,
                                                     $location, importDashboardModalService,
                                                     modalService, $http, $filter) {
        var self = this;

        //Disable drag and drop in the app globally, only allowed areas will have drag and drop support
        $(document).bind({
            dragenter: function(e) {
                e.stopPropagation();
                e.preventDefault();
                var dt = e.originalEvent.dataTransfer;
                dt.effectAllowed = dt.dropEffect = 'none';
            },
            dragover: function(e) {
                e.stopPropagation();
                e.preventDefault();
                var dt = e.originalEvent.dataTransfer;
                dt.effectAllowed = dt.dropEffect = 'none';
            }
        });

        this.isNavBarItemSelected = function (routePath) {
            return routePath === $location.path();
        };

        this.isConsoleVisible = function () {
            return appEditStateService.isConsoleVisible();
        };

        this.isNavBarVisible = function () {
            return appEditStateService.isNavBarVisible();
        };

        this.formatAlertMessageParagraph = function (alertType, alertMessage, alertTimestamp) {
            var htmlString = '<p class="alert-log-message">';
            var level = '';
            if (alertType === 'danger') {
                htmlString += '<span class="alert-red">';
                level = '[ERROR]';
            } else if (alertType === 'success' || alertType === 'info') {
                htmlString += '<span>';
                level = '[INFO]';
            } else if (alertType === 'warning') {
                htmlString += '<span class="alert-yellow">';
                level = '[WARN]';
            }

            htmlString += $filter("date")(alertTimestamp, "[dd-MM-yyyy] [HH:mm:ss]") +
                          ' ' + level + ' : <samp>' + alertMessage + '</samp></span></p>';
            return htmlString;
        };

        this.renderAlertMessages = function () {
            if (appEditStateService.isConsoleVisible()) {
                var allAlerts = appEditStateService.getAlerts();

                var element = document.getElementById('alert-console-container');
                var jqElement = angular.element(element);
                var htmlString = '';

                for (var i = 0; allAlerts && i < allAlerts.length; i++) {
                    var alert = allAlerts[i];
                    htmlString += '<div>';
                    if (self.isHtml(alert.msg)) {
                        htmlString += self.formatAlertMessageParagraph(alert.type,
                            'Got following HTML response from the server:', alert.timestamp);
                        htmlString += '</div><div>';

                        var origServerHtml = alert.msg;
                        var startInd = origServerHtml.indexOf("<body>") + 6;
                        var endInd = origServerHtml.indexOf("</body>");
                        if (startInd > -1) {
                            origServerHtml = origServerHtml.substring(startInd, endInd);
                        }

                        var eolFormattedHtml = '';
                        while ((startInd = origServerHtml.indexOf("<pre>")) !== -1) {
                            eolFormattedHtml += origServerHtml.substring(0, startInd + 5);
                            origServerHtml = origServerHtml.substring(startInd + 5);
                            endInd = origServerHtml.indexOf("</pre>");
                            var preElementHtml = origServerHtml.substring(0, endInd);
                            preElementHtml = preElementHtml.replace(/\\n/g, '<br/>');
                            preElementHtml = preElementHtml.replace(/\\t/g, '&nbsp;&nbsp;&nbsp;&nbsp;');

                            eolFormattedHtml += preElementHtml + '</pre>';
                            origServerHtml = origServerHtml.substring(endInd + 6);
                        }
                        if (origServerHtml.length > 0) {
                            eolFormattedHtml += origServerHtml;
                        }

                        htmlString += eolFormattedHtml;
                    } else {
                        htmlString += self.formatAlertMessageParagraph(alert.type, alert.msg, alert.timestamp);
                    }
                    htmlString += '</div>';
                }

                jqElement.html(htmlString);
            }
        };

        this.toggleConsoleVisibility = function () {
            appEditStateService.toggleConsoleVisibility();

            if (appEditStateService.isConsoleVisible()) {
                self.renderAlertMessages();
            }
        };

        this.cleanConsole = function () {
            appEditStateService.removeAllAlerts();
            self.renderAlertMessages();
        };

        this.toggleNavBarVisibility = function () {
            appEditStateService.toggleNavBarVisibility();
        };

        this.isTopMenuBarVisible = function () {
            return appEditStateService.isTopMenuBarVisible();
        };

        this.toggleTopMenuBarVisibility = function () {
            appEditStateService.toggleTopMenuBarVisibility();
        };

        this.showImportDashboardDialog = function () {
            var modalOptions = {
                cancelButtonText: 'Close',
                headerText: 'Import Dashboard'
            };

            importDashboardModalService.show(modalOptions).then(
                function (result) {
                }
            );
        };

        this.showDeleteAllDashboardsDialogue = function () {
            var modalOptions = {
                cancelButtonText: 'Cancel',
                discardAndLeaveButtonText: null,
                saveAndLeaveButtonText: 'Delete All',
                saveAndStayButtonText: null,
                headerText: 'Delete all dashboards',
                bodyText: 'Do you really want to delete all dashboards?'
            };

            modalService.show(modalOptions).then(function (result) {
                if (result === modalOptions.saveAndLeaveButtonText) {
                    appEditStateService.deleteAllDashboards(
                        function (data, status) {
                            appEditStateService.setAlert('success',
                                'Successfully deleted all dashboards!');
                        },
                        function (data, status) {
                        }
                    );
                }
            });

        };

        this.isInEditMode = function () {
            return appEditStateService.isInEditMode();
        };

        this.isInDashboardView = function () {
            return appEditStateService.isInDashboardView();
        };

        this.getAlerts = function () {
            return appEditStateService.getAlerts();
        };

        this.isHtml = function (someString) {
            return someString && someString.indexOf("<html>") > -1;
        };

        this.getAlertMessage = function () {
            return self.getAlert() ? self.getAlert().msg : '';
        };

        this.getAlertType = function () {
            return self.getAlert() ? self.getAlert().type : '';
        };

        this.closeAlert = function () {
            appEditStateService.setAlert(null);
        };

        this.hasOnlyUnreadInfoConsoleMessages = function () {
            return appEditStateService.hasOnlyUnreadInfoConsoleMessages();
        };

        this.hasUnreadErrorConsoleMessages = function () {
            return appEditStateService.hasUnreadErrorConsoleMessages();
        };

        this.hasUnreadWarningConsoleMessages = function () {
            return appEditStateService.hasUnreadWarningConsoleMessages();
        };

        this.hasUnreadConsoleMessages = function () {
            return appEditStateService.hasUnreadConsoleMessages();
        };

        this.getNumOfUnreadConsoleMessages = function () {
            return appEditStateService.getNumOfUnreadConsoleMessages();
        };

        this.startDashboardsPolling = function() {
            return $interval(
                function () {
                    appEditStateService.syncDashboards();

                },
                10000);
        };

        this.stopDashboardsPolling = function () {
            if (self.stopPollFunction) {
                $interval.cancel(self.stopPollFunction);
                self.stopPollFunction = null;
            }
        };

        this.startDefferredRequestDispatcher = function () {
            return $interval(
                function () {
                    console.log("Defferred request pool size: " + requestPool.length);
                    if (!serverRequestInProgress && requestPool.length > 0) {
                        serverRequestInProgress = true;
                        var request = requestPool.shift();

                        var reqAlertBase = request.method + '  ' + request.url + ':';

                        appEditStateService.setAlert('info',
                            '<span class="glyphicon glyphicon-send" title="Queued TX"></span> ' +
                            reqAlertBase +
                            ' DATA=' + angular.toJson({ data: request.data, headers: request.headers })
                        );

                        $http(
                            {
                                method: request.method,
                                url: request.url,
                                data: request.data,
                                headers: request.headers
                            }
                        ).success(
                            function (data, status) {
                                serverRequestInProgress = false;
                                if (data && !data.error && status === 200 && (!data.status || parseInt(data.status) === 200)) {
                                    request.onSuccess(data, status);
                                } else {
                                    request.onError(data, status);
                                }
                            }
                        ).error(
                            function (data, status) {
                                serverRequestInProgress = false;
                                request.onError(data, status);
                            }
                        );
                    }
                },
                1000
            );
        };

        this.stopDefferredRequestDispatcher = function () {
            if (self.stopDefferredRequestDispatcherFunction) {
                $interval.cancel(self.stopDefferredRequestDispatcherFunction);
                self.stopDefferredRequestDispatcherFunction = null;
            }
        };

        this.toggleConsoleAutoRefresh = function () {
            self.autoRefreshAlerts = !self.autoRefreshAlerts;
        };

        this.isConsoleAutoRefreshOn = function () {
            return self.autoRefreshAlerts;
        };

        self.stopPollFunction = self.startDashboardsPolling();
        self.stopDefferredRequestDispatcherFunction = self.startDefferredRequestDispatcher();
        self.autoRefreshAlerts = true;

        $scope.$on('$destroy', function () {
            self.stopDashboardsPolling();
            self.stopDefferredRequestDispatcher();
        });

        $scope.$watch(
            function () {
                return appEditStateService.getAlerts().length;
            },
            function (newAlerts, oldAlerts) {
                if (self.autoRefreshAlerts) {
                    self.renderAlertMessages();
                }
            },
            true
        );

    }).
    service('appEditStateService', function ($http, $rootScope, $location) {
        var self = this;
        this.alert = null;
        self.isNavBarVisibleFlag = true;
        self.isTopMenuBarVisibleFlag = true;
        self.isConsoleVisibleFlag = false;

        self.alerts = [];
        self.unreadAlerts = [];
        self.unreadErrorAlertsCount = 0;
        self.unreadInfoAlertsCount = 0;
        self.unreadWarnAlertsCount = 0;

        //collection of dashboards to be compared periodically with remote dashboards on the server side
        this.localDashboards = null;

        this.isConsoleVisible = function () {
            return self.isConsoleVisibleFlag;
        };

        this.removeAllAlerts = function () {
            self.alerts = [];
        };

        this.toggleConsoleVisibility = function () {
            self.isConsoleVisibleFlag = !self.isConsoleVisibleFlag;
            if (self.isConsoleVisibleFlag) {
                if (self.unreadAlerts.length > 0) {
                    for (var i = 0; i < self.unreadAlerts.length; i++ ) {
                        var alertObj = self.unreadAlerts[i];
                        self.alerts.push(alertObj);
                    }
                    self.unreadAlerts = [];
                }
                self.unreadErrorAlertsCount = 0;
                self.unreadInfoAlertsCount = 0;
                self.unreadWarnAlertsCount = 0;
            }
        };

        this.isNavBarVisible = function () {
            return self.isNavBarVisibleFlag;
        };

        this.toggleNavBarVisibility = function () {
            self.isNavBarVisibleFlag = !self.isNavBarVisibleFlag;
        };

        this.isTopMenuBarVisible = function () {
            return self.isTopMenuBarVisibleFlag;
        };

        this.toggleTopMenuBarVisibility = function () {
            self.isTopMenuBarVisibleFlag = !self.isTopMenuBarVisibleFlag;
        };

        this.broadcastEvent = function (eventName, arg) {
            if (eventName && arg) {
                $rootScope.$broadcast(eventName, arg);
            } else if (eventName) {
                $rootScope.$broadcast(eventName);
            }
        };

        this.getLocalDashboards = function () {
            return self.localDashboards;
        };

        this.setLocalDashboards = function (dashboards) {
            self.localDashboards = dashboards;
        };

        this.syncDashboards = function () {
            self.getDashboards(
                function (dashboards) {
                    if (self.compareDashboards(self.getLocalDashboards(), dashboards)) {
                        self.broadcastEvent('DashboardsUpdate', dashboards);
                        self.setLocalDashboards(dashboards);
                    }
                },
                function (data, status) {
                    self.setAlert('danger', angular.toJson(data));
                }
            );

        };

        this.getApplicationPathForAbsUrl = function (fullUrl) {
            var baseLen = $location.absUrl().length - $location.url().length;
            return fullUrl.substring(baseLen);
        };

        this.isSuccessData = function (data) {
            if (data && data.status && parseInt(data.status) !== 200) {
                return false;
            }
            return true;
        };

        this.isInEditMode = function () {
            var currentRoutePath = $location.path();
            return currentRoutePath.indexOf('dashboardEditView') !== -1;
        };

        this.isInDashboardView = function() {
            var currentRoutePath = $location.path();
            return currentRoutePath.indexOf('dashboardView') !== -1 || self.isInEditMode();
        };

        this.getAlerts = function () {
            return self.alerts;
        };

        this.setAlert = function (alertType, data, status) {
            var alertObj = { type : alertType,
                             timestamp : new Date(),
                             status : status,
                             msg: ((typeof data) === 'string' ? data : angular.toJson(data))
            };
            if (self.isConsoleVisibleFlag) {
                self.alerts.unshift(alertObj);
            } else {
                if (alertType === 'success' || alertType === 'info') {
                    self.unreadInfoAlertsCount++;
                    self.unreadAlerts.unshift(alertObj);
                } else if (alertType === 'danger') {
                    self.unreadErrorAlertsCount++;
                    self.unreadAlerts.unshift(alertObj);
                } else if (alertType === 'warning') {
                    self.unreadWarnAlertsCount++;
                    self.unreadAlerts.unshift(alertObj);
                } else {
                    console.warn("Unknown alert type: '" + alertType + "', message: '" + alertMessage + "'");
                }
            }
        };

        this.hasOnlyUnreadInfoConsoleMessages = function () {
            return self.unreadInfoAlertsCount > 0 &&
                self.unreadErrorAlertsCount === 0 &&
                self.unreadWarnAlertsCount === 0;
        };

        this.hasUnreadErrorConsoleMessages = function () {
            return self.unreadErrorAlertsCount > 0;
        };

        this.hasUnreadWarningConsoleMessages = function () {
            return self.unreadWarnAlertsCount > 0;
        };

        this.hasUnreadConsoleMessages = function () {
            return self.unreadAlerts.length > 0;
        };

        this.getNumOfUnreadConsoleMessages = function () {
            return self.unreadAlerts.length;
        };

        this.requestServer = function (reqUrl, reqMethod, reqData,
                                       reqHeaders, successCallback, errorCallback) {

            var reqAlertBase = reqMethod + '  ' + reqUrl + ':';

            if (serverRequestInProgress) {

                var request = {
                    url: reqUrl,
                    method: reqMethod,
                    data: reqData,
                    headers: reqHeaders,

                    onSuccess: function (data, status) {
                        var reqStatusAlert = '<span class="glyphicon glyphicon-envelope" title="RX"></span> ' +
                                             reqAlertBase + ' STATUS=' + status + ' DATA=' +  angular.toJson(data);
                        if (self.isSuccessData(data)) {
                            self.setAlert('success', reqStatusAlert, status);
                            if (successCallback) {
                                successCallback(data, status);
                            }
                        } else {
                            self.setAlert('danger', reqStatusAlert, status);
                            if (errorCallback) {
                                errorCallback(data, status);
                            }
                        }
                    },

                    onError: function(data, status) {
                        serverRequestInProgress = false;
                        var reqStatusAlert = '<span class="glyphicon glyphicon-envelope" title="RX"></span> ' +
                                             reqAlertBase + ' STATUS=' + status + ' DATA=' +  angular.toJson(data);
                        self.setAlert('danger', reqStatusAlert, status);
                        if (errorCallback) {
                            errorCallback(data, status);
                        }
                    }

                };

                self.setAlert('info',
                    'Another request is executing. Pushing request to a wait queue: ' +
                    angular.toJson(request));

                requestPool.push(request);
                return;
            }

            serverRequestInProgress = true;

            self.setAlert('info',
                '<span class="glyphicon glyphicon-send" title="TX"></span> ' + reqAlertBase + ' DATA=' +
                angular.toJson({ data: reqData, headers: reqHeaders }
                )
            );

            $http(
                {
                    url: reqUrl,
                    method: reqMethod,
                    data: reqData,
                    headers: reqHeaders
                }
            ).success(function (data, status) {
                    serverRequestInProgress = false;
                    var reqStatusAlert = '<span class="glyphicon glyphicon-envelope" title="RX"></span> ' +
                                         reqAlertBase + ' STATUS=' + status + ' DATA=' +  angular.toJson(data);
                    if (self.isSuccessData(data)) {
                        self.setAlert('success', reqStatusAlert, status);
                        if (successCallback) {
                            successCallback(data, status);
                        }
                    } else {
                        self.setAlert('danger', data, status);
                        if (errorCallback) {
                            errorCallback(data, status);
                        }
                    }
                })
                .error(function (data, status) {
                    serverRequestInProgress = false;

                    var reqStatusAlert = '<span class="glyphicon glyphicon-envelope" title="RX"></span> ' +
                        reqAlertBase + ' STATUS=' + status + ' DATA=' +  angular.toJson(data);

                    self.setAlert('danger', reqStatusAlert, status);
                    if (errorCallback) {
                        errorCallback(data, status);
                    }
                }
            );
        };

        this.deleteAllLogMonitorRecipients = function (successCallback, errorCallback) {
            var reqUrl = 'api/logmonitor/deleteAllRecipients';
            var reqMethod = 'POST';
            var reqData = null;
            var reqHeaders = $http.defaults.headers.post;
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders, successCallback, errorCallback);
        };

        this.deleteLogMonitorRecipientById = function (recipientId, successCallback, errorCallback) {
            var reqUrl = 'api/logmonitor/deleteRecipientById/' + recipientId;
            var reqMethod = 'POST';
            var reqData = null;
            var reqHeaders = $http.defaults.headers.post;
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders, successCallback, errorCallback);
        };

        this.addLogMonitorRecipient = function (recipient, successCallback, errorCallback) {
            var reqUrl = 'api/logmonitor/addRecipient';
            var reqMethod = 'POST';
            var reqData = recipient;
            var reqHeaders = $http.defaults.headers.post;
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders, successCallback, errorCallback);
        };

        this.updateLogMonitorRecipient = function (recipient, successCallback, errorCallback) {
            var reqUrl = 'api/logmonitor/updateRecipient';
            var reqMethod = 'POST';
            var reqData = recipient;
            var reqHeaders = $http.defaults.headers.post;
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders, successCallback, errorCallback);
        };

        this.getLogMonitorRecipients = function (successCallback, errorCallback) {
            var reqUrl = 'api/logmonitor/recipients';
            var reqMethod = 'GET';
            var reqData = null;
            var reqHeaders = $http.defaults.headers.common;
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders, successCallback, errorCallback);
        };

        this.runGroovyScript = function (scriptContent, successCallback, errorCallback) {
            var reqUrl = 'api/runGroovyScript';
            var reqMethod = 'POST';
            var reqData = $.param( { script : scriptContent } );
            var reqHeaders = { 'Content-Type' : 'application/x-www-form-urlencoded' };
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders, successCallback, errorCallback);
        };

        this.getAllResources = function (successCallback, errorCallback) {
            var reqUrl = 'api/listResources';
            var reqMethod = 'POST';
            var reqData = null;
            var reqHeaders = $http.defaults.headers.post;
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders, successCallback, errorCallback);
        };

        this.deleteAllResources = function (successCallback, errorCallback) {
            var reqUrl = 'api/deleteResources';
            var reqMethod = 'POST';
            var reqData = null;
            var reqHeaders = $http.defaults.headers.post;
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders, successCallback, errorCallback);
        };

        this.deleteResourceById = function (id, successCallback, errorCallback) {
            var reqUrl = 'api/deleteResourceById?resourceId=' + id;
            var reqMethod = 'POST';
            var reqData = null;
            var reqHeaders = $http.defaults.headers.post;
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders, successCallback, errorCallback);
        };

        this.getProcessDefinitions = function (successCallback, errorCallback) {
            var reqUrl = 'api/listProcessDefinitions';
            var reqMethod = 'POST';
            var reqData = null;
            var reqHeaders = $http.defaults.headers.post;
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders, successCallback, errorCallback);
        };

        this.getNodeList = function (successCallback, errorCallback) {
            var reqUrl = 'api/nodeList';
            var reqMethod = 'GET';
            var reqData = null;
            var reqHeaders = $http.defaults.headers.common;
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders, successCallback, errorCallback);
        };

        this.getProcessLogs = function (dashboardId, parametersJson, successCallback, errorCallback) {
            var reqUrl = 'api/dashboards/' + dashboardId + '/logs';
            var reqMethod = 'POST';
            var reqData = parametersJson;
            var reqHeaders = $http.defaults.headers.post;
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders, successCallback, errorCallback);
        };

        this.getMonitorValues = function (dashboardId, successCallback, errorCallback) {
            var reqUrl = 'api/dashboards/' + dashboardId + '/monitors';
            var reqMethod = 'GET';
            var reqData = null;
            var reqHeaders = $http.defaults.headers.common;
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders, successCallback, errorCallback);
        };

        this.pauseProcess = function (dashboardId, successCallback, errorCallback) {
            var reqUrl = 'api/dashboards/' + dashboardId + '/pause';
            var reqMethod = 'POST';
            var reqData = null;
            var reqHeaders = $http.defaults.headers.post;

            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders,
                function (data, status) {
                    var dashboard = self.normalizeDashboard(data.dashboard);
                    self.broadcastEvent('ProcessPaused', dashboard);
                    successCallback(dashboard);
                },
                errorCallback
            );
        };

        this.resumeProcess = function (dashboardId, successCallback, errorCallback) {
            var reqUrl = 'api/dashboards/' + dashboardId + '/resume';
            var reqMethod = 'POST';
            var reqData = null;
            var reqHeaders = $http.defaults.headers.post;
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders,
                function (data, status) {
                    var dashboard = self.normalizeDashboard(data.dashboard);
                    self.broadcastEvent('ProcessResumed', dashboard);
                    successCallback(dashboard);
                },
                errorCallback
            );
        };

        this.launchProcess = function (dashboardId, dashboard, successCallback, errorCallback) {
            var reqUrl = 'api/dashboards/' + dashboardId + '/launch';
            var reqMethod = 'POST';
            var reqData = dashboard;
            var reqHeaders = $http.defaults.headers.post;
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders,
                function (data, status) {
                    var dashboard = self.normalizeDashboard(data.dashboard);
                    self.broadcastEvent('ProcessLaunched', dashboard);
                    successCallback(dashboard);
                },
                errorCallback
            );
        };

        this.stopProcess = function (dashboardId, successCallback, errorCallback) {
            var reqUrl = 'api/dashboards/' + dashboardId + '/cancel';
            var reqMethod = 'POST';
            var reqData = null;
            var reqHeaders = $http.defaults.headers.post;
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders,
                function (data, status) {
                    self.broadcastEvent('ProcessStopped');
                    successCallback(data, status);
                },
                errorCallback
            );
        };

        this.reLaunchJobs = function (jobs, successCallback, errorCallback) {
            var reqUrl = 'api/dashboards/reLaunchJobs';
            var reqMethod = 'POST';
            var reqData = jobs;
            var reqHeaders = $http.defaults.headers.post;
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders, successCallback, errorCallback);
        };

        this.getStuckJobs = function (dashboardId, successCallback, errorCallback) {
            var reqUrl = 'api/dashboards/' + dashboardId + '/stuckJobs';
            var reqMethod = 'GET';
            var reqData = null;
            var reqHeaders = $http.defaults.headers.common;
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders, successCallback, errorCallback);
        };

        this.getWaitingUserTasks = function (dashboardId, successCallback, errorCallback) {
            var reqUrl = 'api/dashboards/' + dashboardId + '/userTasks';
            var reqMethod = 'GET';
            var reqData = null;
            var reqHeaders = $http.defaults.headers.common;
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders, successCallback, errorCallback);
        };

        this.completeUserTasks = function (dashboardId, userTasks, successCallback, errorCallback) {
            var reqUrl = 'api/dashboards/' + dashboardId + '/completeTask';
            var reqMethod = 'POST';
            var reqData = userTasks;
            var reqHeaders = $http.defaults.headers.post;
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders, successCallback, errorCallback);
        };

        this.deleteProcessDefinition = function (deploymentId, cascade, successCallback, errorCallback) {
            var reqUrl = 'api/deleteProcessDefinition/' + deploymentId;
            var reqMethod = 'DELETE';
            var reqData = null;
            var reqHeaders = $http.defaults.headers.common;
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders, successCallback, errorCallback);
        };

        this.deleteAllProcessDefinitions = function (cascade, successCallback, errorCallback) {
            var reqUrl = 'api/deleteAllProcessDefinitions';
            var reqMethod = 'DELETE';
            var reqData = null;
            var reqHeaders = $http.defaults.headers.common;
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders, successCallback, errorCallback);
        };

        this.deleteAllDashboards = function (successCallback, errorCallback) {
            var reqUrl = 'api/deleteAllDashboards';
            var reqMethod = 'DELETE';
            var reqData = null;
            var reqHeaders = $http.defaults.headers.common;
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders,
                function (data, status) {
                    self.localDashboards = null;
                    self.broadcastEvent('DashboardsUpdate', null);
                    successCallback(data);
                },
                errorCallback
            );
        };

        this.hasAgentDistribution = function (successCallback, errorCallback) {
            var reqUrl = 'api/hasAgentDistro';
            var reqMethod = 'GET';
            var reqData = null;
            var reqHeaders = $http.defaults.headers.common;
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders, successCallback, errorCallback);
        };

        this.downloadAgentDistribution = function (agentConfig, callback) {
            console.log(angular.toJson(agentConfig, true));
            var request = new XMLHttpRequest();
            request.open("GET", "api/preconfiguredAgent", false);
            request.send();
        };

        this.deleteNodeById = function (id, successCallback, errorCallback) {
            var reqUrl = 'api/deleteNode/' + id;
            var reqMethod = 'DELETE';
            var reqData = null;
            var reqHeaders = $http.defaults.headers.common;
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders, successCallback, errorCallback);
        };

        this.deleteDashboardById = function (id, successCallback, errorCallback) {
            var reqUrl = 'api/dashboards/' + id;
            var reqMethod = 'DELETE';
            var reqData = null;
            var reqHeaders = $http.defaults.headers.common;
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders,
                function (data, status) {
                    //remove the dashboard from collection of local ones
                    for (var i = 0; self.localDashboards && i < self.localDashboards.length; i++) {
                        if (id === self.localDashboards[i].id) {
                            self.localDashboards.splice(i, 1);
                            break;
                        }
                    }
                    self.broadcastEvent('DeleteDashboardFinished');
                    successCallback(data, status);
                },
                errorCallback
            );
        };

        this.updateDashboard = function (dashboard, successCallback, errorCallback) {
            var reqUrl = 'api/dashboards/' + dashboard.id;
            var reqMethod = 'PUT';
            var reqData = dashboard;
            var reqHeaders = $http.defaults.headers.put;
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders,
                function (data, status) {
                    var dashboard = self.normalizeDashboard(data.dashboard);
                    self.updateLocalDashboard(dashboard);
                    self.broadcastEvent('UpdateDashboardFinished', dashboard);
                    successCallback(dashboard);
                },
                errorCallback
            );
        };

        this.addNewDashboard = function (dashboard, successCallback, errorCallback) {
            var reqUrl = 'api/dashboards';
            var reqMethod = 'POST';
            var reqData = dashboard;
            var reqHeaders = $http.defaults.headers.post;
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders,
                function (data, status) {
                    self.broadcastEvent('AddNewDashboardFinished');
                    successCallback(self.normalizeDashboard(data.dashboard));
                },
                errorCallback
            );
        };

        this.cloneDashboard = function (dashboardId, newDashboardName, successCallback, errorCallback) {
            var reqUrl = 'api/dashboards/' + dashboardId + '/clone';
            var reqMethod = 'POST';
            var reqData = $.param( { name : newDashboardName } );
            var reqHeaders = { 'Content-Type' : 'application/x-www-form-urlencoded' };
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders,
                function (data, status) {
                    self.broadcastEvent('CloneDashboardFinished');
                    successCallback(self.normalizeDashboard(data.dashboard));
                },
                errorCallback
            );
        };

        this.fetchProcessKeyLocalization = function (keys) {
            var reqUrl = 'api/listProperties2';
            var reqMethod = 'POST';
            var reqData = keys && keys.length > 0 ?  { propertyNames : keys } : null;
            var reqHeaders = $http.defaults.headers.post;
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders,
                function (data, status) {
                    if (!self.propertyList) {
                        self.propertyList = {};
                    }
                    for (var i = 0; data.propertyList && i < data.propertyList.length; i++) {
                        var propertyValue = data.propertyList[i];
                        self.propertyList[propertyValue.name] = propertyValue.value;
                    }
                }
            );
        };

        this.getDashboards = function (successCallback, errorCallback) {
            if (dashboardSyncRequestIsInProgress) {
                return;
            }

            dashboardSyncRequestIsInProgress = true;

            var reqUrl = 'api/dashboards';
            var reqAlertBase = reqUrl + ' GET DATA=';

            $http.get(reqUrl)
                .success(function (data, status) {
                    dashboardSyncRequestIsInProgress = false;
                    var alertMsg = reqAlertBase + angular.toJson(data);
                    if (self.isSuccessData(data)) {
                        if (successCallback) {
                            successCallback(self.normalizeDashboards(data.dashboards));
                        }
                    } else {
                        self.setAlert('danger', alertMsg, status);
                        if (errorCallback) {
                            errorCallback(data, status);
                        }

                    }
                })
                .error(function (data, status) {
                    dashboardSyncRequestIsInProgress = false;
                    var alertMsg = reqAlertBase + angular.toJson(data);
                    self.setAlert('danger', alertMsg, status);
                    if (errorCallback) {
                        errorCallback(data, status);
                    }
                }
            );
        };

        this.getDashboardById = function (id, successCallback, errorCallback) {
            var reqUrl = 'api/dashboards/' + id;
            var reqMethod = 'GET';
            var reqData = null;
            var reqHeaders = $http.defaults.headers.common;
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders,
                function (data, status) {
                    successCallback(self.normalizeDashboard(data.dashboard));
                },
                errorCallback
            );
        };

        this.getSubprocesses = function (dashboardId, successCallback, errorCallback) {
            var reqUrl = 'api/dashboards/subprocesses/' + dashboardId;
            var reqMethod = 'GET';
            var reqData = null;
            var reqHeaders = $http.defaults.headers.common;
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders, successCallback, errorCallback);
        };

        this.getHistoricalProcessInstances = function (dashboardId, successCallback, errorCallback) {
            var reqUrl = 'api/dashboardRunHistory/' + dashboardId;
            var reqMethod = 'GET';
            var reqData = null;
            var reqHeaders = $http.defaults.headers.common;
            self.requestServer(reqUrl, reqMethod, reqData, reqHeaders, successCallback, errorCallback);
        };

        this.getNodeAgentPlugins = function (nodeName, successCallback, errorCallback) {
            var reqUrl = 'api/nodePluginList?nodeName=' + nodeName;
            var reqHeaders = $http.defaults.headers.common;

            self.requestServer(reqUrl, 'GET', null, reqHeaders, successCallback, errorCallback);
        };

        this.getLocalDashboardById = function (id) {
            for (var i = 0; self.localDashboards && i < self.localDashboards.length; i++) {
                var dshbrd = self.localDashboards[i];
                if (dshbrd.id === id) {
                    return dshbrd;
                }
            }
            return null;
        };

        this.updateLocalDashboard = function (dashboard) {
            for (var i = 0; self.localDashboards && i < self.localDashboards.length; i++) {
                var dshbrd = self.localDashboards[i];
                if (dshbrd.id === dashboard.id) {
                    self.localDashboards[i] = dashboard;
                    return;
                }
            }
            if (!self.localDashboards) {
                self.localDashboards = [];
            }
            self.localDashboards.push(dashboard);
        };

        this.getKeyLocalization = function (key) {
            if (self.propertyList) {
                var localization = self.propertyList[key];
                if (!localization) {
                    localization = key;
                }
                return localization;
            }
            return key;
        };

        this.normalizeDashboards = function (dashboards) {
            if (dashboards) {
                for (var i = 0; i < dashboards.length; i++) {
                    dashboards[i] = self.normalizeDashboard(dashboards[i]);
                }
            }
            return dashboards;
        };

        this.normalizeDashboard = function (dashboard) {
            if (dashboard) {
                if (!dashboard.iconName) {
                    dashboard.iconName = "questionFile";
                }
            }
            return dashboard;
        };

        this.findMonitorIndex = function (monitors, key) {
            if (!monitors || monitors.length === 0) {
                return -1;
            }

            for (var i = 0; i < monitors.length; i++) {
                var monitor = monitors[i];
                if (monitor.key === key) {
                    return i;
                }
            }

            return -1;
        };

        /**
         * Fills into monitor objects some additional info.
         *
         * @param monitors
         */
        this.normalizeDashboardMonitors = function (monitors) {
            if (!monitors) {
                monitors = [];
            }
            if (monitors.length === 0) {
                //nothing to normalize, just return the empty array
                return monitors;
            }

            for (var i = 0; i < monitors.length; i++) {
                var monitor = monitors[i];
                if (!monitor.img) {
                    monitor.img = "img/blackMonitor48x48.png";
                }
            }
            return monitors;
        };

        this.denormalizeDashboardMonitors = function (monitors) {
            if (!monitors || monitors.length === 0) {
                return monitors;
            }
            var result = [];
            for (var i = 0; i < monitors.length; i++) {
                var monitor = monitors[i];
                result[i] = { key: monitor.key, name: monitor.name };
            }

            return result;
        };

        this.deleteMonitor = function (monitors, key) {
            if (!monitors || monitors.length === 0) {
                //Nothing to delete
                return monitors;
            }

            var ind = self.findMonitorIndex(monitors, key);
            if (ind < 0) {
                alert("Could not find monitor key '" + key + "'!");
                return monitors;
            }

            monitors.splice(ind, 1);
            return monitors;
        };

        this.addMonitorToDashboard = function (monitors, key, name) {
            if (!monitors) {
                monitors = [];
            }

            if (monitors.length === 0) {
                //create a normalized monitors array by default
                monitors.push({ key: key, name: name, img: "img/blackMonitor48x48.png" });
                return true;
            }

            //First check that we don't have this value yet.
            var ind = self.findMonitorIndex(monitors, key);
            if (ind >= 0) {
                var msg = "There is already a monitor with such key '" + key + "'!";
                alert(msg);
                console.warn(msg);
                return false;
            }
            monitors.push({ key: key, name: name, img: "img/blackMonitor48x48.png" });
            return true;
        };

        /**
         * Returns true only when there is some difference between the two dashboard
         * collections.
         *
         * @param dashboardsLocal
         * @param dashboardsRemote
         * @returns {boolean}
         */
        this.compareDashboards = function (dashboardsLocal, dashboardsRemote) {
            if (!dashboardsLocal || dashboardsLocal.length === 0) {
                if (dashboardsRemote && dashboardsRemote.length > 0) {
                    return true;
                }
                return false;
            }

            if (!dashboardsRemote) {
                //Just inform the current user that dashboards have disappeared remotely
                self.setAlert('warning', 'Seems that somebody else has just removed all the dashboards!');
                return false;
            }

            if (dashboardsLocal.length != dashboardsRemote.length) {
                if (dashboardsRemote.length === 0) {
                    //Just inform the current user that dashboards have disappeared remotely
                    self.setAlert('warning', 'Seems that somebody else has just removed all the dashboards!');
                    return false;
                }
                //Ok, we got a different number of dashboards from the server, let us refresh
                return true;
            }

            //Seems we still have the same amount of dashboards stored on the server side.
            //But it does not mean their configuration has not been updated by someone in meanwhile.
            //We need to check that.

            var myDashboardsMap = {};
            for (var i = 0; i < dashboardsLocal.length; i++) {
                myDashboardsMap[dashboardsLocal[i].id] = dashboardsLocal[i];
            }

            var theirDashboardsMap = {};
            for (var i = 0; i < dashboardsRemote.length; i++) {
                theirDashboardsMap[dashboardsRemote[i].id] = dashboardsRemote[i];
            }

            if (!self.compareDashboardMaps(myDashboardsMap, theirDashboardsMap) ||
                !self.compareDashboardMaps(theirDashboardsMap, myDashboardsMap)) {
                return true;
            }
            return false;
        };

        /**
         * Returns true only if a dashboards1 is a subset of a dashboard2.
         *
         * @param dashboards1    [dashboard.id => dashboard] collection
         * @param dashboards2    [dashboard.id => dashboard] collection
         */
        this.compareDashboardMaps = function (dashboards1, dashboards2) {
            var keys1 = Object.keys(dashboards1);
            for (var i = 0; i < keys1.length; i++) {
                var dashboard1 = dashboards1[keys1[i]];
                var dashboard2 = dashboards2[dashboard1.id];
                if (!dashboard2 || !self.dashboardsAreEqual(dashboard1, dashboard2)) {
                    return false;
                }
            }
            return true;
        };

        this.dashboardsAreEqual = function (dashboard1, dashboard2) {
            if (!dashboard1 && !dashboard2) {
                return true;
            } else if (!dashboard1 && dashboard2) {
                return false;
            } else if (dashboard1 && !dashboard2) {
                return false;
            }
            if (dashboard1.length !== dashboard2.length) {
                return false;
            }
            if (dashboard1.id !== dashboard2.id) {
                return false;
            }
            if (dashboard1.processKey !== dashboard2.processKey) {
                return false;
            }
            if (dashboard1.name !== dashboard2.name) {
                return false;
            }
            if (dashboard1.hideNonRequiredConfigParameters !== dashboard2.hideNonRequiredConfigParameters) {
                return false;
            }
            if (dashboard1.hasWaitingUserTasks !== dashboard2.hasWaitingUserTasks) {
                return false;
            }
            if (dashboard1.hasStuckJobs !== dashboard2.hasStuckJobs) {
                return false;
            }
            if (dashboard1.active !== dashboard2.active) {
                return false;
            }
            if (dashboard1.suspended !== dashboard2.suspended) {
                return false;
            }
            if (dashboard1.processStarted !== dashboard2.processStarted) {
                return false;
            }
            if (dashboard1.processEnded !== dashboard2.processEnded) {
                return false;
            }
            if (dashboard1.processInstanceId !== dashboard2.processInstanceId) {
                return false;
            }
            if (dashboard1.iconName !== dashboard2.iconName) {
                return false;
            }

            var monitors1 = dashboard1.monitors;
            var monitors2 = dashboard2.monitors;
            if ((!monitors1 && monitors2) || (monitors1 && !monitors2)) {
                return false;
            }
            if (monitors1) {
                for (var i = 0; i < monitors1.length; i++) {
                    var monitor1 = monitors1[i];
                    var monitor2 = monitors2[i];
                    if (monitor1 && !monitor2) {
                        return false;
                    }
                    if (monitor2 && !monitor1) {
                        return false;
                    }
                    if (monitor1.key !== monitor2.key) {
                        return false;
                    }
                    if (monitor1.name !== monitor2.name) {
                        return false;
                    }
                }
            }

            var config1 = dashboard1.config;
            var config2 = dashboard2.config;
            if ((!config1 && config2) || (config1 && !config2)) {
                return false;
            }
            if (config1) {
                for (var i = 0; i < config1.length; i++) {
                    var configItem1 = config1[i];
                    var configItem2 = config2[i];
                    if (configItem1 && !configItem2) {
                        return false;
                    }
                    if (configItem2 && !configItem1) {
                        return false;
                    }
                    if (configItem1.formId !== configItem2.formId) {
                        return false;
                    }
                    if (configItem1.type !== configItem2.type) {
                        return false;
                    }
                    if (configItem1.name !== configItem2.name) {
                        return false;
                    }
                    if (configItem1.value !== configItem2.value) {
                        return false;
                    }
                    if (configItem1.required !== configItem2.required) {
                        return false;
                    }
                }
            }
            return true;
        };
    }).service('configParamService', function () {
        var self = this;

        this.updateEnumParam = function (param, enumKey) {
            if (!param || param.type !== 'enum') {
                return;
            }
            var map = self.getEnumNameValueMap(param, true);
            param.value = map[enumKey];
        };

        this.getEnumValueLabels = function (param) {
            if (!param || !param.typeInformation || param.type !== 'enum') {
                return null;
            }
            var namesToValuesMap = self.getEnumNameValueMap(param, true);
            var result = [];
            var keys = Object.keys(namesToValuesMap);
            for (var i = 0; i < keys.length; i++) {
                result.push(keys[i]);
            }

            return result;
        };

        this.getEnumParamValueLabel = function (param) {
            if (!param) {
                return null;
            }
            if (param.type !== 'enum') {
                throw "Param " + angular.toJson(param) +
                      " is expected to be of type 'enum' but in real has type '" +
                      param.type + "'";
            }
            if (!param.value) {
                return 'Select value...';
            }
            var valueToNamesMap = self.getEnumNameValueMap(param, false);
            var label = valueToNamesMap[param.value];
            if (!label) {
                label = 'Select value...';
            }
            return label;
        };

        this.enumParamHasValue = function (param) {
            if (!param) {
                return false;
            }
            if (param.type !== 'enum') {
                throw "Param " + angular.toJson(param) +
                " is expected to be of type 'enum' but in real has type '" +
                param.type + "'";
            }
            if (!param.value) {
                return false;
            }
            var valueToNamesMap = self.getEnumNameValueMap(param, false);
            var label = valueToNamesMap[param.value];
            return label ? true : false;
        };

        /**
         * Parses strings like '{ True=Yes, False=No }' and return a JSON
         * of form { "Yes" : True, "No" : False }.
         *
         * @param   userTask         user task waiting for action
         * @param   isNameToValue    if true then maps name => value, otherwise maps value => name
         * @returns                  a JSON as a map giving all key=value pairs
         */
        this.getEnumNameValueMap = function (param, isNameToValue) {
            if (!param || param.type !== 'enum' || !param.typeInformation) {
                return null;
            }

            var result = {};
            var valueToNamePairs = param.typeInformation.substring(1, param.typeInformation.length - 1).split(',');
            for (var i = 0; i < valueToNamePairs.length; i++) {
                var valueToNamePair = valueToNamePairs[i].trim();
                var valueNameArray = valueToNamePair.split('=');
                if (valueNameArray && valueNameArray.length > 1) {
                    if (isNameToValue) {
                        result[valueNameArray[1]] = valueNameArray[0];
                    } else {
                        result[valueNameArray[0]] = valueNameArray[1];
                    }

                }
            }
            return result;
        };

    }).service('nodeService', function () {
        var self = this;

        this.findNodeById = function(id, nodeList) {
            for (var i = 0; nodeList && i < nodeList.length; i++) {
                var node = nodeList[i];
                if (id === node.id) {
                    return node;
                }
            }
            return null;
        };

        this.areNodesEqual = function (node1, node2) {
            if (!node1 && !node2) {
                return true;
            } else if (node1 && !node2) {
                return false;
            } else if (!node1 && node2) {
                return false;
            }
            if (node1.id !== node2.id) {
                return false;
            }
            if (node1.name !== node2.name) {
                return false;
            }
            if (node1.hostName !== node2.hostName) {
                return false;
            }
            if (node1.ip4 !== node2.ip4) {
                return false;
            }
            if (node1.lastHeartbeat !== node2.lastHeartbeat) {
                return false;
            }
            if (node1.lastHeartbeatRequest !== node2.lastHeartbeatRequest) {
                return false;
            }
            if (node1.version !== node2.version) {
                return false;
            }
            if (node1.isAvailable !== node2.isAvailable) {
                return false;
            }
            if (node1.isAgentUpdating !== node2.isAgentUpdating) {
                return false;
            }
            return true;
        };

        this.areNodeListsEqual = function (nodeList1, nodeList2) {
            if (!nodeList1 && !nodeList2) {
                return true;
            } else if (!nodeList2 && nodeList1) {
                return nodeList1.length === 0;
            } else if (!nodeList1 && nodeList2) {
                return nodeList2.length === 0;
            } else if (nodeList1.length !== nodeList2.length) {
                return false;
            }

            for (var i = 0; i < nodeList1.length; i++) {
                var node1 = nodeList1[i];
                var node2 = self.findNodeById(node1.id, nodeList2);
                if (!self.areNodesEqual(node1, node2)) {
                    return false;
                }
            }
            return true;
        };

    }
);
