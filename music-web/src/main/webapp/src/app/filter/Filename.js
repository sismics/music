'use strict';

/**
 * Extract filename from a fullpath.
 */
angular.module('music').filter('filename', function() {
  return function(fullPath) {
    return fullPath.replace(/^.*[\\\/]/, '');
  }
})