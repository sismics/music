'use strict';

/**
 * Main default controller.
 */
angular.module('music').controller('MainDefault', function($state) {
  $state.transitionTo('main.music.albums');
});