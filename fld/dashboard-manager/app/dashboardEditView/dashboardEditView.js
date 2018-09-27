'use strict';

angular.module('fldDashboardApp.dashboardEditView', ['ngRoute'])
    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/dashboardEditView/:dashboardId', {
            templateUrl: 'dashboardEditView/dashboardEditView.html'
        });
    }])
    .controller('DashboardEditDetailsController', ['$http', '$scope', '$routeParams', '$window',
        '$location', 'appEditStateService', 'modalService', 'twoTextBoxesModalService',
        function ($http, $scope, $routeParams, $window, $location, appEditStateService, modalService, twoTextBoxesModalService) {
            var self = this;

            this.isModified = false;
            this.detachDashboardEditViewRouteChangeListener = null;


            this.init = function (dashboard) {
                $scope.dashboard = dashboard;
                $scope.config = angular.copy(dashboard.config);
                $scope.hideNonRequiredConfigParameters = angular.copy(dashboard.hideNonRequiredConfigParameters);
                $scope.monitors = appEditStateService.normalizeDashboardMonitors(dashboard.monitors);
            };

            if (!$routeParams.dashboardId || $routeParams.dashboardId === 'new') {
                //create a new one
                $scope.dashboard = {iconName: 'questionFile'};
                $scope.monitors = [];
            } else {
                var dashboardId = parseInt($routeParams.dashboardId);

                var dashboard = appEditStateService.getLocalDashboardById(dashboardId);
                if (!dashboard) {
                    appEditStateService.getDashboardById(dashboardId,
                        function (dashboard) {
                            self.init(dashboard);
                        },
                        function (data, status) {
                            $scope.dashboard = { iconName : 'questionFile' };
                            appEditStateService.setAlert('danger',
                                'Failed to get a dashboard by id=' + dashboardId + ': ' + angular.toJson(data));
                        }
                    );
                } else {
                    self.init(dashboard);
                }
            }

            this.save = function (callback) {
                //save original config and monitors values
                //var oldConfig = $scope.dashboard.config;
                //var oldMonitors = $scope.dashboard.monitors;

                //update config and monitor values in the JSON with the edited values
                $scope.dashboard.config = $scope.config;
                $scope.dashboard.hideNonRequiredConfigParameters = $scope.hideNonRequiredConfigParameters;
                $scope.dashboard.monitors = appEditStateService.denormalizeDashboardMonitors($scope.monitors);
                if (!$scope.dashboard.id) {
                    appEditStateService.addNewDashboard($scope.dashboard,
                        function (dashboard) {
                            appEditStateService.setAlert('success', 'Successfully created a new dashboard!');
                            $scope.dashboard = dashboard;
                            $scope.config = angular.copy(dashboard.config);
                            $scope.hideNonRequiredConfigParameters = angular.copy(dashboard.hideNonRequiredConfigParameters);
                            $scope.monitors = angular.copy(dashboard.monitors);
                            callback();
                        },
                        function (data, status) {
                            //something went wrong, roll back the original dashboard values
                            //$scope.dashboard.config = oldConfig;
                            //$scope.dashboard.monitors = oldMonitors;
                            appEditStateService.setAlert('danger', 'Failed to create a new dashboard: ' + angular.toJson(data));
                        });
                } else {
                    appEditStateService.updateDashboard($scope.dashboard,
                        function (dashboard) {
                            appEditStateService.setAlert('success', 'Successfully updated the dashboard (id=' + dashboard.id + ')');
                            $scope.dashboard = dashboard;
                            $scope.config = angular.copy(dashboard.config);
                            $scope.hideNonRequiredConfigParameters = angular.copy(dashboard.hideNonRequiredConfigParameters);
                            $scope.monitors = angular.copy(dashboard.monitors);
                            callback();
                        },
                        function (data, status) {
                            appEditStateService.setAlert('danger', 'Failed to update the dashboard: ' + angular.toJson(data));
                        });
                }
            };

            this.cleanUpDirtyState = function () {
                self.setHasChanges(false);
            };

            this.stopListeningRouteChanges = function () {
                if (!self.hasChanges() && self.detachDashboardEditViewRouteChangeListener) {
                    self.detachDashboardEditViewRouteChangeListener(); //Stop listening for location changes
                    self.detachDashboardEditViewRouteChangeListener = null;
                }
            };

            this.onChangeLocation = function (event, newUrl, oldUrl) {
                if (self.hasChanges()) {
                    //show a modal dialog asking to save changes
                    var modalOptions = {
                        cancelButtonText: 'Cancel',
                        discardAndLeaveButtonText: 'Discard & Leave',
                        saveAndLeaveButtonText: 'Save & Leave',
                        saveAndStayButtonText: 'Save & Stay',
                        headerText: 'Save changes?',
                        bodyText: 'You have unsaved changes, padavan. Do you want me to save them before navigating away?'
                    };

                    modalService.show(modalOptions).then(function (result) {
                        if (result === modalOptions.saveAndLeaveButtonText) {
                            self.save(function () {
                                self.cleanUpDirtyState();
                                self.stopListeningRouteChanges();
                                $location.path(appEditStateService.getApplicationPathForAbsUrl(newUrl));
                            });
                        } else if (result === modalOptions.saveAndStayButtonText) {
                            self.save(function () {
                                self.cleanUpDirtyState();
                            });
                        } else if (result === modalOptions.discardAndLeaveButtonText) {
                            self.cleanUpDirtyState();
                            self.stopListeningRouteChanges();
                            $location.path(appEditStateService.getApplicationPathForAbsUrl(newUrl));
                        }
                    });
                    event.preventDefault();
                }
            };

            this.detachDashboardEditViewRouteChangeListener = $scope.$on('$locationChangeStart', this.onChangeLocation);

            this.isNewDashboard = function () {
                return !$scope.dashboard || !$scope.dashboard.id;
            };

            this.onSave = function () {
                var isNew = self.isNewDashboard();
                self.save(function () {
                    self.cleanUpDirtyState();
                    self.stopListeningRouteChanges();
                    if (isNew) {
                        $location.path('/dashboardView/' + $scope.dashboard.id);
                    } else {
                        $window.history.back();
                    }
                });
            };

            this.hasMonitors = function () {
                return $scope.dashboard && $scope.dashboard.monitors && $scope.dashboard.monitors.length > 0;
            };

            this.onSaveAndStay = function () {
                self.save(function () {
                    self.cleanUpDirtyState();
                });
            };

            this.onCancel = function () {
                self.cleanUpDirtyState();
                self.stopListeningRouteChanges();
                $window.history.back();
            };

            this.setHasChanges = function (hasChanges) {
                self.isModified = hasChanges;
            };

            this.hasChanges = function () {
                return self.isModified;
            };

            this.openAddValueWidgetDialog = function () {
                var modalOptions = {
                    label1: 'Monitor Key',
                    placeholder1: 'Monitor Key',
                    label2: 'Monitor Name',
                    placeholder2: 'Monitor Name',
                    cancelButtonText: 'Cancel',
                    okButtonText: 'OK',
                    headerText: 'Add Monitor'
                };

                twoTextBoxesModalService.show(modalOptions).then(function (result) {
                    //Need to think of a way not to close the edit dialog if a widget with such value is already added
                    if (result !== 'cancel') {
                        if (appEditStateService.addMonitorToDashboard($scope.monitors, result.text1, result.text2)) {
                            self.setHasChanges(true);
                        }
                    }
                });

            };

            this.removeAllWidgets = function () {
                var modalOptions = {
                    cancelButtonText: 'Cancel',
                    discardAndLeaveButtonText: null,
                    saveAndLeaveButtonText: 'OK',
                    saveAndStayButtonText: null,
                    headerText: 'Remove all widgets',
                    bodyText: 'Are you sure you want to delete all of the widgets? Press OK to delete them.'
                };

                modalService.show(modalOptions).then(function (result) {
                    if (result === modalOptions.saveAndLeaveButtonText) {
                        $scope.dashboard.monitors = null;
                        self.setHasChanges(true);
                    }
                });
            };

            this.removeValueWidget = function (monitor) {
                if (monitor) {
                    var sizeBeforeDeletion = $scope.monitors ? $scope.monitors.length : 0;
                    console.log("Num of mons before removing: " + sizeBeforeDeletion);
                    $scope.monitors = appEditStateService.deleteMonitor($scope.monitors, monitor.key);
                    var sizeAfterDeletion = $scope.monitors ? $scope.monitors.length : 0;
                    console.log("Num of mons after removing: " + sizeAfterDeletion);
                    if (sizeBeforeDeletion !== sizeAfterDeletion) {
                        self.setHasChanges(true);
                    }
                }
            };

            this.editValueWidget = function (monitor) {
                var modalOptions = {
                    label1: 'Monitor Key',
                    placeholder1: 'Monitor Key',
                    label2: 'Monitor Name',
                    placeholder2: 'Monitor Name',
                    cancelButtonText: 'Cancel',
                    okButtonText: 'OK',
                    headerText: 'Add Monitor',
                    textInput1: monitor.key,
                    textInput2: monitor.name
                };

                twoTextBoxesModalService.show(modalOptions).then(function (result) {
                    if (result !== 'cancel') {
                        monitor.key = result.text1;//TODO: check the key for existence?
                        monitor.name = result.text2;
                        self.setHasChanges(true);
                    }
                });

            };

            $scope.setHasChanges = this.setHasChanges;
            $scope.hasChanges = this.hasChanges;

            $scope.updateWorkflowConfiguration = function (config, hideNonReqParams) {
                $scope.config = config;
                $scope.hideNonRequiredConfigParameters = hideNonReqParams;
            };

            $scope.onIconUpdate = function (iconName) {
                $scope.dashboard.iconName = iconName;
                self.setHasChanges(true);
            };

        }]);