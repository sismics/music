'use strict';

/**
 * Search controller.
 */
App.controller('Search', function($rootScope, $scope, $stateParams, Restangular) {
  // Server call debounced
  var search = _.debounce(function(query) {
    Restangular.one('search', query).get().then(function(data) {
      $scope.results = data;
    });
  }, 300);

  // Watch the search query
  $scope.$watch('$stateParams.query', function(newval) {
    search(newval);
  });
});