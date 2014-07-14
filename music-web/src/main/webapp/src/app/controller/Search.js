'use strict';

/**
 * Search controller.
 */
angular.module('music').controller('Search', function($rootScope, $scope, $stateParams, Restangular, Playlist) {
  // Server call debounced
  var search = _.debounce(function(query) {
    Restangular.one('search', query).get({ limit: 100 }).then(function(data) {
      $scope.results = data;
    });
  }, 300);

  // Watch the search query
  $scope.$watch('$stateParams.query', function(newval) {
    search(newval);
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
    var track = _.findWhere($scope.results.tracks, { id: trackId });
    if (track) {
      track.liked = liked;
    }
  });
});