'use strict';

var currentGroovyConsoleContent = [];
var currentGroovyScriptContent = 'System.out.println("hello world!");';
var originalTopMenuBarDisplayStyle = '';
var originalSideBarDisplayStyle = '';

angular.module('fldDashboardApp.groovyConsoleView', ['ngRoute'])
    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/groovyConsole', {
            templateUrl: 'groovyConsoleView/groovyConsoleView.html',
            controller: 'GroovyConsoleViewController',
            controllerAs: 'groovyConsoleController'
        })
    }])
    .controller('GroovyConsoleViewController', ['$http', '$scope', '$routeParams',
        'appEditStateService', 'modalService',
        function ($http, $scope, $routeParams, appEditStateService, modalService) {
            var self = this;

            $scope.codeMirrorEditorOptions = {
                lineNumbers: true,
                matchBrackets: true,
                autoCloseBrackets: true,
                mode: 'groovy',
                theme: 'dracula',
                extraKeys: {
                    "Ctrl-Space" : "autocomplete",
                    "F11": function(cm) {
                        var isFullScreen = cm.getOption("fullScreen");
                        if (!isFullScreen) {
                            self.goFullScreenMode();
                        } else {
                            self.exitFullScreenMode();
                        }
                        cm.setOption("fullScreen", !isFullScreen);
                    },
                    "Esc": function(cm) {

                        if (cm.getOption("fullScreen")) {
                            cm.setOption("fullScreen", false)
                            self.exitFullScreenMode();
                        };
                    }
                }
            };
            $scope.groovyScriptContent = currentGroovyScriptContent;
            $scope.groovyConsoleContent = currentGroovyConsoleContent;


            this.executeGroovyScript = function () {
                appEditStateService.runGroovyScript($scope.groovyScriptContent,
                    function (data, status) {
                        var timeStamp = (new Date).toISOString().replace(/z|t/gi,' ').trim();
                        currentGroovyConsoleContent.push('-------------------- ' + timeStamp + ' --------------------');
                        currentGroovyConsoleContent.push(data.output);
                        $scope.groovyConsoleContent = currentGroovyConsoleContent;
                    },
                    function (data, status) {
                    }
                );
            };

            this.clearOutput = function () {
                currentGroovyConsoleContent = [];
                $scope.groovyConsoleContent = currentGroovyConsoleContent;
            };

            this.exitFullScreenMode = function () {
                var topMenuBarDisplayStyle = document.getElementById("topMenuBarElement").style.display;
                if (topMenuBarDisplayStyle === 'none') {
                    document.getElementById("topMenuBarElement").style.display = originalTopMenuBarDisplayStyle;
                }

                var sideBarDisplayStyle = document.getElementById("sidebarPanel").style.display;
                if (sideBarDisplayStyle === 'none') {
                    document.getElementById("sidebarPanel").style.display = originalSideBarDisplayStyle;
                }
            };

            this.goFullScreenMode = function () {
                var topMenuBarDisplayStyle = document.getElementById("topMenuBarElement").style.display;
                if (topMenuBarDisplayStyle !== 'none') {
                    originalTopMenuBarDisplayStyle = topMenuBarDisplayStyle;
                    document.getElementById("topMenuBarElement").style.display = 'none';
                }

                var sideBarDisplayStyle = document.getElementById("sidebarPanel").style.display;
                if (sideBarDisplayStyle !== 'none') {
                    originalSideBarDisplayStyle = sideBarDisplayStyle;
                    document.getElementById("sidebarPanel").style.display = 'none';
                }
            };

            $scope.$on('$destroy', function () {
                currentGroovyScriptContent = $scope.groovyScriptContent;
            });

        }
    ]);
