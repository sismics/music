'use strict';

/**
 * Audio player service.
 */
angular.module('music').factory('Playlist', function($rootScope, Restangular, toaster) {
  var currentTrack = null;
  var currentStatus = 'stopped';
  var tracks = [];
  var repeat = true;
  var shuffle = false;

  // Read Local Storage settings
  if (!_.isUndefined(localStorage.playlistRepeat)) {
    repeat = localStorage.playlistRepeat == 'true';
  }
  if (!_.isUndefined(localStorage.playlistShuffle)) {
    shuffle = localStorage.playlistShuffle == 'true';
  }

  // Maintain updated status
  $rootScope.$on('audio.play', function() {
    currentStatus = 'playing';
  });
  $rootScope.$on('audio.pause', function() {
    currentStatus = 'paused';
  });
  $rootScope.$on('audio.ended', function() {
    currentStatus = 'stopped';
  });

  // Update playlist on track.liked
  $rootScope.$on('track.liked', function(e, trackId, liked) {
    _.each(_.where(tracks, { id: trackId }), function(track) {
      track.liked = liked;
    });

    $rootScope.$broadcast('playlist.updated', angular.copy(tracks));
  });

  // Service
  var service = {
    /**
     * Reset the service.
     */
    reset: function() {
      $rootScope.$broadcast('audio.stop');
      currentTrack = null;
      tracks = [];
    },

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
     * Move a track in the playlist.
     * @param order
     * @param neworder
     */
    moveTrack: function(order, neworder) {
      if (currentTrack != null) {
        if (order < currentTrack && neworder >= currentTrack) {
          currentTrack--;
        } else if (order > currentTrack && neworder <= currentTrack) {
          currentTrack++;
        } else if (order == currentTrack) {
          currentTrack = neworder;
        }
      }

      Restangular.one('playlist', order).post('move', {
        neworder: neworder
      }).then(function() {
        service.update();
      });
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
      var size = _.size(tracks);
      if (size == 0) {
        return;
      }
      if (shuffle) {
        currentTrack = _.random(0, size - 1);
        $rootScope.$broadcast('audio.set', true);
        return;
      }
      if (size > currentTrack + 1) {
        currentTrack++;
        $rootScope.$broadcast('audio.set', true);
      } else if (repeat) {
        currentTrack = 0;
        $rootScope.$broadcast('audio.set', true);
      }
    },

    /**
     * Add a track to the playlist.
     * @param track
     * @param play If true, immediately play the first track once added
     * @param name Track name
     */
    add: function(track, play) {
      Restangular.one('playlist').put({
        id: track.id,
        order: null
      }).then(function() {
            var promise = service.update();
            if (play) {
              promise.then(function() {
                service.play(0);
              })
            } else {
              toaster.pop('success', 'Track added', track.title);
            }
          });
    },

    /**
     * Add a list of tracks to the playlist.
     * @param trackIdList
     * @param play If true, immediately play the first track once added
     */
    addAll: function(trackIdList, play) {
      Restangular.one('playlist/multiple').put({
        ids: trackIdList
      }).then(function() {
            var promise = service.update();
            if (play) {
              promise.then(function() {
                if (_.size(tracks) > 0) {
                  service.play(0);
                }
              })
            } else {
              toaster.pop('success', 'Now playing', 'All tracks added');
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
     * Remove all tracks from the playlist.
     * @param update Update playlist if true
     * @returns {*}
     */
    clear: function(update) {
      // Stop the audio
      currentTrack = null;
      $rootScope.$broadcast('audio.stop');

      var promise = Restangular.one('playlist').remove();
      if (update) {
        service.update();
      }
      return promise;
    },

    /**
     * Remove all tracks from the playlist and play a new one.
     * @param track
     */
    removeAndPlay: function(track) {
      service.clear(false).then(function() {
        service.add(track, true);
      });
    },

    /**
     * Remove all tracks from the playlist and play a list of new ones.
     * @param trackIdList
     */
    removeAndPlayAll: function(trackIdList) {
      // Stop the audio
      currentTrack = null;
      $rootScope.$broadcast('audio.stop');

      Restangular.one('playlist').remove().then(function() {
        service.addAll(trackIdList, true);
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
      return angular.copy(tracks[currentTrack]);
    },

    /**
     * Like/unlike a track by ID.
     * @param trackId
     * @param liked
     */
    likeById: function(trackId, liked) {
      var promise = null;
      if (liked) {
        promise = Restangular.one('track', trackId).post('like');
      } else {
        promise = Restangular.one('track/' + trackId + '/like', null).remove();
      }

      promise.then(function() {
        // Broadcast track.liked
        $rootScope.$broadcast('track.liked', trackId, liked);
      });
    },

    currentStatus: function() { return currentStatus; },
    currentOrder: function() { return currentTrack; },
    getTracks: function() { return angular.copy(tracks); },
    isRepeat: function() { return repeat; },
    toggleRepeat: function() { repeat = !repeat; localStorage.playlistRepeat = repeat; },
    isShuffle: function() { return shuffle; },
    toggleShuffle: function() { shuffle = !shuffle; localStorage.playlistShuffle = shuffle; }
  };

  return service;
});