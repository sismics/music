'use strict';

/**
 * Settings account controller.
 */
App.controller('SettingsAccount', function($scope, Restangular, toaster) {
  /**
   * Edit user.
   */
  $scope.editUser = function() {
    Restangular.one('user').post('', $scope.user).then(function() {
      $scope.user = {};
      toaster.pop('success', 'Account update', 'Password successfully changed');
    });
  };

  /**
   * Connect to Last.fm.
   */
  $scope.connectLastFm = function() {
    Restangular.one('user/lastfm').put($scope.lastfm).then(function() {
      $scope.lastfm = {};
      toaster.pop('success', 'Last.fm', 'Account successfully connected');
    }, function(data) {
      toaster.pop('error', 'Last.fm', data.data.message);
    });
  };
});