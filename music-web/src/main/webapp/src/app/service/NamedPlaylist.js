'use strict';

/**
 * Named playlist service.
 */
angular.module('music').factory('NamedPlaylist', function($rootScope, $modal, Restangular) {
  $rootScope.playlists = [];

  var service = {
    update: function() {
      Restangular.one('playlist').get().then(function(data) {
        $rootScope.playlists = data.items;
      });
    },

    addToPlaylist: function(playlist, tracks) {
      Restangular.one('playlist/' + playlist.id + '/multiple').put({
        ids: _.pluck(tracks, 'id'),
        clear: false
      });
    },

    removeTrack: function(playlist, order) {
      return Restangular.one('playlist/' + playlist.id, order).remove();
    },

    remove: function(playlist) {
      return Restangular.one('playlist/' + playlist.id).remove().then(function() {
        $rootScope.playlists = _.reject($rootScope.playlists, function(p) {
          return p.id === playlist.id;
        });
      });
    },

    createPlaylist: function() {
      $modal.open({
        templateUrl: 'partial/modal.createplaylist.html',
        controller: function($scope, $modalInstance) {
          'ngInject';
          $scope.ok = function (name) {
            $modalInstance.close(name);
          };

          $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
          };
        }
      }).result.then(function(name) {
        Restangular.one('playlist').put({
          name: name
        }).then(function(data) {
          $rootScope.playlists.push(data.item);
        });
      });
    }
  };

  return service;
});