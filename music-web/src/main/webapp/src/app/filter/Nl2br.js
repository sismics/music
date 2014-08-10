'use strict';

/**
 * Transform line break in <br />.
 */
angular.module('music').filter('nl2br', function() {
  return function(s) {
    return s.replace(/(?:\r\n|\r|\n)/g, '<br />');
  }
})