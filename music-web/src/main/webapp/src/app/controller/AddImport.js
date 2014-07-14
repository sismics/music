'use strict';

/**
 * Add imported music controller.
 */
angular.module('music').controller('AddImport', function($scope, Restangular, $dialog) {
  // Refresh imported files
  $scope.refresh = function() {
    Restangular.one('import').getList().then(function(data) {
      $scope.files = data.files;

      // Guess artist and title
      _.each($scope.files, function(file) {
        var sep = file.file.indexOf(' - ');
        if (sep > -1) {
          file.artist = file.file.substring(0, sep);
          file.album_artist = file.artist;
          file.title = file.file.substring(sep + 3, file.file.lastIndexOf('.'));
        } else {
          file.title = file.file.substring(0, file.file.lastIndexOf('.'));
        }
      });
    });
  };

  // Move an imported file to the collection
  $scope.moveFile = function(file) {
    Restangular.one('import').post('', file).then(function() {
      $scope.files.splice($scope.files.indexOf(file), 1);
    }, function(data) {
      $dialog.messageBox('Import error', data.data.message, [
        { result: 'ok', label: 'OK', cssClass: 'btn-primary' }
      ]);
    });
  };

  // Delete a file
  $scope.deleteFile = function(file) {
    Restangular.one('import').remove({ file: file.file }).then(function() {
      $scope.files.splice($scope.files.indexOf(file), 1);
    }, function() {
      $dialog.messageBox('Delete error', 'Error deleting the file', [
        { result: 'ok', label: 'OK', cssClass: 'btn-primary' }
      ]);
    });
  };

  $scope.refresh();
});