'use strict';

/**
 * Audio player controller.
 */
angular.module('music').controller('AudioPlayer', function($scope, $state, Playlist) {
  // Open the current playlist
  $scope.openNowPlaying = function () {
    $state.transitionTo('main.playing');
  };

  // Open an album
  $scope.openAlbum = function(id) {
    $state.transitionTo('main.album', { id: id });
  };

  // Like/unlike a track
  $scope.toggleLikeTrack = function(track) {
    Playlist.likeById(track.id, !track.liked);
  };
});