'use strict';

/**
 * Playlist dropdown directive.
 */
angular.module('music').directive('playlistDropdown', function() {
  return {
    restrict: 'E',
    templateUrl: 'partial/playlistdropdown.directive.html',
    replace: true,
    scope: {
      tracks: '='
    },
    controller: function($scope, NamedPlaylist) {
      // Create a new named playlist
      $scope.createPlaylist = function(tracks) {
        NamedPlaylist.createPlaylist(tracks);
      };

      // Add to playlist
      $scope.addToPlaylist = function(playlist, tracks) {
        NamedPlaylist.addToPlaylist(playlist, tracks);
      };
    }
  }
});