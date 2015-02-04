'use strict';

/**
 * Add music from external sources controller.
 */
angular.module('music').controller('AddExternal', function($scope, Restangular, toaster, $interval) {
  // Import form defaults
  var initImportForm = function() {
    $scope.import = {
      'quality': '192K',
      'format': 'mp3'
    };
  };
  initImportForm();

  // Retrieve dependencies versions
  Restangular.one('import/dependencies').get().then(function(data) {
    $scope.dependencies = data;
  }, function() {
    $scope.dependencies = {};
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
  $scope.imports = [];
  $scope.refresh = function() {
    Restangular.one('import/progress').get().then(function(data) {
      if ($scope.imports.length == data.imports.length) {
        // Copy message show state
        _.each(data.imports, function(imp, i) {
          data.imports[i].show = $scope.imports[i].show;
        });
      }
      $scope.imports = data.imports;
    });
  };

  // Cleanup finished imports
  $scope.cleanup = function() {
    Restangular.one('import').post('progress/cleanup').then(function() {
      $scope.refresh();
    });
  };

  // Retry an import
  $scope.retryImport = function(imp) {
    Restangular.one('import').post('progress/' + imp.id + '/retry').then(function() {
      $scope.refresh();
    });
  };

  // Kill an import
  $scope.killImport = function(imp) {
    Restangular.one('import').post('progress/' + imp.id + '/kill').then(function() {
      $scope.refresh();
    });
  };

  // Refresh periodically
  $scope.refresh();
  var stop = $interval(function() {
    if ($scope.imports.length > 0) {
      $scope.refresh();
    }
  }, 3000);

  $scope.$on('$destroy', function() {
    $interval.cancel(stop);
  });
});