'use strict';

/**
 * Sismics Music application.
 */
var App = angular.module('music',
    // Dependencies
    ['ui.router', 'ui.bootstrap', 'dialog', 'ui.route', 'restangular']
  )

/**
 * Configuring modules.
 */
  .config(function ($stateProvider, RestangularProvider, $httpProvider, $uiViewScrollProvider) {
    // Configuring UI Router
    $uiViewScrollProvider.useAnchorScroll();
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
      .state('main.settings', {
        url: '/settings',
        views: {
          'content': {
            templateUrl: 'partial/settings.html',
            controller: 'Settings'
          }
        }
      });

      // Configuring Restangular
      RestangularProvider.setBaseUrl('api');

      // Configuring $http to act like jQuery.ajax
      $httpProvider.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded;charset=utf-8';
      $httpProvider.defaults.headers.put['Content-Type'] = 'application/x-www-form-urlencoded;charset=utf-8';
      $httpProvider.defaults.transformRequest = [function(data) {
        var param = function(obj) {
          var query = '';
          var name, value, fullSubName, subName, subValue, innerObj, i;

          for(name in obj) {
            value = obj[name];

            if(value instanceof Array) {
              for(i=0; i<value.length; ++i) {
                subValue = value[i];
                fullSubName = name;
                innerObj = {};
                innerObj[fullSubName] = subValue;
                query += param(innerObj) + '&';
              }
            } else if(value instanceof Object) {
              for(subName in value) {
                subValue = value[subName];
                fullSubName = name + '[' + subName + ']';
                innerObj = {};
                innerObj[fullSubName] = subValue;
                query += param(innerObj) + '&';
              }
            }
            else if(value !== undefined && value !== null) {
              query += encodeURIComponent(name) + '=' + encodeURIComponent(value) + '&';
            }
          }

          return query.length ? query.substr(0, query.length - 1) : query;
        };

        return angular.isObject(data) && String(data) !== '[object File]' ? param(data) : data;
      }];
  })

/**
 * Application initialization.
 */
.run(function($rootScope, $state, $stateParams) {
  $rootScope.$state = $state;
  $rootScope.$stateParams = $stateParams;
});