'use strict';

angular.module('fldDashboardApp.importDashboardModalService', [])
    .service('importDashboardModalService', ['$uibModal', function ($uibModal) {
        this.show = function (options) {
            var modalInst = $uibModal.open({
                templateUrl: 'components/modal/importDashboardConfigModal.html',
                controller: 'ImportDashboardConfigController',
                controllerAs: 'importDashboardController',
                backdrop: 'static',
                size: 'lg',
                keyboard: false,
                resolve : {
                    modalOptions : function() {
                        return options;
                    }
                }
            });
            return modalInst.result;
        };
    }])
    .controller('ImportDashboardConfigController', function ($scope, $uibModalInstance, modalOptions, appEditStateService) {
        $scope.modalOptions = modalOptions;

        this.onCancel = function() {
            $uibModalInstance.close($scope.modalOptions.cancelButtonText);
        };

        $scope.$on('AllFileUploadsCompleted_importDashboard', function () {
            appEditStateService.syncDashboards();
        });

    });
