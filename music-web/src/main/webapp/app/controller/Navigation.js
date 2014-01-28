'use strict';

/**
 * Navigation controller.
 */
App.controller('Navigation', function($rootScope, $scope, User, $state) {
  // Returns true if the user is admin
  $scope.$watch('userInfo', function(userInfo) {
    $scope.isAdmin = userInfo && userInfo.base_functions && userInfo.base_functions.indexOf('ADMIN') != -1;
  });
  
  // Load user data
  User.userInfo().then(function(data) {
    $rootScope.userInfo = data;
  });
  
  // User logout
  $scope.logout = function($event) {
    User.logout().then(function() {
      User.userInfo(true).then(function(data) {
        $rootScope.userInfo = data;
      });
      $state.transitionTo('login');
    });
    $event.preventDefault();
  };
});