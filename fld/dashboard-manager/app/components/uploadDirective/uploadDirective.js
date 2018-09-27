'use strict';

/**
 * Directive for uploading files with drag n drop support and upload status information.
 *
 */
angular.module('fldDashboardApp.uploadDirective', [])
    .directive('fileUploader', [ 'FileUploader', function (FileUploader) {
        return {
            restrict: 'E',
            templateUrl: 'components/uploadDirective/uploadDirective.html',
            scope: {
                customerInfo : '=info',
                title : '=?title',
                dropZoneLabel : '=dropZoneLabel',
                customOverClass : '=customOverClass',
                dropZoneClass : '=dropZoneClass',
                submitUrl : '=submitUrl',
                fileAlias : '=fileAlias',
                uploader : '=?',
                eventSuffix : '=eventSuffix'
            },
            controller : function ($scope, appEditStateService, FileUploader) {
                $scope.deployedNumber = 0;
                if (!$scope.eventSuffix) {
                    $scope.eventSuffix = '';
                }
                if (!$scope.uploader) {
                    $scope.uploader = new FileUploader({
                        url   : $scope.submitUrl,
                        alias : $scope.fileAlias
                    });
                }
                var uploader = $scope.uploader;

                //FileUploader filters
                uploader.filters.push({
                    name: 'fileNumberLimitFilter',
                    fn: function(item, options) {
                        return this.queue.length < 10;
                    }
                });

                //FileUploader callbacks
                uploader.onWhenAddingFileFailed = function(item /*{File|FileLikeObject}*/, filter, options) {
                    appEditStateService.setAlert('danger', "Failed to add file '" + item + "'!");
                };

                //uploader.onAfterAddingFile = function(fileItem) {
                //    console.info('onAfterAddingFile', fileItem);
                //};

                uploader.onAfterAddingAll = function(addedFileItems) {
                };

                //uploader.onBeforeUploadItem = function(item) {
                //    console.info('onBeforeUploadItem', item);
                //};
                //uploader.onProgressItem = function(fileItem, progress) {
                //    console.info('onProgressItem', fileItem, progress);
                //};
                //uploader.onProgressAll = function(progress) {
                //    console.info('onProgressAll', progress);
                //};

                uploader.onSuccessItem = function(fileItem, response, status, headers) {
                    appEditStateService.broadcastEvent('FileUploadSucceeded_' + $scope.eventSuffix, response);
                    $scope.deployedNumber++;
                };

                uploader.onErrorItem = function(fileItem, response, status, headers) {
                    var msg = "Failed to deploy! ";
                    if (response && response.errors) {
                        for (var i = 0; i < response.errors.length; i++) {
                            msg += response.errors[i] + ' ';
                        }
                    }
                    appEditStateService.setAlert('danger', msg);
                };

                uploader.onCancelItem = function(fileItem, response, status, headers) {
                    appEditStateService.setAlert('warn', "Upload of file '" + fileItem + "' is cancelled!")
                };

                uploader.onCompleteItem = function(fileItem, response, status, headers) {
                };

                uploader.onCompleteAll = function() {
                    if ($scope.deployedNumber > 0) {
                        appEditStateService.broadcastEvent('AllFileUploadsCompleted_' + $scope.eventSuffix);
                        appEditStateService.setAlert('success', 'Successfully deployed ' + $scope.deployedNumber + ' files!');
                    }
                    $scope.deployedNumber = 0;
                };
            }
        };
    }]);
