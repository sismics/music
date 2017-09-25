'use strict';

/**
 * Album controller.
 */
angular.module('music').controller('AlbumArt', function($scope, $stateParams, $state, Restangular, $http, toaster, $dialog) {
  // Load album
  Restangular.one('album', $stateParams.id).get().then(function(data) {
    $scope.album = data;
    $scope.query = $scope.album.artist.name + " - " + $scope.album.name;
  });

  // Search with an external API
  $scope.apiSearch = function() {
    Restangular.one('albumart').one('search').get({query: $scope.query})
      .then(function (data) {
        $scope.results = data.albumArts;
        if ($scope.results.length === 0) {
          toaster.pop('warning', 'Search', 'No image found');
        }
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
        .post('albumart/fromurl', { url: $scope.url })
        .then(function(data) {
          if (!_.isUndefined(data.message) && data.message == 'AlbumArtNotCopied') {
            toaster.pop('warning', 'Album art not copied', 'Album directory not writable');
          }
          $state.transitionTo('main.album', { id: $stateParams.id });
        }, function(data) {
          var btns = [{ result:'ok', label: 'OK', cssClass: 'btn-primary' }];
          $dialog.messageBox('Album art', data.data.message, btns);
        });
  };

  // Upload a local file
  $scope.uploadFile = function() {
    var formData = new FormData();
    formData.append('file', $scope.file);

    $.ajax({
      type: 'PUT',
      url: '../api/album/' + $stateParams.id + '/albumart',
      data: formData,
      cache: false,
      contentType: false,
      processData: false,
      success: function() {
        $state.transitionTo('main.album', { id: $stateParams.id });
      },
      error: function() {
        toaster.pop('error', 'Album art upload failed', 'Please try again');
      }
    });
  };
});