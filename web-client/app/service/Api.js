'use strict';

/**
 * Subsonic API service.
 */
App.factory('Api', function($cookies, Base64, $http) {
  return {
    /**
     * Store the credentials in cookie.
     * @param username
     * @param password
     */
    storeCredentials: function(username, password) {
      $cookies.username = username;
      $cookies.password = password;
    },

    /**
     * Return default http config.
     */
    httpConfig: function(params) {
      if (!params) params = {};

      return {
        method: 'JSONP',
        params: _.extend(params, {
          c: 'sismicsmusic', // Client name
          v: '1.6.0', // REST API version
          f: 'jsonp', // Reponse type
          callback: 'JSON_CALLBACK' // JSONP callback
        }),
        header: {
          'Authorization': 'Basic ' + Base64.encode($cookies.username + ':' + $cookies.password)
        }
      }
    },

    /**
     * Returns authenticated URL parameters.
     */
    getUrlParameters: function() {
      return 'c=sismics&v=1.6.0&u=' + $cookies.username + '&p=' + $cookies.password;
    },

    /**
     * Returns base URL.
     */
    getBaseUrl: function() {
      // TODO Configure base URL in login form
      $cookies.base_url = 'http://sismics.bgamard.org/subsonic/rest';
      return $cookies.base_url;
    },

    /**
     * Check if we can use the authenticated API.
     */
    checkAccess: function() {
      return $http(_.extend(this.httpConfig(), {
        url: this.getBaseUrl() + '/ping.view'
      }));
    },

    getAlbums: function(params) {
      return $http(_.extend(this.httpConfig(params), {
        url: this.getBaseUrl() + '/getAlbumList.view'
      }));
    }
  }
});