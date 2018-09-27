'use strict';

angular.module('fldDashboardApp.processKeyDropDownDirective', [])
    .directive('processKeyDropDown', function () {
        return {
            restrict: 'E',
            templateUrl: 'components/processKeyDropDownDirective/processKeyDropDownDirective.html',
            scope: {
                value: "="
            },
            controller: 'ProcessKeyDropDownController',
            controllerAs: 'procKeyDropDownController'
        };
    })
    .controller('ProcessKeyDropDownController', function ($scope, appEditStateService) {

        appEditStateService.getProcessDefinitions(
            function (data) {
                $scope.procDefinitions = data.processDefinitions;
            },
            function (data, status) {
            }
        );

        this.setValue = function (procKey) {
            $scope.value = procKey;
        };

        this.getValueLabel = function () {
            return $scope.value ? $scope.value : 'Select process key...';
        };

    });
