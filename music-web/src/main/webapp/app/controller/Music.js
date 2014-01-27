'use strict';

/**
 * Music library controller.
 */
App.controller('Music', function($scope, Restangular) {
  // Load all albums
  Restangular.all('album').getList().then(function(data) {
    $scope.albums = data.albums;
  })
});