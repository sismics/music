'use strict';

/**
 * Websocket service.
 */
angular.module('music').factory('Websocket', function($websocket, Restangular) {
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
        service.dispatch(JSON.parse(message));
      })
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
      console.log(message);
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