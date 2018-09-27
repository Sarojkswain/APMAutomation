'use strict';

angular.module('fldDashboardApp.twoTextBoxesModalService', [])
    .service('twoTextBoxesModalService', ['$uibModal', function ($uibModal) {
        this.show = function (options) {
            return $uibModal.open({
                templateUrl: 'components/modal/twoTextBoxesModal.html',
                controller: 'TwoTextBoxesModalController',
                controllerAs: 'twoTextBoxesModalController',
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
    .controller('TwoTextBoxesModalController', function ($scope, $uibModalInstance, modalOptions) {
        $scope.textInput1 = modalOptions.textInput1;
        $scope.textInput2 = modalOptions.textInput2;

        $scope.modalOptions = modalOptions;

        this.onOk = function() {
            $uibModalInstance.close({ text1: $scope.textInput1, text2: $scope.textInput2 });
        };

        this.onCancel = function() {
            $uibModalInstance.dismiss('cancel');
        };

    });
