'use strict';

/**
 * Add imported music controller.
 */
angular.module('music').controller('AddImport', function($scope, Restangular, $dialog) {
  $scope.isLoading = false;

  // Refresh imported files
  $scope.refresh = function() {
    Restangular.one('import').get().then(function(data) {
      $scope.files = data.files;
    });
  };

  // Move all files to the collection
  $scope.moveAllFiles = function() {
    _.each($scope.files, function(file) {
      $scope.moveFile(file);
    });
  };

  // Move an imported file to the collection
  $scope.moveFile = function(file) {
    $scope.isLoading = true;
    Restangular.one('import').post('', file).then(function() {
      $scope.files.splice($scope.files.indexOf(file), 1);
      $scope.isLoading = false;
    }, function(data) {
      $dialog.messageBox('Import error', data.data.message, [
        { result: 'ok', label: 'OK', cssClass: 'btn-primary' }
      ]);
      $scope.isLoading = false;
    });
  };

  // Delete a file
  $scope.deleteFile = function(file) {
    $scope.isLoading = true;
    Restangular.one('import').remove({ file: file.file }).then(function() {
      $scope.files.splice($scope.files.indexOf(file), 1);
      $scope.isLoading = false;
    }, function() {
      $dialog.messageBox('Delete error', 'Error deleting the file', [
        { result: 'ok', label: 'OK', cssClass: 'btn-primary' }
      ]);
      $scope.isLoading = false;
    });
  };

  // Copy a file's metadata to all others
  $scope.copyMetadata = function(property, file) {
    var value = propByPath(file, property);
    _.each(_.reject($scope.files, function(t) {
      return t == file;
    }), function(t) {
      propByPath(t, property, value);
    });
  };

  // Guess the order
  $scope.guessOrder = function() {
    _.each($scope.files, function(f, i) {
      f.order = ++i;
    });
  };

  // Load artists and albums for autocomplete
  Restangular.one('album').get().then(function(data) {
    $scope.albums = _.chain(data.albums).pluck('name').uniq().value();
  });
  Restangular.one('artist').get().then(function(data) {
    $scope.artists = _.chain(data.artists).pluck('name').uniq().value();
  });

  $scope.refresh();
});