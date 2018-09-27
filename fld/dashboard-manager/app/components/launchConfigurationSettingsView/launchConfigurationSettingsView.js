'use strict';

/**
 * Directive to show process configuration parameters.
 *
 */
angular.module('fldDashboardApp.launchConfigurationSettingsViewDirective', [])
    .directive('workflowParameters', [function ($scope) {
        return {
            restrict: 'E',
            templateUrl: 'components/launchConfigurationSettingsView/launchConfigurationSettingsView.html',
            link: function (scope, element, attrs) {
                scope.hasFocus = (attrs.hasfocus === 'true');
            }
        };
    }])

    .controller('LaunchConfigurationSettingsController', function ($scope,  appEditStateService, configParamService) {
        var self = this;

        this.toggleShowSelectableConfigItems = function () {
            $scope.selectableConfigItemsCollapsed = !$scope.selectableConfigItemsCollapsed;
        };

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
            if ($scope.config) {
                if (!$scope.nodeList) {
                    for (var i = 0; $scope.config && i < $scope.config.length; i++) {
                        var workflowParam = $scope.config[i];
                        if (workflowParam.type === 'node') {
                            self.fetchNodeList();
                            break;
                        }
                    }
                }
            } else if (!$scope.nodeList) {
                //we don't have config array - we can't assume anything
                //so, just get the node list anyway
                self.fetchNodeList();
            }

            jQuery(document).ready( function() {
                $('#selectableConfigItemsContainer').dblclick( function() {
                    $(this).select();
                    if (document.body.createTextRange) {
                        var range = document.body.createTextRange();
                        range.moveToElementText(this);
                        range.select();
                    } else if (window.getSelection) {
                        var selection = window.getSelection();
                        var range = document.createRange();
                        range.selectNodeContents(this);
                        selection.removeAllRanges();
                        selection.addRange(range);
                    }
                });
            });
            $scope.selectableConfigItemsCollapsed = true;
        };

        this.onSelectConfigurationParameterRow = function (id) {
            var selectorId = '#' + id;
            $(selectorId).select();
            if (document.body.createTextRange) {
                var range = document.body.createTextRange();
                range.moveToElementText($(selectorId).get(0));
                range.select();
            } else if (window.getSelection) {
                var selection = window.getSelection();
                var range = document.createRange();
                range.selectNodeContents($(selectorId).get(0));
                selection.removeAllRanges();
                selection.addRange(range);
            }
        };

        this.setHasChanges = function (hasChanges) {
            $scope.setHasChanges(hasChanges);
            $scope.updateWorkflowConfiguration($scope.config, $scope.hideNonRequiredConfigParameters);
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

        this.getValueTextForEnumParameter = function (param) {
            if (self.paramHasValue(param)) {
                return configParamService.getEnumParamValueLabel(param);
            }
            return 'Select value...';
        };

        this.getEnumValueLabels = function (param) {
            return configParamService.getEnumValueLabels(param);
        };

        this.updateEnumParameter = function (workflowParam, enumKey) {
            var oldVal = workflowParam.value;
            configParamService.updateEnumParam(workflowParam, enumKey);
            var newVal = workflowParam.value;
            self.setHasChanges(newVal !== oldVal);
        };

        this.getLocalizedParamName = function (key) {
            return appEditStateService.getKeyLocalization(key);
        };

        this.paramHasValue = function (param) {
            return param.value !== undefined && param.value !== null;
        };

        this.enumParamHasValue = function (param) {
            return configParamService.enumParamHasValue(param);
        };

        this.updateBooleanParameter = function (param, boolValue) {
            self.setHasChanges(param.value !== boolValue);
            param.value = boolValue;
        };

        this.updateNodeParameter = function (workflowParam, node) {
            self.setHasChanges(workflowParam.value !== node.name);
            workflowParam.value = node.name;
        };

        this.hideParameter = function (workflowParam) {
            return !workflowParam.required && $scope.hideNonRequiredConfigParameters;
        };

        this.init();
    });
