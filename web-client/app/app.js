'use strict';

/**
 * Sismics Music application.
 */
var App = angular.module('music',
    // Dependencies
    ['ui.state', 'ui.route', 'ui.bootstrap', 'ngMobile']
  )

/**
 * Configuring modules.
 */
.config(function($stateProvider, $httpProvider) {
  // Configuring UI Router
  // TODO
})

/**
 * Application initialization.
 */
.run(function($rootScope, $state, $stateParams) {
  $rootScope.$state = $state;
  $rootScope.$stateParams = $stateParams;
});