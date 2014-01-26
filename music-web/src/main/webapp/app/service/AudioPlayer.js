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

    currentTrack: function() {
      return tracks[0];
    }
  }
});