'use strict';

/**
 * Artist controller.
 */
angular.module('music').controller('Artist', function($scope, $stateParams, Restangular, Playlist) {
  // Get artist details
  Restangular.one('artist', $stateParams.id).get().then(function(data) {
    $scope.artist = data;
  });

  // Play a single track
  $scope.playTrack = function(track) {
    Playlist.removeAndPlay(track);
  };

  // Add a single track to the playlist
  $scope.addTrack = function(track) {
    Playlist.add(track, false);
  };

  // Like/unlike a track
  $scope.toggleLikeTrack = function(track) {
    Playlist.likeById(track.id, !track.liked);
  };

  // Update UI on track liked
  $scope.$on('track.liked', function(e, trackId, liked) {
    var track = _.findWhere($scope.artist.tracks, { id: trackId });
    if (track) {
      track.liked = liked;
    }
  });
});