'use strict';


angular.module('fldDashboardApp.dashboardListNavigationDirective', [])

    .directive('navigationSidebarDashboards', ['$http', '$location', function($http, $location) {
        return {
            restrict: 'E',
            templateUrl: 'components/dashboardListNavigationView/dashboardListNavigationView.html',
            controller: function($http, appEditStateService, $scope) {
                var self = this;
                $scope.appSettings = globalLOAppSettings;

                $scope.$on('AddNewDashboardFinished', function (event) {
                    self.loadDashboards();
                });

                $scope.$on('UpdateDashboardFinished', function (event) {
                    self.loadDashboards();
                });

                $scope.$on('DeleteDashboardFinished', function (event) {
                    self.loadDashboards();
                });

                $scope.$on('ProcessLaunched', function (event) {
                    self.loadDashboards();
                });

                $scope.$on('ProcessStopped', function (event) {
                    self.loadDashboards();
                });

                $scope.$on('CloneDashboardFinished', function (event) {
                    self.loadDashboards();
                });

                $scope.$on('DashboardsUpdate', function (event, dashboards) {
                    self.dashboards = dashboards;
                });

                $scope.$on('DashboardUpdate', function (event, dashboard) {
                    for (var i = 0; self.dashboards && i < self.dashboards.length; i++) {
                        var iDashboard = self.dashboards[i];
                        if (dashboard.id === iDashboard.id) {
                            self.dashboards[i] = dashboard;
                        }
                    }
                });

                this.loadDashboards = function () {
                    appEditStateService.getDashboards(
                        function (dashboards) {
                            self.dashboards = dashboards;
                        },
                        function (data, status) {
                        }
                    );
                };

                this.isDashboardSelected = function(dashboardId) {
                    var currentRoutePath = $location.path();
                    var dashboardView = '/dashboardView/' + dashboardId;
                    var dashboardEditView = '/dashboardEditView/' + dashboardId;
                    return currentRoutePath === dashboardView || currentRoutePath === dashboardEditView;
                };

                this.getSmallIconFileName = function (dashboard) {
                    if (!dashboard) {
                        return 'img/questionFile32x32.png';
                    }

                    if (!dashboard.iconName) {
                        dashboard.iconName = 'questionFile';
                    }
                    return 'img/' + dashboard.iconName + '32x32.png';
                };

                this.getDashboardStatusIcon = function (dashboard) {
                    if (!dashboard) {
                        return '';
                    }

                    if (!dashboard.suspended) {
                        return self.isDashboardSelected(dashboard.id) ? 'img/spinnerWhite32x32.gif' : 'img/spinner32x32.gif';
                    }

                    return self.isDashboardSelected(dashboard.id) ? 'img/pauseWhite32x32.png' : 'img/pause32x32.png';
                };

                this.getMonitorImageUrl = function (dashboard) {
                    if (!dashboard.active) {
                        return '';
                    }
                    var hasInvalid = false;
                    var hasUnknown = false;
                    for (var i = 0; i < dashboard.monitors.length; i++) {
                        var monitor = dashboard.monitors[i];
                        if (monitor.value === 'Error') {
                            return 'img/redMonitor32x32.png';
                        } else if (!monitor.value ||
                            (monitor.value !== 'Unknown' && monitor.value !== 'OK')) {
                            hasInvalid = true;
                        } else if (monitor.value === 'Unknown') {
                            hasUnknown = true;
                        }
                    }
                    if (hasInvalid) {
                        return 'img/invalidMonitor32x32.png';
                    }
                    if (hasUnknown) {
                        return 'img/yellowMonitor32x32.png';
                    }
                    return 'img/greenMonitor32x32.png';
                };

                self.loadDashboards();

            },
            controllerAs: 'dashboardListController'
        };
    }]);
