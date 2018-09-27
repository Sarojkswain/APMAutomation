'use strict';

angular.module('fldDashboardApp.notFoundView', ['ngRoute'])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/notFound', {
            templateUrl: 'notFoundView/notFoundView.html',
            controller: 'NotFoundViewController'
        });
    }])
    .controller('NotFoundViewController', ['appEditStateService', function (appEditStateService) {
    }]);