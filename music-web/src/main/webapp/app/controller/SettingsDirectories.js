'use strict';

/**
 * Directories settings controller.
 */
App.controller('SettingsDirectories', function($scope, Restangular, $dialog) {
  // Initialize add directory form
  $scope.directory = {
    name: '', location: ''
  };
  
  // Load directories
  $scope.loadDirectories = function() {
    Restangular.one('directory').getList().then(function(data) {
      $scope.directories = data.directories;
    });
  };
  $scope.loadDirectories();

  // Remove a directory
  $scope.deleteDirectory = function(directory) {
    var title = 'Delete directory';
    var msg = 'Do you really want to delete directory ' + directory.name + '?';
    var btns = [
      { result: 'cancel', label: 'Cancel' },
      { result: 'ok', label: 'OK', cssClass: 'btn-primary' }
    ];

    $dialog.messageBox(title, msg, btns, function (result) {
      if (result == 'ok') {
        Restangular.one('directory', directory.id).remove().then(function () {
          $scope.loadDirectories();
        });
      }
    });
  };
  
  // Add a directory
  $scope.addDirectory = function() {
    Restangular.one('directory').put({
      name: $scope.directory.name,
      location: $scope.directory.location
    })
        .then(function() {
          // Reset add directory form
          $scope.directory = {
            name: '', location: ''
          };

          // Reload directories
          $scope.loadDirectories();
        });
  };
});