'use strict';

/**
 * Directories settings controller.
 */
angular.module('music').controller('SettingsDirectories', function($scope, Restangular, $dialog, toaster) {
  // Initialize add directory form
  $scope.directory = {
    location: ''
  };
  
  // Load directories
  $scope.loadDirectories = function() {
    Restangular.one('directory').get().then(function(data) {
      $scope.directories = data.directories;
    });
  };
  $scope.loadDirectories();

  // Remove a directory
  $scope.deleteDirectory = function(directory) {
    var title = 'Delete directory';
    var msg = 'Do you really want to delete directory "' + directory.location + '"?';
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
      location: $scope.directory.location
    })
        .then(function() {
          // Reset add directory form
          $scope.editForm.submitted = false;
          $scope.directory = {
            location: ''
          };

          // Reload directories
          $scope.loadDirectories();
        });
  };

  // Rescan all directories
  $scope.rescan = function() {
    Restangular.one('app/batch').post('reindex').then(function() {
      toaster.pop('info', 'Directories', 'Rescan in progress...');
    });
  };
});