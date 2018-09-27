'use strict';

angular.module('fldDashboardApp.version', [
  'fldDashboardApp.version.interpolate-filter',
  'fldDashboardApp.version.version-directive'
])

.value('version', '0.1');
