'use strict';

/**
 * Audio player service.
 */
App.factory('AudioPlayer', function($rootScope) {
  var tracks = [];

  return {
    play: function(_tracks) {
      tracks = _tracks;
      $rootScope.$broadcast('audio.set');
    },

    prev: function() {
      $rootScope.$broadcast('audio.set');
    },

    next: function() {
      $rootScope.$broadcast('audio.set');
    },

    currentTrack: function() {
      return tracks[0];
    }
  }
});