'use strict';

/**
 * Player controller.
 */
App.controller('Player', function($scope, $state) {
  $scope.openNowPlaying = function () {
    $state.transitionTo('main.playing');
  }
});