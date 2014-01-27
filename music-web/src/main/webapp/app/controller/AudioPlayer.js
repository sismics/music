'use strict';

/**
 * Audio player controller.
 */
App.controller('AudioPlayer', function($scope, $state, $timeout) {
  $scope.openNowPlaying = function () {
    $state.transitionTo('main.playing');
  }
});