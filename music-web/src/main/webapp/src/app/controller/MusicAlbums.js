'use strict';

/**
 * Albums library controller.
 */
angular.module('music').controller('MusicAlbums', function($scope, $stateParams, $state, Restangular, Playlist, Album, $timeout) {
  // Initialize controller
  $scope.loaded = false;
  $scope.loading = false;
  $scope.filter = $stateParams.filter;
  $scope.order = $stateParams.order ? $stateParams.order : null;
  if ($scope.order == null) {
    if (localStorage.albumsOrder) {
      $scope.order = localStorage.albumsOrder;
    } else {
      $scope.order = 'alpha';
    }
  }
  $scope.albums = Album.getCache();
  $scope.total = 0;

  // Keep scroll position
  var cache = Album.getScrollPosition();
  $scope.cacheHeight = cache.height;
  $timeout(function() {
    window.scrollTo(0, cache.scroll);
  });
  var scroll = function() {
    Album.setScrollPosition(window.pageYOffset, $('#music-albums-container').height());
  };
  angular.element(window).bind('scroll', scroll);
  $scope.$on('$destroy', function () {
    angular.element(window).unbind('scroll', scroll);
  });

  // Load more albums
  $scope.loadMore = function(reset) {
    if (reset)  {
      $scope.albums = [];
      $scope.loaded = false;
    }

    if ($scope.total == $scope.albums.length && $scope.loaded || $scope.loading) {
      return;
    }

    $scope.loading = true;
    Restangular.one('album').get({
      limit: 20,
      offset: $scope.albums.length,
      search: $scope.filter,
      sort_column: $scope.getSortColumn($scope.order),
      asc: $scope.order == 'alpha'
    }).then(function(data) {
          $scope.albums = $scope.albums.concat(data.albums);
          $scope.total = data.total;
          $scope.loaded = true;
          $scope.loading = false;
          Album.setCache($scope.albums);
        });
  };

  // Debounced version
  $scope.loadMoreDebounced = _.debounce($scope.loadMore, 300);

  // Keep the filter and order in sync with the view state
  $scope.$watch('filter + order', function(a, b) {
    localStorage.albumsOrder = $scope.order;

    $state.go('main.music.albums', {
      filter: $scope.filter,
      order: $scope.order
    }, {
      location: 'replace',
      notify: false
    });

    if (a == b) return;
    $scope.loadMoreDebounced(true);
  });

  // Load and play an album
  $scope.playAlbum = function(album) {
    Restangular.one('album', album.id).get().then(function(data) {
      Playlist.removeAndPlayAll(_.pluck(data.tracks, 'id'));
    });
  };

  // Convert UI sort order to column number
  $scope.getSortColumn = function(sort) {
    switch (sort) {
      case 'alpha':
        return 0;
      case 'latest':
        return 1;
      case 'playcount':
        return 2;
      default:
        return 0;
    }
  }
});