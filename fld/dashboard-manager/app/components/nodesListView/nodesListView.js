'use strict';

angular.module('fldDashboardApp.nodeListViewDirective', [])
    .directive('nodeList', ['$uibModal', '$http', function ($uibModal, $http, $scope) {
        return {
            restrict: 'E',
            templateUrl: 'components/nodesListView/nodesListView.html',
            controller: 'NodeListController',
            controllerAs: 'controller'
        };
    }])
    .controller('NodeListController', function ($uibModal, $scope, $http, appEditStateService) {
        this.openNodesList = function () {
            $uibModal.open({
                templateUrl: 'nodeSelectorModal',
                controller: 'NodeSelectorController',
                controllerAs: 'nodeSelectorController',
                backdrop: 'static',
                keyboard: false
            }).result.then(function (result) {
                    $scope.selectedNode = result;
                });

        };

    })
    .controller('NodeSelectorController', function ($scope, $uibModalInstance) {
        $scope.fldNodeName = '';

        this.onOk = function() {
            $uibModalInstance.close($scope.fldNodeName);
        };

        this.onCancel = function() {
            $uibModalInstance.dismiss('cancel');
        };

    });
