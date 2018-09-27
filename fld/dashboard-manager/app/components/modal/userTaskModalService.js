'use strict';

angular.module('fldDashboardApp.userTaskModalService', [])
    .service('userTaskModalService', ['$uibModal', function($uibModal) {
        this.show = function (options, dashboardId, usrTasks, userTaskSuccessfulResolutionCallback, userTaskUnsuccessfulResolutionCallback) {
            return $uibModal.open({
                templateUrl  : 'components/modal/userTaskModal.html',
                controller   : 'UserTaskModalServiceController',
                controllerAs : 'userTaskModalController',
                backdrop     : 'static',
                keyboard     : true,
                resolve      : {
                    modalOptions : function () {
                        return options;
                    },
                    userTasks : function () {
                        return usrTasks;
                    },
                    dashboardId : function () {
                        return dashboardId;
                    },
                    successCallback : function () {
                        return userTaskSuccessfulResolutionCallback;
                    },
                    errorCallback : function () {
                        return userTaskUnsuccessfulResolutionCallback;
                    }
                }
            }).result;
        };
    }])
    .controller('UserTaskModalServiceController', function($scope, $uibModalInstance, modalOptions, userTasks, dashboardId,
                                                           successCallback, errorCallback, appEditStateService) {
        var self = this;

        this.successCallback = successCallback;
        this.errorCallback = errorCallback;

        $scope.selectedUserTask = null;
        $scope.userTasks = userTasks;
        $scope.modalOptions = modalOptions;
        $scope.title = $scope.modalOptions.headerText;
        $scope.dashboardId = dashboardId;

        this.onSelectUserTask = function (userTask) {
            $scope.selectedUserTask = userTask;
            $scope.title = userTask ? userTask.name : $scope.modalOptions.headerText;
        };

        this.onApply = function () {
            appEditStateService.completeUserTasks($scope.dashboardId, $scope.selectedUserTask,
                function (data) {
                    self.successCallback($scope.selectedUserTask);
                    self.removeUserTask($scope.selectedUserTask);
                    self.onSelectUserTask(null);
                    if ($scope.userTasks.length === 0) {
                        $uibModalInstance.close({ returnCode : 'resolvedAll', modifiedUserTasks : $scope.userTasks });
                    }
                },
                function (data, status) {
                    self.errorCallback($scope.selectedUserTask);
                }
            );
        };

        this.onBackToList = function () {
            self.onSelectUserTask(null);
        };

        this.removeUserTask = function (userTask) {
            var ind = -1;
            for (var i = 0; i < $scope.userTasks.length; i++) {
                var iTask = $scope.userTasks[i];
                if (iTask.id === userTask.id) {
                    ind = i;
                    break;
                }
            }
            if (ind !== -1) {
                $scope.userTasks.splice(ind, 1);
            }
        };

        this.onCancel = function () {
            $uibModalInstance.close({ returnCode : $scope.modalOptions.cancelButtonText, modifiedUserTasks : $scope.userTasks });
        };

    });
