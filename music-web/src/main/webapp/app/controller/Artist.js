'use strict';

/**
 * Artist controller.
 */
App.controller('Artist', function($scope, $stateParams, Restangular) {
  // Get artist details
  Restangular.one('artist', $stateParams.id).get().then(function(data) {
    $scope.artist = data;
  });

  // Get artists' albums
  Restangular.all('album').getList({ artist: $stateParams.id }).then(function(data) {
    $scope.albums = data.albums;
  });
});