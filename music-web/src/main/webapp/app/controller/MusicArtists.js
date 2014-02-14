'use strict';

/**
 * Artists library controller.
 */
App.controller('MusicArtists', function($scope, $stateParams, $state, Restangular, filterFilter) {
  // Initialize filtering
  $scope.filter = $stateParams.filter;
  $scope.artists = [];
  $scope.filteredArtists = [];
  $scope.allArtists = [];
  var index = 0;
  
  // Load all artists
  Restangular.setDefaultHttpFields({cache: true});
  Restangular.all('artist').getList().then(function(data) {
    $scope.allArtists = data.artists;
    $scope.filteredArtists = filterFilter($scope.allArtists, $scope.filter);
    $scope.loadMore(true);
  });
  Restangular.setDefaultHttpFields({});

  // Load more artists
  $scope.loadMore = function(reset) {
    if (reset)  {
      $scope.artists = [];
      index = 0;
    }
    $scope.artists = $scope.artists.concat($scope.filteredArtists.slice(index, index + 40));
    index += 40;
  };

  // Debounced version
  $scope.loadMoreDebounced = _.debounce($scope.loadMore, 50);

  // Keep the filter in sync with the view state
  $scope.$watch('filter', function() {
    $scope.filteredArtists = filterFilter($scope.allArtists, $scope.filter);
    $scope.loadMoreDebounced(true);

    $state.go('main.music.artists', {
      filter: $scope.filter
    }, {
      location: 'replace',
      notify: false
    });
  });
});