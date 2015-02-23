'use strict';

/**
 * Websocket service.
 */
angular.module('music').factory('Websocket', function($websocket, Restangular) {
  /*Restangular.one('player').post('register').then(function(data) {
    var token = data.token;

    var stream = $websocket('ws://' + window.location.host + window.location.pathname + '../ws/player?token=' + token);

    stream.onOpen(function() {
      console.log('Websocket opened, sending message now!')

      Restangular.one('../ws/player').post('command', {
        token: token,
        json: JSON.stringify({ 'command': 'play', 'trackId': 'fake_track_id' })
      });
    });

    stream.onMessage(function(message) {
      console.log('ws message received!', message);
    });
  });*/

  return {
  }
});