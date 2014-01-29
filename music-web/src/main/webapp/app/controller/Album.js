'use strict';

/**
 * Album controller.
 */
App.controller('Album', function($scope, $stateParams, Restangular, Playlist) {
  $scope.album = {
    tracks: []
  };

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
    Playlist.add(track.id, false);
  };

  // Add all tracks to the playlist in a random order
  $scope.shuffleAllTracks = function() {
    Playlist.addAll(_.shuffle(_.pluck($scope.album.tracks, 'id')), false);
  };

  // Play all tracks
  $scope.playAllTracks = function() {
    Playlist.removeAndPlayAll(_.pluck($scope.album.tracks, 'id'));
  };

  // Add all tracks to the playlist
  $scope.addAllTracks = function() {
    Playlist.addAll(_.pluck($scope.album.tracks, 'id'), false);
  };
});