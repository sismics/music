'use strict';

/**
 * Artists library controller.
 */
angular.module('music').controller('MusicArtists', function($scope, $stateParams, $state, Restangular) {
  // Initialize filtering
  $scope.loaded = false;
  $scope.loading = false;
  $scope.filter = $stateParams.filter;
  $scope.artists = [];
  $scope.total = 0;

  // Load more artists
  $scope.loadMore = function(reset) {
    if (reset)  {
      $scope.artists = [];
      $scope.loaded = false;
    }

    if ($scope.total == $scope.artists.length && $scope.loaded || $scope.loading) {
      return;
    }

    $scope.loading = true;
    Restangular.one('artist').get({
      limit: 20,
      offset: $scope.artists.length,
      search: $scope.filter,
      sort_column: 0,
      asc: true
    }).then(function(data) {
          $scope.artists = $scope.artists.concat(data.artists);
          $scope.total = data.total;
          $scope.loaded = true;
          $scope.loading = false;
        });
  };

  // Debounced version
  $scope.loadMoreDebounced = _.debounce($scope.loadMore, 300);

  // Keep the filter in sync with the view state
  $scope.$watch('filter', function(a, b) {
    $state.go('main.music.artists', {
      filter: $scope.filter
    }, {
      location: 'replace',
      notify: false
    });

    if (a == b) return;
    $scope.loadMoreDebounced(true);
  });
});