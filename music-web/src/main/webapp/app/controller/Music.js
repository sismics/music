'use strict';

/**
 * Music library controller.
 */
App.controller('Music', function($scope, $stateParams, $state, Restangular) {
  // Initialize filtering
  $scope.filter = $stateParams.filter;
  
  // Load all albums
  Restangular.setDefaultHttpFields({cache: true});
  Restangular.all('album').getList().then(function(data) {
    $scope.albums = data.albums;
  });
  Restangular.setDefaultHttpFields({});

  // Keep the filter in sync with the view state
  $scope.$watch('filter', function() {
    $state.go('main.music', {
      filter: $scope.filter
    }, {
      location: 'replace',
      notify: false
    });
  });
});