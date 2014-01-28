'use strict';

/**
 * Audio player service.
 */
App.factory('Playlist', function($rootScope, Restangular, $timeout) {
  var currentTrack = null;
  var tracks = [];

  var service = {
    /**
     * Play a track from the current playlist.
     * @param _currentTrack
     */
    play: function(_currentTrack) {
      if (_.size(tracks) > _currentTrack) {
        currentTrack = _currentTrack;
        $rootScope.$broadcast('audio.set', true);
      }
    },
    
    /**
     * Update the playlist.
     */
    update: function() {
      var promise = Restangular.one('playlist').getList();
      promise.then(function(data) {
        tracks = data.tracks;
        $rootScope.$broadcast('playlist.updated', tracks);
      });
      return promise;
    },

    /**
     * Open a track without playing it.
     * @param _currentTrack
     */
    open: function(_currentTrack) {
      if (_.size(tracks) > _currentTrack) {
        currentTrack = _currentTrack;
        $rootScope.$broadcast('audio.set', false);
      }
    },

    /**
     * Play the previous track.
     */
    prev: function() {
      if (currentTrack > 0) {
        currentTrack--;
        $rootScope.$broadcast('audio.set', true);
      }
    },

    /**
     * Play the next track.
     */
    next: function() {
      if (_.size(tracks) > currentTrack + 1) {
        currentTrack++;
        $rootScope.$broadcast('audio.set', true);
      } else if (_.size(tracks) > 0) {
        currentTrack = 0;
        $rootScope.$broadcast('audio.set', true);
      }
    },

    /**
     * Add a track to the playlist.
     * @param trackId
     * @param play If true, immediately play the first track once added
     */
    add: function(trackId, play) {
      Restangular.one('playlist').put({
        id: trackId,
        order: null
      }).then(function() {
            var promise = service.update();
            if (play) {
              promise.then(function() {
                service.play(0);
              })
            }
          });
    },

    /**
     * Remove a given track from the playlist.
     * @param order
     */
    remove: function(order) {
      if (currentTrack != null) {
        if (order < currentTrack) {
          currentTrack--;
        }
        if (order == currentTrack) {
          // Stop the audio, we are listening to the removed track
          currentTrack = null;
          $rootScope.$broadcast('audio.stop');
        }
      }

      Restangular.one('playlist', order).remove().then(function() {
        service.update();
      });
    },

    /**
     * Remove all tracks from the playlist and play a new one.
     * @param trackId
     */
    removeAndPlay: function(trackId) {
      // Stop the audio
      currentTrack = null;
      $rootScope.$broadcast('audio.stop');

      Restangular.one('playlist').remove().then(function() {
        service.add(trackId, true);
      });
    },

    /**
     * Returns the current track (or null if none).
     * @returns {*}
     */
    currentTrack: function() {
      if (currentTrack == null) {
        return null;
      }
      return tracks[currentTrack];
    },

    /**
     * Returns the current track index.
     * @returns currentTrack
     */
    currentOrder: function() {
      return currentTrack;
    },

    /**
     * Returns all tracks from the playlist.
     * @returns {Array}
     */
    getTracks: function() {
      return tracks;
    }
  };

  return service;
});