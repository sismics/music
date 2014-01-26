'use strict';

/**
 * Filter rounding input number.
 */
App.filter('round', function() {
  return function(n) {
    return Math.round(n);
  }
})