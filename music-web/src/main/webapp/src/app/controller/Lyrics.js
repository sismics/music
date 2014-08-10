'use strict';

/**
 * Lyrics controller.
 */
angular.module('music').controller('Lyrics', function($scope, Playlist, Restangular) {
  $scope.lyrics = [];
  $scope.error = false;

  var updateScope = function() {
    $scope.track = Playlist.currentTrack();
  };

  // Listen to track changes
  updateScope();
  $scope.$on('audio.set', updateScope);

  // Load lyrics on track change
  $scope.$watch('track.id', function(id) {
    $scope.lyrics = [];
    if (!id) {
      return;
    }

    $scope.error = false;
    Restangular.one('track', id).one('lyrics').get().then(function(data) {
      $scope.lyrics = data.lyrics;
    }, function() {
      $scope.lyrics = [];
      $scope.error = true;
    });
  });
});