'use strict';

/**
 * Main controller.
 */
angular.module('music').controller('Main', function($state, $scope, Playlist) {
  $scope.startPartyMode = function() {
    Playlist.party(true, true);
    $state.transitionTo('main.playing');
  };
});