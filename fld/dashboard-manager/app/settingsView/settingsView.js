/**
 * Created by sinal04 on 22/02/2016.
 */

'use strict';

var logMonitorRecipients = [];
var globalLOAppSettings = localStorage.loadOrchestratorSettings ? angular.fromJson(localStorage.loadOrchestratorSettings) : {};

angular.module('fldDashboardApp.settingsView', ['ngRoute'])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/settings', {
            templateUrl: 'settingsView/settingsView.html',
            controller: 'SettingsController',
            controllerAs: 'settingsController'
        })
    }])
    .controller('SettingsController', ['$http', '$scope', '$routeParams', '$interval',
        '$timeout', 'appEditStateService', 'modalService',
        function ($http, $scope, $routeParams, $interval, $timeout, appEditStateService, modalService) {
            var self = this;
            $scope.initLogMonitorRecipientsFinished = false;
            $scope.initLogMonitorRecipientsFailed = false;
            $scope.logMonitorRecipientsList = logMonitorRecipients;
            $scope.enableHeartbeat = true;
            $scope.globalAppSettings = globalLOAppSettings;

            this.getLogMonitorRecipients = function (callback) {
                appEditStateService.getLogMonitorRecipients(
                    function (data, status) {
                        if (callback) {
                            callback(true);
                        }

                        self.setLogMonitorRecipients(data.recipients);
                    },
                    function (data, status) {
                        if (callback) {
                            callback(false);
                        }
                    }
                );
            };

            this.setLogMonitorRecipients = function (recipients) {
                var recipientsToAdd = [];
                var localHasNonEditableRecipients = self.hasNonEditableRecipients($scope.logMonitorRecipientsList);
                var comingHasEditableRecipients = self.hasEditableRecipients(recipients);

                for (var i = 0; i < recipients.length; i++) {
                    var recipient = recipients[i];

                    if ($scope.logMonitorRecipientsList.length === 0) {
                        recipientsToAdd.push(recipient);
                    } else {
                        if (self.isRecipientEditable(recipient)) {
                            var localRecipient = self.findRecipientById($scope.logMonitorRecipientsList, recipient.id);
                            if (!localRecipient) {
                                recipientsToAdd.push(recipient);
                            } else if (!localRecipient.dirty) {
                                //We have this recipient stored locally and it is not locally modified.
                                //Let's see if any of its info has changed
                                if (recipient.name !== localRecipient.name) {
                                    localRecipient.name = recipient.name;
                                }
                                if (recipient.surname !== localRecipient.surname) {
                                    localRecipient.surname = recipient.surname;
                                }
                                if (recipient.reason !== localRecipient.reason) {
                                    localRecipient.reason = recipient.reason;
                                }
                                if (recipient.telephone !== localRecipient.telephone) {
                                    localRecipient.telephone = recipient.telephone;
                                }
                                if (!recipient.emailAddresses && localRecipient.emailAddresses) {
                                    localRecipient.emailAddresses = null;
                                } else if (!localRecipient.emailAddresses && recipient.emailAddresses) {
                                    localRecipient.emailAddresses = recipient.emailAddresses;
                                } else {
                                    var emailsToAdd = self.getArraysDifference(recipient.emailAddresses, localRecipient.emailAddresses);
                                    var emailsToRemove = self.getArraysDifference(localRecipient.emailAddresses, recipient.emailAddresses);

                                    //Remove emails
                                    for (var k = 0; k < emailsToRemove.length; k++) {
                                        var emailToRemove = emailsToRemove[k];
                                        for (var j = 0; j < localRecipient.emailAddresses.length; j++) {
                                            var localRecEmail = localRecipient.emailAddresses[j];
                                            if (localRecEmail === emailToRemove) {
                                                localRecipient.emailAddresses.splice(j, 1);
                                            }
                                        }
                                    }

                                    //Add emails
                                    if (emailsToAdd.length > 0) {
                                        localRecipient.emailAddresses.push.apply(localRecipient.emailAddresses, emailsToAdd);
                                    }
                                }
                            }
                        } else if (!localHasNonEditableRecipients && !comingHasEditableRecipients) {
                            /*
                             * Unmodifiable recipients are taken from the app
                             * context file and should not be changed during runtime.
                             * So, if we have already any locally stored unmodifiable recipients,
                             * don't bother comparing local one(s) with what the server sends to us
                             * anymore. Furthermore, if the server sends us some modifiable recipients,
                             * we should remove any locally stored unmodifiable ones.
                             */
                            recipientsToAdd.push(recipient);
                        }
                    }
                }

                //Remove removed recipients
                for (var i = 0; i < $scope.logMonitorRecipientsList.length;) {
                    var localRecipient = $scope.logMonitorRecipientsList[i];
                    if (self.isRecipientEditable(localRecipient)) {
                        //don't remove locally added only
                        if (localRecipient.id !== -1) {
                            var recipient = self.findRecipientById(recipients, localRecipient.id);
                            if (!recipient) {
                                $scope.logMonitorRecipientsList.splice(i, 1);
                                continue;
                            }
                        }
                    } else if (comingHasEditableRecipients) {
                        $scope.logMonitorRecipientsList.splice(i, 1);
                        continue;
                    }
                    i++;
                }

                //Add added recipients
                if (recipientsToAdd.length > 0) {
                    $scope.logMonitorRecipientsList.push.apply($scope.logMonitorRecipientsList, recipientsToAdd);
                }
            };

            this.getArraysDifference = function (arr1, arr2) {
                var result = [];
                var k = 0;
                for (var i = 0; i < arr1.length; i++) {
                    var email1 = arr1[i];
                    for (var j = 0; j < arr2.length; j++) {
                        var email2 = arr2[j];
                        if (email2 === email1) {
                            break;
                        }
                        if (j === arr2.length - 1) {
                            result[k++] = email1;
                        }
                    }
                }
                return result;
            };

            this.findRecipientById = function (recipientsArray, id) {
                for (var i = 0; i < recipientsArray.length; i++) {
                    var recipient = recipientsArray[i];
                    if (id === recipient.id) {
                        return recipient;
                    }
                }
                return null;
            };

            this.addNewRecipient = function () {
                var newRecipient = { id : -1 };
                if (!$scope.logMonitorRecipientsList) {
                    $scope.logMonitorRecipientsList = [ newRecipient ];
                } else {
                    $scope.logMonitorRecipientsList.push(newRecipient);
                }
            };

            this.toggleRecipientNameMode = function (recipient, editable) {
                if (!self.isRecipientEditable(recipient)) {
                    return;
                }
                if ((typeof editable !== 'undefined') && editable !== null) {
                    recipient.isNameEditable = editable;
                } else {
                    recipient.isNameEditable = !recipient.isNameEditable;
                }
                if (recipient.isNameEditable) {
                    self.toggleRecipientEmailsMode(recipient, false);
                }
            };

            this.toggleRecipientSurnameMode = function (recipient, editable) {
                if (!self.isRecipientEditable(recipient)) {
                    return;
                }
                if ((typeof editable !== 'undefined') && editable !== null) {
                    recipient.isSurnameEditable = editable;
                } else {
                    recipient.isSurnameEditable = !recipient.isSurnameEditable;
                }
                if (recipient.isSurnameEditable) {
                    self.toggleRecipientEmailsMode(recipient, false);
                }
            };

            this.toggleRecipientTelephoneMode = function (recipient, editable) {
                if (!self.isRecipientEditable(recipient)) {
                    return;
                }
                if ((typeof editable !== 'undefined') && editable !== null) {
                    recipient.isTelephoneEditable = editable;
                } else {
                    recipient.isTelephoneEditable = !recipient.isTelephoneEditable;
                }
                if (recipient.isTelephoneEditable) {
                    self.toggleRecipientEmailsMode(recipient, false);
                }
            };

            this.toggleRecipientReasonMode = function (recipient, editable) {
                if (!self.isRecipientEditable(recipient)) {
                    return;
                }
                if ((typeof editable !== 'undefined') && editable !== null) {
                    recipient.isReasonEditable = editable;
                } else {
                    recipient.isReasonEditable = !recipient.isReasonEditable;
                }
                if (recipient.isReasonEditable) {
                    self.toggleRecipientEmailsMode(recipient, false);
                }
            };

            this.toggleRecipientEmailsMode = function (recipient, editable) {
                if (!self.isRecipientEditable(recipient)) {
                    return;
                }
                if ((typeof editable !== 'undefined') && editable !== null) {
                    recipient.areEmailsEditable = editable;
                } else {
                    recipient.areEmailsEditable = !recipient.areEmailsEditable;
                }
            };

            this.saveEmailIfValid = function (recipient, email) {
                if (email && email.trim() !== '') {
                    if (recipient.emailAddresses) {
                        recipient.emailAddresses.push(email);
                    } else {
                        recipient.emailAddresses = [ email ];
                    }
                    recipient.tmpEmail = null;
                    self.setRecipientDirty(recipient);
                }

                self.toggleRecipientEmailsMode(recipient, false);
            };

            this.removeEmail = function (recipient, index, event) {
                event.stopPropagation();
                if (recipient.emailAddresses && index < recipient.emailAddresses.length) {
                    recipient.emailAddresses.splice(index, 1);
                    self.setRecipientDirty(recipient);
                }
            };

            this.isRecipientEditable = function (recipient) {
                return (typeof recipient.id !== 'undefined') && recipient.id !== null;
            };

            this.hasEditableRecipients = function (recipients) {
                for (var i = 0; i < recipients.length; i++) {
                    var recipient = recipients[i];
                    if (self.isRecipientEditable(recipient)) {
                        return true;
                    }
                }
                return false;
            };

            this.hasNonEditableRecipients = function (recipients) {
                for (var i = 0; i < recipients.length; i++) {
                    var recipient = recipients[i];
                    if (!self.isRecipientEditable(recipient)) {
                        return true;
                    }
                }
                return false;
            };

            this.deleteRecipient = function (recipient, index) {
                var modalOptions = {
                    cancelButtonText: 'Cancel',
                    discardAndLeaveButtonText: null,
                    saveAndLeaveButtonText: 'Delete',
                    saveAndStayButtonText: null,
                    headerText: 'Delete Recipient',
                    bodyText: 'Do you really want to delete this recipient?'
                };
                modalService.show(modalOptions).then(function (result) {
                    if (result === modalOptions.saveAndLeaveButtonText) {
                        if (recipient.id === -1) {
                            $scope.logMonitorRecipientsList.splice(index, 1);
                        } else {
                            appEditStateService.deleteLogMonitorRecipientById(recipient.id,
                                function (data, status) {
                                    $scope.logMonitorRecipientsList.splice(index, 1);
                                },
                                function (data, status) {
                                }
                            );
                        }
                    }
                });
            };

            this.updateRecipient = function (recipient) {
                recipient.saveInProgress = true;
                appEditStateService.updateLogMonitorRecipient(recipient,
                    function (data, status) {
                        recipient.dirty = false;
                        recipient.saveInProgress = false;
                    },
                    function (data, status) {
                        recipient.saveInProgress = false;
                    }
                );
            };

            this.addRecipient = function (recipient, index) {
                recipient.saveInProgress = true;
                appEditStateService.addLogMonitorRecipient(recipient,
                    function (data, status) {
                        $scope.logMonitorRecipientsList[index] = data.recipient;
                        recipient.saveInProgress = false;
                    },
                    function (data, status) {
                        recipient.saveInProgress = false;
                    }
                );
            };

            this.setRecipientDirty = function (recipient) {
                recipient.dirty = true;
            };

            this.discardChanges = function (recipient) {
                recipient.dirty = false;
            };

            this.removeAllRecipients = function () {
                var modalOptions = {
                    cancelButtonText: 'Cancel',
                    discardAndLeaveButtonText: null,
                    saveAndLeaveButtonText: 'Delete',
                    saveAndStayButtonText: null,
                    headerText: 'Delete All Persisted Recipients',
                    bodyText: 'Do you really want to delete all persisted recipients?'
                };
                modalService.show(modalOptions).then(function (result) {
                    if (result === modalOptions.saveAndLeaveButtonText) {
                        appEditStateService.deleteAllLogMonitorRecipients(
                            function (data, status) {
                                appEditStateService.setAlert('info', 'Removed ' + data.count + ' log monitor recipients', status);

                                for (var i = 0; i < $scope.logMonitorRecipientsList.length;) {
                                    var recipient = $scope.logMonitorRecipientsList[i];
                                    if (self.isRecipientEditable(recipient)) {
                                        $scope.logMonitorRecipientsList.splice(i, 1);
                                    } else {
                                        i++;
                                    }
                                }

                            },
                            function (data, status) {
                            }
                        );
                    }
                });
            };

            this.init = function () {
                self.getLogMonitorRecipients(function(isSuccess) {
                    $scope.initLogMonitorRecipientsFinished = true;
                    $scope.initLogMonitorRecipientsFailed = !isSuccess;
                });

                self.heartBeatFunction = $interval(
                    function () {
                        self.getLogMonitorRecipients();
                    },
                    10000 /* once per 10 sec */
                );
            };

            this.destroyHeartBeatFunction = function () {
                if (self.heartBeatFunction) {
                    $interval.cancel(self.heartBeatFunction);
                    self.heartBeatFunction = null;
                }
            };

            this.saveHideOptDashbIconsFlag = function () {
                localStorage.loadOrchestratorSettings = angular.toJson($scope.globalAppSettings);
            };

            $scope.$on('$destroy', function () {
                self.destroyHeartBeatFunction();
            });

            self.init();
        }
    ]
);