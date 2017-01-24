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
  var visualization = true;
  var partyMode = false;

  // Read Local Storage settings
  if (!_.isUndefined(localStorage.playlistRepeat)) {
    repeat = localStorage.playlistRepeat == 'true';
  }
  if (!_.isUndefined(localStorage.playlistShuffle)) {
    shuffle = localStorage.playlistShuffle == 'true';
  }
  if (!_.isUndefined(localStorage.playlistVisualization)) {
    visualization = localStorage.playlistVisualization == 'true';
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

  // Fill the playlist with new tracks when in party mode
  $rootScope.$on('audio.set', function() {
    if (partyMode && currentTrack >= _.size(tracks) - 2) {
      service.party(false, false);
    }
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
      service.setPartyMode(false);
    },

    /**
     * Play a track from the current playlist.
     * @param _currentTrack Track to play
     */
    play: function(_currentTrack) {
      if (_.size(tracks) > _currentTrack) {
        currentTrack = _currentTrack;
        $rootScope.$broadcast('audio.set', true);
      }
    },

    /**
     * Move a track in the playlist.
     * @param order Old order
     * @param neworder New order
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

      Restangular.one('playlist/default', order).post('move', {
        neworder: neworder
      }).then(function(data) {
        service.setTracks(data.tracks);
      });
    },

    /**
     * Update the playlist.
     */
    update: function() {
      var promise = Restangular.one('playlist/default').get();
      promise.then(function(data) {
        service.setTracks(data.tracks);
      });
      return promise;
    },

    /**
     * Open a track without playing it.
     * @param _currentTrack Track to open
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
     * @param track Track to add
     * @param clear Clear the playlist
     * @param play If true, immediately play the first track once added
     */
    add: function(track, clear, play) {
      service.setPartyMode(false);

      Restangular.one('playlist/default').put({
        id: track.id,
        clear: clear,
        order: null
      }).then(function(data) {
            service.setTracks(data.tracks);

            if (play) {
              service.play(0);
            } else {
              toaster.pop('success', 'Track added', track.title);
            }
          });
    },

    /**
     * Add a list of tracks to the playlist.
     * @param trackIdList List of track IDs
     * @param clear Clear the playlist
     * @param play If true, immediately play the first track once added
     */
    addAll: function(trackIdList, clear, play) {
      service.setPartyMode(false);

      Restangular.one('playlist/default/multiple').put({
        ids: trackIdList,
        clear: clear
      }).then(function(data) {
            service.setTracks(data.tracks);

            if (play) {
              if (_.size(tracks) > 0) {
                service.play(0);
              }
            } else {
              toaster.pop('success', 'Now playing', 'All tracks added');
            }
          });
    },

    /**
     * Start or continue party mode.
     * @param clear Clear the playlist
     * @param play If true, immediately play the first track once added
     */
    party: function(clear, play) {
      Restangular.one('playlist/default').post('party', {
        clear: clear
      }).then(function(data) {
            service.setTracks(data.tracks);
            service.setPartyMode(_.size(tracks) > 0);

            if (play && _.size(tracks) > 0) {
              service.play(0);
              toaster.pop('success', 'Party mode', 'Let\'s get the party started!');
            }
          });
    },

    /**
     * Remove a given track from the playlist.
     * @param order Order to remove
     */
    remove: function(order) {
      if (currentTrack != null) {
        if (order < currentTrack) {
          currentTrack--;
        } else if (order == currentTrack) {
          // Stop the audio, we are listening to the removed track
          currentTrack = null;
          $rootScope.$broadcast('audio.stop');
        }
      }

      Restangular.one('playlist/default', order).remove().then(function(data) {
        service.setTracks(data.tracks);
      });
    },

    /**
     * Remove all tracks from the playlist.
     * @returns Promise
     */
    clear: function() {
      // Stop the audio
      currentTrack = null;
      service.setPartyMode(false);
      $rootScope.$broadcast('audio.stop');

      return Restangular.one('playlist/default').post('clear', {}).then(function() {
        service.setTracks([]);
      });
    },

    /**
     * Remove all tracks from the playlist and play a new one.
     * @param track
     */
    removeAndPlay: function(track) {
      service.add(track, true, true);
    },

    /**
     * Remove all tracks from the playlist and play a list of new ones.
     * @param trackIdList
     */
    removeAndPlayAll: function(trackIdList) {
      // Stop the audio
      currentTrack = null;
      $rootScope.$broadcast('audio.stop');

      service.addAll(trackIdList, true, true);
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

    /**
     * Update the tracks list.
     * @param _tracks
     */
    setTracks: function(_tracks) {
      tracks = _tracks;
      $rootScope.$broadcast('playlist.updated', tracks);
    },

    currentStatus: function() { return currentStatus; },
    currentOrder: function() { return currentTrack; },
    getTracks: function() { return angular.copy(tracks); },
    isRepeat: function() { return repeat; },
    toggleRepeat: function() {
      repeat = !repeat;
      localStorage.playlistRepeat = repeat;
    },
    isShuffle: function() {return shuffle; },
    toggleShuffle: function() {
      shuffle = !shuffle;
      localStorage.playlistShuffle = shuffle;
    },
    isVisualization: function() { return visualization; },
    toggleVisualization: function() {
      visualization = !visualization;
      localStorage.playlistVisualization = visualization;
    },
    setPartyMode: function(_partyMode) {
      var prev = partyMode;
      partyMode = _partyMode;
      if (partyMode != prev) {
        $rootScope.$broadcast('playlist.party', partyMode);
      }
    },
    isPartyMode: function() { return partyMode; }
  };

  return service;
});