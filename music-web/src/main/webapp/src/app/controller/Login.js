'use strict';

/**
 * Login controller.
 */
angular.module('music').controller('Login', function($rootScope, $scope, $state, $dialog, User, Playlist, NamedPlaylist, Websocket) {
  $scope.login = function() {
    User.login($scope.user).then(function() {
      User.userInfo(true).then(function(data) {
        $rootScope.userInfo = data;
      });

      // Update playlist on application startup
      Playlist.update().then(function() {
        // Open the first track without playing it
        Playlist.open(0);
      });

      // Fetch named playlist
      NamedPlaylist.update();

      // Connect this player
      Websocket.connect();

      $state.transitionTo('main.music.albums');
    }, function() {
      var title = 'Login failed';
      var msg = 'Username or password invalid';
      var btns = [{ result:'ok', label: 'OK', cssClass: 'btn-primary' }];

      $dialog.messageBox(title, msg, btns);
    });
  };
});