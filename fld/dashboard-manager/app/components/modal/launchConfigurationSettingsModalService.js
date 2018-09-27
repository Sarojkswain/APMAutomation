'use strict';

angular.module('fldDashboardApp.launchConfigurationSettingsModalService', [])
    .service('launchConfigSettingsModalService', ['$uibModal', function($uibModal) {
        this.show = function (options, dboard) {
            return $uibModal.open({
                templateUrl  : 'components/modal/launchConfigurationSettingsModal.html',
                controller   : 'LaunchConfigurationSettingsModalController',
                controllerAs : 'workflowParametersModalController',
                backdrop     : 'static',
                keyboard     : true,
                resolve      : {
                    modalOptions : function() {
                        return options;
                    },
                    dashboard : function() {
                        return dboard;
                    }
                }
            }).result;
        };
    }])
    .controller('LaunchConfigurationSettingsModalController', function($scope, $uibModalInstance, modalOptions, dashboard) {
        var self = this;
        this.modalOptions = modalOptions;
        this.isDirty = false;

        $scope.config = angular.copy(dashboard.config);
        $scope.hideNonRequiredConfigParameters = angular.copy(dashboard.hideNonRequiredConfigParameters);
        $scope.workflowId = dashboard.processKey;
        $scope.modalOptions = modalOptions;

        this.onSaveAndRun = function () {
            $uibModalInstance.close({ returnCode : self.modalOptions.saveAndRunButtonText,
                workflowConfiguration : $scope.config, hideNonRequiredConfigParameters : $scope.hideNonRequiredConfigParameters });
        };

        this.onSaveDontRun = function () {
            $uibModalInstance.close({ returnCode : modalOptions.saveDontRunButtonText,
                workflowConfiguration : $scope.config, hideNonRequiredConfigParameters : $scope.hideNonRequiredConfigParameters });
        };

        this.onRunDontSave = function () {
            self.setHasChanges(false);
            $uibModalInstance.close({ returnCode : modalOptions.runDontSaveButtonText,
                workflowConfiguration : $scope.config, hideNonRequiredConfigParameters : $scope.hideNonRequiredConfigParameters });
        };

        this.onCancel = function () {
            self.setHasChanges(false);
            $uibModalInstance.close({ returnCode : modalOptions.cancelButtonText, workflowConfiguration : null, hideNonRequiredConfigParameters : null });
        };

        this.setHasChanges = function(hasChanges) {
            self.isDirty = hasChanges;
        };

        this.hasChanges = function() {
            return self.isDirty;
        };

        this.updateWorkflowConfiguration = function(workflowConfiguration, hideNonRequiredConfigParams) {
            $scope.config = workflowConfiguration;
            $scope.hideNonRequiredConfigParameters = hideNonRequiredConfigParams;
        };

        $scope.setHasChanges = self.setHasChanges;
        $scope.updateWorkflowConfiguration = self.updateWorkflowConfiguration;

    });
