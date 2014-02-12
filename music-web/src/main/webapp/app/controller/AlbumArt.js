'use strict';

/**
 * Album controller.
 */
App.controller('AlbumArt', function($scope, $stateParams, $state, Restangular, $http) {
  // Load album
  Restangular.one('album', $stateParams.id).get().then(function(data) {
    $scope.album = data;
    $scope.query = $scope.album.artist.name + " - " + $scope.album.name;
  });

  // Search with an external API
  $scope.apiSearch = function() {
    $http({
      method: 'JSONP',
      url: 'https://ajax.googleapis.com/ajax/services/search/images?v=1.0&rsz=8&q=' + $scope.query + '&callback=JSON_CALLBACK'
    })
        .success(function(data) {
          $scope.results = data.responseData.results;
        });
  };

  // Select a search result
  $scope.selectResult = function(url) {
    $scope.url = url;
    $scope.uploadLink();
  };

  // Update the album art with the given URL
  $scope.uploadLink = function() {
    Restangular.one('album', $stateParams.id)
        .post('albumart', { url: $scope.url })
        .then(function() {
          $state.transitionTo('main.album', { id: $stateParams.id });
        });
  };
});