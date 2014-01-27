'use strict';

/**
 * Main default controller.
 */
App.controller('MainDefault', function($state, User) {
  User.userInfo().then(function(data) {
    if (data.anonymous) {
      $state.transitionTo('login');
    } else {
      $state.transitionTo('main.music');
    }
  });
});