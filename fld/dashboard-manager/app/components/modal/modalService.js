'use strict';

angular.module('fldDashboardApp.modalService', [])
    .service('modalService', ['$uibModal', function($uibModal) {
        this.show = function (options) {
            return $uibModal.open({
                templateUrl  : 'components/modal/modal.html',
                controller   : 'ModalController',
                controllerAs : 'controller',
                backdrop     : 'static',
                keyboard     : true,
                resolve      : {
                    modalOptions : function() {
                        return options;
                    }
                }
            }).result;
        };
    }])
    .controller('ModalController', function($scope, $uibModalInstance, modalOptions) {
        this.modalOptions = modalOptions;
        $scope.modalOptions = modalOptions;

        this.onSaveAndLeave = function (result) {
            $uibModalInstance.close(result);
        };
        this.onSaveAndStay = function (result) {
            $uibModalInstance.close(result);
        };
        this.onCancel = function (result) {
            $uibModalInstance.close(result);
        };
        this.onCancelAndLeave = function (result) {
            $uibModalInstance.close(result);
        };
    });
