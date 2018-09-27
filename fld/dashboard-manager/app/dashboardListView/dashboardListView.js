'use strict';

angular.module('fldDashboardApp.dashboardListView', ['ngRoute'])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/dashboardList', {
            templateUrl: 'dashboardListView/dashboardListView.html',
            controller: 'DashboardListViewController',
            controllerAs: 'dashboardListCtrl'
        })
    }])
    .controller('DashboardListViewController', ['$http', '$scope', '$routeParams', 'appEditStateService',
        function ($http, $scope, $routeParams, appEditStateService) {

            var self = this;

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
                $scope.dashboards = dashboards;
            });

            $scope.$on('DashboardUpdate', function (event, dashboard) {
                for (var i = 0; $scope.dashboards && i < $scope.dashboards.length; i++) {
                    var iDashboard = $scope.dashboards[i];
                    if (dashboard.id === iDashboard.id) {
                        $scope.dashboards[i] = dashboard;
                    }
                }
            });

            this.loadDashboards = function () {
                appEditStateService.getDashboards(
                    function (dashboards) {
                        $scope.dashboards = dashboards;
                    },
                    function (data, status) {
                    }
                );
            };

            this.getSpinnerIconForDashboard = function (dashboard) {
                if (!dashboard) {
                    return '';
                }
                if (dashboard.active) {
                    if (!dashboard.suspended) {
                        return 'img/spinner32x32.gif';
                    } else {
                        return 'img/pause32x32.png';
                    }
                }
                return '';
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

            this.loadDashboards();

        }
    ]);