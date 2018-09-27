'use strict';

angular.module('fldDashboardApp.homeView', ['ngRoute'])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/', {
            templateUrl: 'homeView/homeView.html',
            controller: 'HomeViewController'
        });
    }])
    .controller('HomeViewController', ['appEditStateService', function (appEditStateService) {
    }]);