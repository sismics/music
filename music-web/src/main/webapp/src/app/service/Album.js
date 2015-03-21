'use strict';

/**
 * Album service.
 */
angular.module('music').factory('Album', function() {
  var cache = [];
  var scroll = 0;
  var height = 0;
  
  return {
    setCache: function(albums) {
      cache = albums;
    },
    
    getCache: function() {
      return cache;
    },

    clearCache: function() {
      cache = [];
      scroll = 0;
      height = 0;
    },

    setScrollPosition: function(_scroll, _height) {
      scroll = _scroll;
      height = _height;
    },

    getScrollPosition: function() {
      return {
        scroll: scroll,
        height: height
      };
    }
  }
});