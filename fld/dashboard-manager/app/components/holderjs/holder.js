'use strict';

angular.module('fldDashboardApp.holderDirective', [])
    .directive('holderJs', function () {
        return {
            link: function (scope, element, attrs) {
                attrs.$set('data-src', attrs.holderJs);
                Holder.run({ images: element[0], renderer : 'html' });
            }
        };
    });