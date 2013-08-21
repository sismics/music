'use strict';

/**
 * Album controller.
 */
App.controller('Album', function($scope, Api) {
  $scope.urlParameters = Api.getUrlParameters();
});