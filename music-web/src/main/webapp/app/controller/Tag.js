'use strict';

/**
 * Tag controller.
 */
App.controller('Tag', function($scope, $stateParams, $state, Restangular) {
  // Load album
  Restangular.one('album', $stateParams.id).get().then(function(data) {
    $scope.album = data;
  });

  // Back to album
  $scope.back = function() {
    $state.transitionTo('main.album', {
      id: $stateParams.id
    })
  };
});