'use strict';

/**
 * Sismics Music application.
 */
var App = angular.module('music',
    // Dependencies
    ['ui.state', 'ui.route', 'ngMobile', 'ngCookies']
  )

/**
 * Configuring modules.
 */
  .config(function ($stateProvider, $httpProvider) {
    // Configuring UI Router
    $stateProvider
      .state('login', {
        url: '/login',
        views: {
          'page': {
            templateUrl: 'partial/login.html',
            controller: 'Login'
          }
        }
      })
      .state('main', {
        url: '',
        abstract: true,
        views: {
          'page': {
            templateUrl: 'partial/main.html',
            controller: 'Main'
          }
        }
      })
      .state('main.default', {
        url: '',
        views: {
          'content': {
            controller: 'MainDefault'
          }
        }
      })
      .state('main.latestalbums', {
        url: '/latest',
        views: {
          'content': {
            templateUrl: 'partial/latestalbums.html',
            controller: 'LatestAlbums'
          }
        }
      })
      .state('main.playing', {
        url: '/playing',
        views: {
          'content': {
            templateUrl: 'partial/playing.html',
            controller: 'Playing'
          }
        }
      })
      .state('main.music', {
        url: '/music',
        views: {
          'content': {
            templateUrl: 'partial/music.html',
            controller: 'Music'
          }
        }
      })
      .state('main.album', {
        url: '/album/:id',
        views: {
          'content': {
            templateUrl: 'partial/album.html',
            controller: 'Album'
          }
        }
      })
  })

/**
 * Application initialization.
 */
.run(function($rootScope, $state, $stateParams) {
  $rootScope.$state = $state;
  $rootScope.$stateParams = $stateParams;
});