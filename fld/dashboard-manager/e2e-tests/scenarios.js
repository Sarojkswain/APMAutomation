'use strict';

/* https://github.com/angular/protractor/blob/master/docs/toc.md */

describe('my app', function() {

  browser.get('index.html');

  it('should automatically redirect to /homeView when location hash/fragment is empty', function() {
    expect(browser.getLocationAbsUrl()).toMatch("/homeView");
  });


  describe('view1', function() {

    beforeEach(function() {
      browser.get('index.html#/homeView');
    });


    it('should render homeView when user navigates to /homeView', function() {
      expect(element.all(by.css('[ng-view] p')).first().getText()).
        toMatch(/partial for view 1/);
    });

  });


  describe('view2', function() {

    beforeEach(function() {
      browser.get('index.html#/dashboardView');
    });


    it('should render dashboardView when user navigates to /dashboardView', function() {
      expect(element.all(by.css('[ng-view] p')).first().getText()).
        toMatch(/partial for view 2/);
    });

  });
});
