'use strict';

/**
 * Settings account controller.
 */
angular.module('music').controller('SettingsAccount', function($rootScope, $scope, User, Restangular, toaster) {
  // Edit user
  $scope.editUser = function() {
    Restangular.one('user').post('', $scope.user).then(function() {
      $scope.user = {};
      toaster.pop('success', 'Account update', 'Password successfully changed');
      $scope.editUserForm.submitted = false;
    });
  };

  // Refresh Last.fm data
  var refreshLastFm = function() {
    Restangular.one('user/lastfm').get().then(function(data) {
      $scope.lastFm = data;
    });
  };

  // If the user is already connected to Last.fm, refresh data
  User.userInfo().then(function(data) {
    if (data.lastfm_connected) {
      refreshLastFm();
    }
  });

  // Connect to Last.fm
  $scope.connectLastFm = function() {
    Restangular.one('user/lastfm').put($scope.lastfm).then(function() {
      $scope.lastfm = {};
      $rootScope.userInfo.lastfm_connected = true;
      toaster.pop('success', 'Last.fm', 'Account successfully connected');

      // Refresh Last.fm data to populate the box
      refreshLastFm();
    }, function(data) {
      toaster.pop('error', 'Last.fm', data.data.message);
    });
  }

  // Disconnect from Last.fm
  $scope.disconnectLastFm = function() {
    Restangular.one('user/lastfm').remove().then(function() {
      $rootScope.userInfo.lastfm_connected = false;
    });
  };
});