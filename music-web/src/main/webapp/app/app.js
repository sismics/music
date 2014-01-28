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
            templateUrl: 'partial/main.default.html',
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
      .state('main.settingsdirectories', {
        url: '/settings/directories',
        views: {
          'content': {
            templateUrl: 'partial/settings.directories.html',
            controller: 'SettingsDirectories'
          }
        }
      })
      .state('main.settingsaccount', {
      url: '/settings/account',
      views: {
        'content': {
          templateUrl: 'partial/settings.account.html',
          controller: 'SettingsAccount'
        }
      }
    })
    .state('main.settingslog', {
      url: '/settings/log',
      views: {
        'content': {
          templateUrl: 'partial/settings.log.html',
          controller: 'SettingsLog'
        }
      }
    })
    .state('main.settingsuser', {
      url: '/settings/user',
      views: {
        'content': {
          templateUrl: 'partial/settings.user.html',
          controller: 'SettingsUser'
        }
      }
    })
    .state('main.settingsuser.edit', {
      url: '/edit/:username',
      views: {
        'user': {
          templateUrl: 'partial/settings.user.edit.html',
          controller: 'SettingsUserEdit'
        }
      }
    })
    .state('main.settingsuser.add', {
      url: '/add',
      views: {
        'user': {
          templateUrl: 'partial/settings.user.edit.html',
          controller: 'SettingsUserEdit'
        }
      }
    })

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