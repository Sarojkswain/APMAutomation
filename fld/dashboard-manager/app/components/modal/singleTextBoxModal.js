'use strict';

angular.module('fldDashboardApp.singleTextBoxModalService', [])
    .service('singleTextBoxModalService', ['$uibModal', function ($uibModal) {
        this.show = function (options) {
            return $uibModal.open({
                templateUrl: 'components/modal/singleTextBoxModal.html',
                controller: 'SingleTextModalController',
                controllerAs: 'singleTextModalController',
                backdrop: 'static',
                keyboard: false,
                resolve : {
                    modalOptions : function() {
                        return options;
                    }
                }

            }).result;
        };
    }])
    .controller('SingleTextModalController', function ($scope, $uibModalInstance, modalOptions) {
        $scope.textInput = '';
        $scope.modalOptions = modalOptions;

        this.onOk = function() {
            $uibModalInstance.close($scope.textInput);
        };

        this.onCancel = function() {
            $uibModalInstance.dismiss($scope.modalOptions.cancelButtonText);
        };

    });
