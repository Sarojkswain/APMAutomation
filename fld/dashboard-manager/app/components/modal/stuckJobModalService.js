'use strict';

angular.module('fldDashboardApp.stuckJobModalService', [])
    .service('stuckJobModalService', ['$uibModal', function($uibModal) {
        this.show = function (options, jobs, stuckJobReLaunchSuccessCallback, stuckJobReLaunchErrorCallback) {
            var modalInst = $uibModal.open({
                templateUrl  : 'components/modal/stuckJobModal.html',
                controller   : 'StuckJobModalServiceController',
                controllerAs : 'jobModalController',
                size         : 'lg',
                backdrop     : 'static',
                keyboard     : true,
                resolve      : {
                    modalOptions : function () {
                        return options;
                    },
                    stuckJobs : function () {
                        return jobs;
                    },
                    successCallback : function () {
                        return stuckJobReLaunchSuccessCallback;
                    },
                    errorCallback : function () {
                        return stuckJobReLaunchErrorCallback;
                    }
                }
            });
            return modalInst.result;
        };
    }])
    .controller('StuckJobModalServiceController', function($scope, $uibModalInstance, modalOptions, stuckJobs,
                                                           successCallback, errorCallback, appEditStateService) {
        var self = this;

        this.successCallback = successCallback;
        this.errorCallback = errorCallback;

        $scope.jobs = stuckJobs;
        $scope.modalOptions = modalOptions;

        this.reLaunchJob = function (job) {
            appEditStateService.reLaunchJobs([ job ],
                function (data) {
                    self.successCallback(data);
                    self.removeJob(job);
                    if ($scope.jobs.length === 0) {
                        $uibModalInstance.close({ returnCode : 'reLaunched', modifiedStuckJobs : $scope.jobs })
                    }
                },
                function (data, status) {
                    self.errorCallback(data, status);
                }
            );
        };

        this.onReLaunchAll = function () {
            appEditStateService.reLaunchJobs($scope.jobs,
                function (data) {
                    self.successCallback(data);
                    $scope.jobs = [];
                    $uibModalInstance.close({ returnCode : 'reLaunchedAll', modifiedStuckJobs : $scope.jobs });
                },
                function (data, status) {
                    self.errorCallback(data, status);
                }
            );
        };

        this.removeJob = function (job) {
            var ind = -1;
            for (var i = 0; i < $scope.jobs.length; i++) {
                var curJob = $scope.jobs[i];
                if (curJob.id === job.id) {
                    ind = i;
                    break;
                }
            }
            if (ind !== -1) {
                $scope.jobs.splice(ind, 1);
            }
        };

        this.onCancel = function () {
            $uibModalInstance.close({ returnCode : $scope.modalOptions.cancelButtonText, modifiedStuckJobs : $scope.jobs });
        };

    });
