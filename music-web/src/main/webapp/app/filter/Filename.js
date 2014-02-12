'use strict';

/**
 * Extract filename from a fullpath.
 */
App.filter('filename', function() {
  return function(fullPath) {
    return fullPath.replace(/^.*[\\\/]/, '');
  }
})