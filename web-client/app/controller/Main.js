'use strict';

/**
 * Main controller.
 */
App.controller('Main', function(Api, $state) {
  // Check that the user can use authenticated API
  Api.checkAccess().then(null, function() {
    $state.transitionTo('login');
  });
});