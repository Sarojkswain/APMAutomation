'use strict';

angular.module('fldDashboardApp.pictoGalleryDirective', [])
    .directive('pictoGallery', [function ($scope) {
        return {
            restrict: 'E',
            templateUrl: 'components/pictoGallery/pictoGalleryView.html'
        };
    }])
    .controller('PictoGalleryController', function ($scope) {
        var self = this;
        this.currentIconName = $scope.dashboard ? $scope.dashboard.iconName : null;

        $scope.iconsArray = [
            [ 'accessibility', 'adplanner', 'binoculars', 'chrome', 'internetExplorer', 'folderDownload', 'fireAlarm', 'nuclearEngineer', 'clean' ],
            [ 'health', 'heart', 'info', 'lineChart', 'pieChart', 'doctor', 'robot', 'atom', 'clock' ],
            [ 'playRed', 'applix', 'compassMap', 'computer', 'packageGraphics', 'drill', 'videoFilm', 'dataTransport', 'diskette' ],
            [ 'plan', 'radar', 'sessionManager', 'calculator', 'preferencesSystemPowerManagement', 'laboratoryRed', 'filmCamera35mm', 'drawingPen', 'fileRoller' ],
            [ 'preferencesSystemTime', 'script', 'serviceManager', 'preferencesSystem', 'applicationExcel', 'microscope', 'checkeredFlag', 'graduated', 'gnomeSessionHalt' ],
            [ 'ibmBlack', 'webSphereLogo', 'jboss', 'oracleLogoRed', 'sharePoint' , 'redhat', 'columnChart', 'guard', 'printer' ],
            [ 'android', 'xmag', 'androidShadow', 'appleShadow', 'linuxPenguin', 'windows', 'windows8Shadow', 'skull', 'trash' ],
            [ 'emailShadow', 'html5Shadow', 'rssShadow', 'javaPlatform', 'serverLightning', 'schoolBus', 'physicalEducation', 'multifunctionPrinter', 'exit' ],
            [ 'beaLogo', 'crtMonitor', 'utilitiesSystemMonitor', 'gnomeSystemMonitor', 'monitor', 'gnomeSystemMonitorII', 'computerII', 'gnomeMonitor', 'adeptInstaller' ]
        ];

        this.getCurrentIconName = function () {
            var selected = self.currentIconName;
            if ($scope.dashboard && $scope.dashboard.iconName) {
                self.currentIconName = $scope.dashboard.iconName;
                selected = self.currentIconName;
            } else if (!self.currentIconName) {
                selected = 'questionFile';
            }
            return selected;
        };

        this.onSelect = function (iconName) {
            self.currentIconName = iconName;
            $scope.onIconUpdate(iconName);
        };

        this.getCurrentMediumIconFileName = function () {
            return self.getCurrentIconName() + '48x48.png';
        };

    });
