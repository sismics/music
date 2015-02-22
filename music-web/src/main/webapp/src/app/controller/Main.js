'use strict';

/**
 * Main controller.
 */
angular.module('music').controller('Main', function($rootScope, $state, $scope, Playlist) {
  $scope.partyMode = Playlist.isPartyMode();

  // Keep party mode in sync
  $rootScope.$on('playlist.party', function(e, partyMode) {
    $scope.partyMode = partyMode;
  });

  // Start party mode
  $scope.startPartyMode = function() {
    Playlist.party(true, true);
    $state.transitionTo('main.playing');
  };

  // Stop party mode
  $scope.stopPartyMode = function() {
    Playlist.setPartyMode(false);
  };
});