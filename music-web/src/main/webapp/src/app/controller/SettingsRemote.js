'use strict';

/**
 * Settings remote control controller.
 */
angular.module('music').controller('SettingsRemote', function($scope, Websocket) {
  $scope.isRegistered = Websocket.isRegistered();
  $scope.token = Websocket.getToken();

  // Toggle remote control
  $scope.toggleRemoteControl = function() {
    if ($scope.isRegistered) {
      Websocket.register().then(function() {
        $scope.token = Websocket.getToken();
        Websocket.connect();
      });
    } else {
      $scope.token = null;
      Websocket.disconnect();
      Websocket.unregister();
    }
  };
});