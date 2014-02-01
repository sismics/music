'use strict';

/**
 * Audio player controller.
 */
App.controller('AudioPlayer', function($scope, $state) {
  // Open the current playlist
  $scope.openNowPlaying = function () {
    $state.transitionTo('main.playing');
  };

  // Open an album
  $scope.openAlbum = function(id) {
    $state.transitionTo('main.album', { id: id });
  };
});