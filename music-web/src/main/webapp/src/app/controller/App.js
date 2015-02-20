'use strict';

/**
 * App controller.
 */
angular.module('music').controller('App', function($rootScope, $scope) {
  // Is the user typing
  var isTyping = function() {
    var activeTag = document.activeElement.tagName.toLowerCase();
    return activeTag == 'input' || activeTag == 'textarea' || activeTag == 'button';
  };

  // Play/pause shortcut
  $scope.shortcutPlayPause = function(e) {
    if (isTyping()) {
      return;
    }
    $rootScope.playpause();
    e.preventDefault();
  };
});