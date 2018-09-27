'use strict';

angular.module('fldDashboardApp.userTaskDirective', [])
    .directive('userTask', [function ($scope) {
        return {
            restrict: 'E',
            templateUrl: 'components/userTaskDirective/userTaskDirective.html',
            link: function (scope, element, attrs) {
                scope.hasFocus = (attrs.hasfocus === 'true');
            }
        };
    }])
    .controller('UserTaskDirectiveController', function ($scope, appEditStateService, configParamService) {

        var self = this;

        $scope.userTaskFormData = $scope.selectedUserTask.formData;

        this.fetchNodeList = function () {
            appEditStateService.getNodeList(
                function (data) {
                    $scope.nodeList = data.nodes;
                },
                function () {
                }
            );
        };

        this.init = function () {
            if (!$scope.nodeList) {
                self.fetchNodeList();
            }
        };

        this.setHasChanges = function (hasChanges) {
            if ($scope.setHasChanges) {
                $scope.setHasChanges(hasChanges);
            }
        };

        this.getLocalizedParamName = function (key) {
            return appEditStateService.getKeyLocalization(key);
        };

        this.getValueTextForBooleanParameter = function (param) {
            if (self.paramHasValue(param)) {
                return param.value ? 'True ' : 'False ';
            }
            return 'Select value... ';
        };

        this.getValueTextForNodeParameter = function (param) {
            if (self.paramHasValue(param)) {
                return param.value;
            }
            return 'Select value... ';
        };

        this.updateEnumParam = function (param, enumKey) {
            var oldVal = param.value;
            configParamService.updateEnumParam(param, enumKey);
            var newVal = param.value;
            self.setHasChanges(newVal !== oldVal);

        };

        this.getEnumValueLabels = function (param) {
            return configParamService.getEnumValueLabels(param);
        };

        this.getEnumParamValueLabel = function (param) {
            return configParamService.getEnumParamValueLabel(param);
        };

        this.enumParamHasValue = function (param) {
            return configParamService.enumParamHasValue(param);
        };

        this.updateBooleanParameter = function (param, boolValue) {
            self.setHasChanges(param.value !== boolValue);
            param.value = boolValue;
        };

        this.updateNodeParameter = function (param, node) {
            self.setHasChanges(param.value !== node.name);
            param.value = node.name;
        };

        this.paramHasValue = function (param) {
            return param && param.value !== undefined && param.value !== null;
        };

    });
