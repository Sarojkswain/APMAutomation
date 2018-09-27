'use strict';

var processUploader = null;
var processFormParamsCollapsed = {};
var diagramCollapsed = {};

angular.module('fldDashboardApp.uploadProcessView', ['ngRoute'])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/uploadProcess', {
            templateUrl: 'uploadProcessView/uploadProcessView.html',
            controller: 'UploadProcessViewController',
            controllerAs: 'uploadProcessController'
        })
    }])
    .controller('UploadProcessViewController', ['$http', '$scope', '$routeParams', 'appEditStateService',
        'FileUploader', 'modalService',
        function ($http, $scope, $routeParams, appEditStateService, FileUploader, modalService) {
            var self = this;
            $scope.procFormParamsCollapsed = processFormParamsCollapsed;
            $scope.diagrmCollapsed = diagramCollapsed;
            $scope.initWorkflowsFinished = false;
            $scope.initResourcesFinished = false;
            $scope.initWorkflowsFailed = false;
            $scope.initResourcesFailed = false;

            this.toggleProcFormProperties = function (procDef) {
                $scope.procFormParamsCollapsed[procDef.key] = !$scope.procFormParamsCollapsed[procDef.key];
            };

            this.toggleDiagram = function (procDef) {
                $scope.diagrmCollapsed[procDef.key] = !$scope.diagrmCollapsed[procDef.key];
            };

            this.fetchProcessDefinitions = function (callback) {
                appEditStateService.getProcessDefinitions(
                    function (data) {
                        if (callback) {
                            callback(true);
                        }

                        var processDefinitions = data.processDefinitions;
                        $scope.procDefList = data.processDefinitions;

                        for (var i = 0; i < processDefinitions.length; i++) {
                            var iProcDef = processDefinitions[i];

                            if ($scope.procFormParamsCollapsed[iProcDef.key] == null ||
                                (typeof $scope.procFormParamsCollapsed[iProcDef.key] === 'undefined')) {
                                $scope.procFormParamsCollapsed[iProcDef.key] = true;
                            }

                            if ($scope.diagrmCollapsed[iProcDef.key] == null ||
                                (typeof $scope.diagrmCollapsed[iProcDef.key] === 'undefined')) {
                                $scope.diagrmCollapsed[iProcDef.key] = true;
                            }
                        }
                    },
                    function (data, status) {
                        if (callback) {
                            callback(false);
                        }
                    }
                );
            };

            this.deleteAllWorkflows = function () {
                var modalOptions = {
                    cancelButtonText: 'Cancel',
                    discardAndLeaveButtonText: null,
                    saveAndLeaveButtonText: 'Delete All',
                    saveAndStayButtonText: null,
                    headerText: 'Delete all workflows',
                    bodyText: 'Do you really want to delete all workflows?'
                };

                modalService.show(modalOptions).then(function (result) {
                    if (result === modalOptions.saveAndLeaveButtonText) {
                        appEditStateService.deleteAllProcessDefinitions(false,
                            function (data, status) {
                                $scope.procDefList = data.processDefinitions;
                                appEditStateService.setAlert('success',
                                    'Successfully deleted all workflow deployments!');
                            },
                            function (data, status) {
                            }
                        );
                    }
                });
            };

            this.deleteWorkflow = function (deploymentId) {
                var modalOptions = {
                    cancelButtonText: 'Cancel',
                    discardAndLeaveButtonText: null,
                    saveAndLeaveButtonText: 'Delete',
                    saveAndStayButtonText: null,
                    headerText: 'Delete workflow',
                    bodyText: 'Do you really want to delete this workflow?'
                };
                modalService.show(modalOptions).then(function (result) {
                    if (result === modalOptions.saveAndLeaveButtonText) {
                        appEditStateService.deleteProcessDefinition(deploymentId, false,
                            function (data) {
                                $scope.procDefList = data.processDefinitions;
                                appEditStateService.setAlert('success',
                                    'Successfully deleted deployment by id = ' + deploymentId);
                            },
                            function (data, status) {
                            }
                        );
                    }
                });
            };

            this.fetchResources = function (callback) {
                appEditStateService.getAllResources(
                    function (data) {
                        if (callback) {
                            callback(true);
                        }
                        $scope.resourceFiles = data.resourceFiles;
                    },
                    function (data, status) {
                        if (callback) {
                            callback(false);
                        }
                    }
                );
            };

            this.deleteAllResources = function () {
                var modalOptions = {
                    cancelButtonText: 'Cancel',
                    discardAndLeaveButtonText: null,
                    saveAndLeaveButtonText: 'Delete All',
                    saveAndStayButtonText: null,
                    headerText: 'Delete all resources',
                    bodyText: 'Do you really want to delete all resources?'
                };

                modalService.show(modalOptions).then(function (result) {
                    if (result === modalOptions.saveAndLeaveButtonText) {
                        appEditStateService.deleteAllResources(
                            function (data, status) {
                                self.fetchResources();
                            },
                            function (data, status) {
                            }
                        );
                    }
                });
            };

            this.deleteResource = function (resource) {
                var modalOptions = {
                    cancelButtonText: 'Cancel',
                    discardAndLeaveButtonText: null,
                    saveAndLeaveButtonText: 'Delete',
                    saveAndStayButtonText: null,
                    headerText: 'Delete resource',
                    bodyText: "Do you really want to delete resource '" +
                    resource.name + "' (id=" + resource.id + ") ?"
                };

                modalService.show(modalOptions).then(function (result) {
                    if (result === modalOptions.saveAndLeaveButtonText) {
                        appEditStateService.deleteResourceById(resource.id,
                            function (data, status) {
                                self.fetchResources();
                            },
                            function (data, status) {
                            }
                        );
                    }
                });
            };

            $scope.uploader = processUploader ? processUploader : new FileUploader({
                url   : 'api/upload',
                alias : 'processArchive'
            });

            if (!processUploader) {
                processUploader = $scope.uploader;
            }

            self.fetchProcessDefinitions(
                function (isSuccess) {
                    $scope.initWorkflowsFinished = true;
                    $scope.initWorkflowsFailed = !isSuccess;
                }
            );

            self.fetchResources(
                function (isSuccess) {
                    $scope.initResourcesFinished = true;
                    $scope.initResourcesFailed = !isSuccess;
                }
            );

            $scope.$on('FileUploadSucceeded_workflows', function (event, response) {
                if (appEditStateService.isSuccessData(response)) {
                    $scope.procDefList = response.processDefinitions;
                    $scope.resourceFiles = response.resourceFiles;
                }
            });

        }
    ]
);