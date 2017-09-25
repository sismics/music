'use strict';

/**
 * File upload directive.
 */
angular.module('music').directive('file', function() {
  return {
    restrict: 'E',
    template: '<input type="file" />',
    replace: true,
    require: 'ngModel',
    link: function(scope, element, attrs, ctrl) {
      element.bind('change', function() {
        scope.$apply(function() {
          attrs.multiple ?
            ctrl.$setViewValue(element[0].files)
            : ctrl.$setViewValue(element[0].files[0]);
        });
      });

      scope.$watch(attrs.ngModel, function(newValue) {
        // Only clear is authorized
        if (_.size(newValue) == 0) {
          element[0].value = '';
        }
      });
    }
  }
});