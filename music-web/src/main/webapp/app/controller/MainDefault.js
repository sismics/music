'use strict';

/**
 * Main defaultcontroller.
 */
App.controller('MainDefault', function($state) {
  $state.transitionTo('main.playing');
});