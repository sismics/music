'use strict';

/**
 * Main controller.
 */
App.controller('Main', function($scope, $state, Playlist, User) {
  User.userInfo().then(function(data) {
    if (data.anonymous) {
      $state.transitionTo('login');
    } else {
      // Update playlist on application startup
      Playlist.update().then(function() {
        // Open the first track without playing it
        Playlist.open(0);
      });
    }
  });
});