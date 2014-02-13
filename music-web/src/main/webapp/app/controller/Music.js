'use strict';

/**
 * Music library controller.
 */
App.controller('Music', function($scope, $stateParams, $state, Restangular, filterFilter) {
  // Initialize filtering
  $scope.filter = $stateParams.filter;
  $scope.albums = [];
  $scope.filteredAlbums = [];
  $scope.allAlbums = [];
  var index = 0;
  
  // Load all albums
  Restangular.setDefaultHttpFields({cache: true});
  Restangular.all('album').getList().then(function(data) {
    $scope.allAlbums = data.albums;
    $scope.filteredAlbums = filterFilter($scope.allAlbums, $scope.filter);
    $scope.loadMore(true);
  });
  Restangular.setDefaultHttpFields({});

  // Load more albums
  $scope.loadMore = function(reset) {
    if (reset)  {
      $scope.albums = [];
      index = 0;
    }
    $scope.albums = $scope.albums.concat($scope.filteredAlbums.slice(index, index + 12));
    index += 12;
  };

  // Debounced version
  $scope.loadMoreDebounced = _.debounce($scope.loadMore, 300);

  // Keep the filter in sync with the view state
  $scope.$watch('filter', function() {
    $scope.filteredAlbums = filterFilter($scope.allAlbums, $scope.filter);
    $scope.loadMoreDebounced(true);

    $state.go('main.music', {
      filter: $scope.filter
    }, {
      location: 'replace',
      notify: false
    });
  });
});