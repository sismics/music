'use strict';

/**
 * Websocket service.
 */
angular.module('music').factory('Websocket', function($websocket) {
  var stream = $websocket('ws://' + window.location.host + window.location.pathname + '../ws/test');

  stream.onOpen(function() {
    console.log('Websocket opened, sending message now!')
    stream.send(JSON.stringify({ message: 'test message' }));
  });

  stream.onMessage(function(message) {
    console.log(message);
  });

  return {
  }
});