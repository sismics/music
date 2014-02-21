'use strict';

/**
 * Filter formatting a duration.
 */
angular.module('music').filter('duration', function() {
  return function(n) {
    var min = Math.floor(n / 60);
    var sec = Math.round(n - min * 60);
    return min + ':' + (sec < 10 ? '0' : '') + sec;
  }
})