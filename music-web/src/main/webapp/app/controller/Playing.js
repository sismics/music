'use strict';

/**
 * Now playing controller.
 */
App.controller('Playing', function($scope, Playlist) {
  // Grab current playlist, and listen to future changes
  $scope.tracks = Playlist.getTracks();
  $scope.$on('playlist.updated', function(e, tracks) {
    $scope.tracks = tracks;
    $scope.currentOrder = Playlist.currentOrder();
  });

  // Grab current track, and listen to future changes
  $scope.currentOrder = Playlist.currentOrder();
  $scope.$on('audio.set', function() {
    $scope.currentOrder = Playlist.currentOrder();
  });
  $scope.$on('audio.stop', function() {
    $scope.currentOrder = Playlist.currentOrder();
  });

  // Remove a track from the playlist
  $scope.removeTrack = function(order) {
    Playlist.remove(order);
  };

  // Play a specific track from the playlist
  $scope.playTrack = function(order) {
    Playlist.play(order);
  };
});