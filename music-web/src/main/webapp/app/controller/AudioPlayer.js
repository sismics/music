'use strict';

/**
 * Audio player controller.
 */
App.controller('AudioPlayer', function($scope, $state, AudioPlayer, $timeout) {
  $timeout(function() {
    AudioPlayer.play([
      {
        url: 'AntagonistA.mp3',
        title: 'Antagonist A',
        artist: 'Kevin MacLeod'
      }
    ]);
  }, 100);

  $scope.openNowPlaying = function () {
    $state.transitionTo('main.playing');
  }
});