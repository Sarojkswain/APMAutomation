'use strict';

var agentUploader = null;

angular.module('fldDashboardApp.uploadAgentView', ['ngRoute'])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/uploadAgent', {
            templateUrl: 'uploadAgentView/uploadAgentView.html',
            controller: 'UploadAgentViewController',
            controllerAs: 'uploadAgentController'
        })
    }])
    .controller('UploadAgentViewController', ['$http', '$scope', '$routeParams', 'appEditStateService', 'FileUploader',
        function ($http, $scope, $routeParams, appEditStateService, FileUploader) {
            $scope.uploader = agentUploader ? agentUploader : new FileUploader({
                url   : 'api/agent',
                alias : 'agentZip'
            });
            if (!agentUploader) {
                agentUploader = $scope.uploader;
            }

            var self = this;
            self.hasDistro = false;
            self.downloadButtonDisabled = true;
            self.agentDownloadServer = 'fldcontroll01c:8080';
            self.agentDownloadPath = '/LoadOrchestrator/api/agent';
            self.fileDownloadCacheUrl = 'http://fldcontroll01c:8080/LoadOrchestrator/filecache/download';
            self.activeMQBrokerUrl = 'tcp://fldcontroll01c:61616';


            this.checkAgentDistro = function () {
                appEditStateService.hasAgentDistribution(
                    function (data, status) {
                        self.hasDistro = data.hasAgentDistribution;
                        self.downloadButtonDisabled = !data.hasAgentDistribution;
                    },
                    function (data, status) {
                    }
                );
            };

            this.downloadAgentDistro = function () {
                self.downloadButtonDisabled = true;
                appEditStateService.downloadAgentDistribution({
                    activeMQBrokerUrl : self.activeMQBrokerUrl,
                    fileDownloadCacheUrl : self.fileDownloadCacheUrl,
                    agentDownloadPath : self.agentDownloadPath,
                    agentDownloadServer : self.agentDownloadServer
                },
                function (request) {
                    self.downloadButtonDisabled = false;
                });
            };

            $scope.$on('FileUploadSucceeded_agent', function (event, response) {
                if (appEditStateService.isSuccessData(response)) {
                    self.hasDistro = true;
                    self.downloadButtonDisabled = false;
                }
            });

            self.checkAgentDistro();

        }]);