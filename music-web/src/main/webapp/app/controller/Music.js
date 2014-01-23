'use strict';

/**
 * Music library controller.
 */
App.controller('Music', function($scope, Api) {
  $scope.urlParameters = Api.getUrlParameters();

  Api.getAlbums({
    type: 'recent',
    size: 500
  }).then(function(response) {
      $scope.albums = response.data['subsonic-response'].albumList.album;
  });
});