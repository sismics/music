'use strict';

/**
 * Sismics Music application.
 */
angular.module('music',
        // Dependencies
        ['ui.router', 'ui.bootstrap', 'dialog', 'ui.route', 'ui.keypress', 'angular-websocket',
          'restangular', 'ui.sortable', 'pasvaz.bindonce', 'toaster', 'infinite-scroll', 'monospaced.qrcode'])

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
          .state('tag', {
            url: '/album/:id/tag',
            views: {
              'page': {
                templateUrl: 'partial/tag.html',
                controller: 'Tag'
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
          .state('main.search', {
            url: '/search/*query',
            views: {
              'content': {
                templateUrl: 'partial/search.html',
                controller: 'Search'
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
          .state('main.lyrics', {
            url: '/lyrics',
            views: {
              'content': {
                templateUrl: 'partial/lyrics.html',
                controller: 'Lyrics'
              }
            }
          })
          .state('main.music', {
            url: '/music',
            views: {
              'content': {
                templateUrl: 'partial/music.html'
              }
            }
          })
          .state('main.music.albums', {
            url: '/albums/{filter}:{order:alpha|latest|playcount|}',
            views: {
              'tab': {
                templateUrl: 'partial/music.albums.html',
                controller: 'MusicAlbums'
              }
            }
          })
          .state('main.music.artists', {
            url: '/artists/*filter',
            views: {
              'tab': {
                templateUrl: 'partial/music.artists.html',
                controller: 'MusicArtists'
              }
            }
          })
          .state('main.playlist', {
            url: '/playlist/:id',
            views: {
              'content': {
                templateUrl: 'partial/playlist.html',
                controller: 'Playlist'
              }
            }
          })
          .state('main.add', {
            url: '/add',
            views: {
              'content': {
                templateUrl: 'partial/add.html',
                controller: 'Add'
              }
            }
          })
          .state('main.add.upload', {
            url: '/upload',
            views: {
              'tab': {
                templateUrl: 'partial/add.upload.html',
                controller: 'AddUpload'
              }
            }
          })
          .state('main.add.external', {
            url: '/external',
            views: {
              'tab': {
                templateUrl: 'partial/add.external.html',
                controller: 'AddExternal'
              }
            }
          })
          .state('main.add.import', {
            url: '/import',
            views: {
              'tab': {
                templateUrl: 'partial/add.import.html',
                controller: 'AddImport'
              }
            }
          })
          .state('main.artist', {
            url: '/artist/:id',
            views: {
              'content': {
                templateUrl: 'partial/artist.html',
                controller: 'Artist'
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
          .state('main.albumart', {
            url: '/album/:id/albumart',
            views: {
              'content': {
                templateUrl: 'partial/albumart.html',
                controller: 'AlbumArt'
              }
            }
          })
          .state('main.settingsremote', {
            url: '/settings/remote',
            views: {
              'content': {
                templateUrl: 'partial/settings.remote.html',
                controller: 'SettingsRemote'
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
          .state('main.settingstranscoding', {
            url: '/settings/transcoding',
            views: {
              'content': {
                templateUrl: 'partial/settings.transcoding.html',
                controller: 'SettingsTranscoding'
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
          .state('main.settingscheck', {
            url: '/settings/check',
            views: {
              'content': {
                templateUrl: 'partial/settings.check.html',
                controller: 'SettingsCheck'
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
      RestangularProvider.setBaseUrl('../api');

      // Configuring $http to act like jQuery.ajax
      $httpProvider.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded;charset=utf-8';
      $httpProvider.defaults.headers.put['Content-Type'] = 'application/x-www-form-urlencoded;charset=utf-8';
      $httpProvider.defaults.transformRequest = [function (data) {
        var param = function (obj) {
          var query = '';
          var name, value, fullSubName, subName, subValue, innerObj, i;

          for (name in obj) {
            value = obj[name];

            if (value instanceof Array) {
              for (i = 0; i < value.length; ++i) {
                subValue = value[i];
                fullSubName = name;
                innerObj = {};
                innerObj[fullSubName] = subValue;
                query += param(innerObj) + '&';
              }
            } else if (value instanceof Object) {
              for (subName in value) {
                subValue = value[subName];
                fullSubName = name + '[' + subName + ']';
                innerObj = {};
                innerObj[fullSubName] = subValue;
                query += param(innerObj) + '&';
              }
            }
            else if (value !== undefined && value !== null) {
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
    .run(function ($rootScope, $state, $stateParams) {
      $rootScope.$state = $state;
      $rootScope.$stateParams = $stateParams;
    });