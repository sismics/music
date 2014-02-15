'use strict';

/**
 * Albums library controller.
 */
App.controller('MusicAlbums', function($scope, $stateParams, $state, Restangular, filterFilter, orderByFilter) {
  // Initialize controller
  $scope.filter = $stateParams.filter;
  $scope.order = $stateParams.order ? $stateParams.order : null;
  if ($scope.order == null) {
    if (localStorage.albumsOrder) {
      $scope.order = localStorage.albumsOrder;
    } else {
      $scope.order = 'alpha';
    }
  }
  $scope.albums = [];
  $scope.filteredAlbums = [];
  $scope.allAlbums = [];
  var index = 0;

  // Refresh album filtering
  var refreshFiltering = function() {
    var order = $scope.order == 'alpha' ? '+artist.name' : '-update_date';
    $scope.filteredAlbums = orderByFilter(filterFilter($scope.allAlbums, $scope.filter), order);
  };
  
  // Load all albums
  Restangular.setDefaultHttpFields({cache: true});
  Restangular.all('album').getList().then(function(data) {
    $scope.allAlbums = data.albums;
    refreshFiltering();
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

  // Keep the filter and order in sync with the view state
  $scope.$watch('filter + order', function() {
    refreshFiltering();
    $scope.loadMoreDebounced(true);
    localStorage.albumsOrder = $scope.order;

    $state.go('main.music.albums', {
      filter: $scope.filter,
      order: $scope.order
    }, {
      location: 'replace',
      notify: false
    });
  });
});