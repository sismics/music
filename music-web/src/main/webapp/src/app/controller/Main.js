'use strict';

/**
 * Main controller.
 */
angular.module('music').controller('Main', function($rootScope, $state, $scope, Playlist, Album) {
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

  // Clear the albums cache if the previous state is not main.album
  $scope.$on('$stateChangeStart', function (e, to, toParams, from) {
    if (to.name == 'main.music.albums' && from.name != 'main.album') {
      Album.clearCache();
    }
  });
});