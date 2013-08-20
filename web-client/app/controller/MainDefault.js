'use strict';

/**
 * Main default controller.
 */
App.controller('MainDefault', function($state) {
  $state.transitionTo('main.latestalbums');
});