'use strict';

/**
 * Navigation controller.
 */
angular.module('music').controller('Navigation', function($rootScope, $http, $scope, User, $state, Playlist, NamedPlaylist, Websocket) {
  // Returns true if at least an asynchronous request is in progress
  $scope.isLoading = function() {
    return $http.pendingRequests.length > 0;
  };

  // Load user data on application startup
  User.userInfo().then(function(data) {
    if (data.anonymous) {
      $state.transitionTo('login');
    } else {
      // Update playlist on application startup
      Playlist.update().then(function() {
        // Open the first track without playing it
        Playlist.open(0);
      });

      // Fetch named playlist
      NamedPlaylist.update();

      // Connect this player
      Websocket.connect();
    }
  });

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
    Playlist.reset();
    Websocket.disconnect();

    User.logout().then(function() {
      User.userInfo(true).then(function(data) {
        $rootScope.userInfo = data;
      });
      $state.transitionTo('login');
    });
    $event.preventDefault();
  };

  // Watch search query
  $scope.$watch('query', function(newval) {
    if (typeof newval == 'undefined') {
      return;
    }

    if (newval.length >= 3) {
      var isSearchView = $state.current.name == 'main.search';
      $state.go('main.search', {
        query: newval
      }, {
        location: isSearchView ? 'replace' : true,
        notify: !isSearchView
      });
    }
  });

  // Listen for state changes to sync the search form
  $rootScope.$on('$stateChangeStart',
      function(event, toState, toParams, fromState){
        if (fromState.name == 'main.search') {
          $scope.query = '';
        }
        if (toState.name == 'main.search') {
          $scope.query = toParams.query;
        }
      });
});