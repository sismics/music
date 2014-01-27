'use strict';

/**
 * Settings controller.
 */
App.controller('Settings', function($scope, Restangular) {
  // Initialize add directory form
  $scope.directory = {
    name: '', location: ''
  }

  // Add a directory
  $scope.addDirectory = function() {
    Restangular.one('directory').put({
      name: $scope.directory.name,
      location: $scope.directory.location
    });
  };
});