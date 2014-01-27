'use strict';

/**
 * Album controller.
 */
App.controller('Album', function($scope, $stateParams, Restangular, Playlist) {
  // Load albums
  Restangular.one('album', $stateParams.id).get().then(function(data) {
    $scope.album = data;
  })

  // Play a single track
  $scope.playTrack = function(track) {
    Playlist.removeAndPlay(track.id);
  };

  // Add a single track to the playlist
  $scope.addTrack = function(track) {
    Playlist.add(track.id);
  }
});