'use strict';

/**
 * Websocket service.
 */
angular.module('music').factory('Websocket', function($websocket, Restangular, toaster, Playlist, $rootScope) {
  var stream = null;

  var service = {
    /**
     * Connect this player.
     * @param user User data
     */
    connect: function() {
      var token = localStorage.playerToken;
      if (_.isUndefined(token) || stream != null) {
        return;
      }

      stream = $websocket('ws://' + window.location.host + window.location.pathname + '../ws/player?token=' + token);

      stream.onMessage(function(message) {
        if (message.data.trim().length == 0) {
          return;
        }

        console.log('ws message: ', message);
        var data = JSON.parse(message.data.substring(message.data.indexOf('|') + 1));

        if (!_.isUndefined(data.type) && data.type == 'PlayerNotFound') {
          // We have a bad token
          delete localStorage.playerToken;
          service.disconnect();
          return;
        }

        service.dispatch(data);
      });
    },

    /**
     * Register this player.
     * @returns Promise
     */
    register: function() {
      return Restangular.one('player').post('register').then(function(data) {
        localStorage.playerToken = data.token;
      });
    },

    /**
     * Unregister this player.
     */
    unregister: function() {
      Restangular.one('player').post('unregister', {
        token: localStorage.playerToken
      });
      delete localStorage.playerToken;
    },

    /**
     * Return true if this player is registered.
     * @returns {boolean} True if registered
     */
    isRegistered: function() {
      return !_.isUndefined(localStorage.playerToken);
    },

    /**
     * Return the player token.
     * @returns Token
     */
    getToken: function() {
      return localStorage.playerToken;
    },

    /**
     * Handle an incoming message.
     * @param message Message
     */
    dispatch: function(message) {
      switch(message.command) {
        case 'HELLO':
            toaster.pop('success', 'Remote control', 'Remote control connected');
          break;

        case 'PLAY_TRACK':
            Playlist.add({
              id: message.data[0],
              title: '' // We don't have the title :(
            }, true, true);
          break;

        case 'PREVIOUS':
            Playlist.prev();
          break;

        case 'NEXT':
          Playlist.next();
          break;

        case 'PLAY':
            $rootScope.$broadcast('audio.command.play');
          break;

        case 'PAUSE':
          $rootScope.$broadcast('audio.command.pause');
          break;
      }
    },

    /**
     * Disconnect this player.
     */
    disconnect: function() {
      if (stream == null) {
        return;
      }

      stream.close(true);
    }
  }

  return service;
});