'use strict';

var nodePluginsExpanded = {};
var nodePlugins = {};
var showNodePlugins = {};

angular.module('fldDashboardApp.nodeListView', ['ngRoute'])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/nodeList', {
            templateUrl: 'nodeList/nodeListView.html',
            controller: 'NodeListController',
            controllerAs: 'nodeListController'
        })
    }])
    .controller('NodeListController', ['$http', '$scope', '$routeParams', '$interval',
        'appEditStateService', 'nodeService', 'modalService',
        function ($http, $scope, $routeParams, $interval, appEditStateService, nodeService, modalService) {
            var self = this;
            $scope.initNodesFinished = false;
            $scope.initNodesFailed = false;
            $scope.nodePluginsExpanded = nodePluginsExpanded;
            $scope.nodePlugins = nodePlugins;
            $scope.showNodePlugins = showNodePlugins;
            $scope.latestAgentVersion = null;

            $scope.formatPluginAPIOperationString =

                this.formatPluginAPIOperationString = function (operation) {
                    var str = operation.name + '(';
                    for (var i = 0; operation.parameters && i < operation.parameters.length; i++) {
                        var param = operation.parameters[i];
                        str += param.javaType + (param.name ? (" " + param.name) : "") + (i == operation.parameters.length - 1 ? "" : ", ")
                    }
                    str += ")";
                    return str;
                }
            ;

            this.getNodes = function (callback) {
                appEditStateService.getNodeList(
                    function (data, status) {
                        if (callback) {
                            callback(true);
                        }

                        var nodes = data.nodes;
                        if (!nodeService.areNodeListsEqual($scope.nodeList, nodes)) {
                            $scope.nodeList = nodes;
                        }
                        if ($scope.latestAgentVersion !== data.latestAgentDistributionVersion) {
                            $scope.latestAgentVersion = data.latestAgentDistributionVersion;
                        }

                    },
                    function (data, status) {
                        if (callback) {
                            callback(false);
                        }
                    }
                );
            };

            this.init = function () {
                self.getNodes(function(isSuccess) {
                    $scope.initNodesFinished = true;
                    $scope.initNodesFailed = !isSuccess;
                });

                self.heartBeatFunction = $interval(
                    function () {
                        self.getNodes();
                    },
                    10000);
            };

            this.deleteNodeById = function (nodeId) {
                var modalOptions = {
                    cancelButtonText: 'Cancel',
                    discardAndLeaveButtonText: null,
                    saveAndLeaveButtonText: 'Delete',
                    saveAndStayButtonText: null,
                    headerText: 'Delete node',
                    bodyText: 'Do you really want to delete this node?'
                };
                modalService.show(modalOptions).then(function (result) {
                    if (result === modalOptions.saveAndLeaveButtonText) {
                        appEditStateService.deleteNodeById(nodeId,
                            function (data) {
                                if (!nodeService.areNodeListsEqual($scope.nodeList, data.nodes)) {
                                    $scope.nodeList = data.nodes;
                                }
                                if ($scope.latestAgentVersion !== data.latestAgentDistributionVersion) {
                                    $scope.latestAgentVersion = data.latestAgentDistributionVersion;
                                }
                            },
                            function (data, status) {
                            }
                        );
                    }
                });
            };

            this.destroyHeartBeatFunction = function () {
                if (self.heartBeatFunction) {
                    $interval.cancel(self.heartBeatFunction);
                    self.heartBeatFunction = null;
                }
            };

            this.toggleNodePlugins = function (nodeName) {
                $scope.nodePluginsExpanded[nodeName] = !$scope.nodePluginsExpanded[nodeName];
                var nodePlugins = $scope.nodePlugins[nodeName];

                if (!$scope.nodePluginsExpanded[nodeName]) {
                    self.modifyShowNodePluginsContext(nodeName, false);
                    if (nodePlugins) {
                        $scope.nodePlugins[nodeName] = null;

                    }
                } else {
                    if (!nodePlugins) {
                        self.modifyShowNodePluginsContext(nodeName, false);

                        appEditStateService.getNodeAgentPlugins(nodeName,
                            function (data, status) {
                                $scope.nodePlugins[nodeName] = data.plugins;
                                self.modifyShowNodePluginsContext(nodeName, true);
                            },
                            function (data, status) {
                                self.modifyShowNodePluginsContext(nodeName, false, 'img/skull64x64.png', true);
                            }
                        );
                    }
                }
            };

            this.modifyShowNodePluginsContext = function (nodeName, show, statusImgUrl, failed) {
                var showNodePluginsCtx = $scope.showNodePlugins[nodeName];
                if (!statusImgUrl) {
                    statusImgUrl = 'img/spinner64x64.gif';
                }
                if (showNodePluginsCtx) {
                    showNodePluginsCtx['show'] = show;
                    showNodePluginsCtx['imgUrl'] = statusImgUrl;
                    showNodePluginsCtx['failed'] = failed;
                } else {
                    $scope.showNodePlugins[nodeName] = { show : show, imgUrl : statusImgUrl, failed : failed };
                }
            };

            this.getNodePlugins = function (nodeName) {
                var plugins = $scope.nodePlugins[nodeName];
                if (plugins) {
                    return plugins;
                }
                return [];
            };

            this.runPluginOperation = function (node, plugin, operation) {

            };

            $scope.$on('$destroy', function () {
                self.destroyHeartBeatFunction();
            });

            self.init();

        }]);