'use strict';

describe('fldDashboardApp.version module', function() {
  beforeEach(module('fldDashboardApp.version'));

  describe('version service', function() {
    it('should return current version', inject(function(version) {
      expect(version).toEqual('0.1');
    }));
  });
});
