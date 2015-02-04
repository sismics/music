'use strict';

/**
 * Settings check controller.
 */
angular.module('music').controller('SettingsCheck', function($scope, User, Restangular) {
  // Admin password check
  User.userInfo().then(function(data) {
    $scope.userInfo = data;
  });

  // Directories check
  Restangular.one('directory').get().then(function(data) {
    $scope.directories = data.directories;
    $scope.directoriesValid = true;
    for (var i = 0; i < data.directories.length; i++) {
      if (!data.directories[i].writable || !data.directories[i].valid) {
        $scope.directoriesValid = false;
        break;
      }
    }
  });

  // Dependencies check
  Restangular.one('import/dependencies').get().then(function(data) {
    $scope.dependencies = data;
  });
});