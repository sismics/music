'use strict';

/**
 * Add music from external sources controller.
 */
angular.module('music').controller('AddExternal', function($scope, Restangular, toaster) {
  // Import form defaults
  var initImportForm = function() {
    $scope.import = {
      'quality': '192K',
      'format': 'mp3'
    };
  };
  initImportForm();

  // Check prerequisites
  Restangular.one('import/check').get().then(function(data) {
    $scope.check = data;
  }, function() {
    $scope.check = {};
  });

  // Start a new import
  $scope.startImport = function() {
    Restangular.one('import').put({
      url: $scope.import.url.split('\n'),
      quality: $scope.import.quality,
      format: $scope.import.format
    })
        .then(function() {
          toaster.pop('info', 'Import', 'Import in progress...');
          initImportForm();
          $scope.refresh();
        }, function() {
          $dialog.messageBox('Import error', 'Error starting import', [
            { result: 'ok', label: 'OK', cssClass: 'btn-primary' }
          ]);
        });
  };

  // Refresh import progress
  $scope.refresh = function() {
    Restangular.one('import').getList('progress').then(function(data) {
      $scope.imports = data.imports;
    });
  };

  // Cleanup finished imports
  $scope.cleanup = function() {
    Restangular.one('import').post('progress/cleanup').then(function() {
      $scope.refresh();
    });
  };

  $scope.refresh();
});