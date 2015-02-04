'use strict';

/**
 * Transcoding settings controller.
 */
angular.module('music').controller('SettingsTranscoding', function($scope, Restangular, $dialog) {
  // Initialize transcoder form
  var initForm = function() {
    $scope.transcoder = {
      name: '',
      source: '',
      destination: '',
      step1: '',
      step2: ''
    };
  };
  initForm();
  
  // Load transcoders
  $scope.loadTranscoders = function() {
    Restangular.one('transcoder').get().then(function(data) {
      $scope.transcoders = data.transcoders;
    });
  };
  $scope.loadTranscoders();

  // Remove a transcoder
  $scope.deleteTranscoder = function(transcoder) {
    var title = 'Delete transcoder';
    var msg = 'Do you really want to delete transcoder ' + transcoder.name + '?';
    var btns = [
      { result: 'cancel', label: 'Cancel' },
      { result: 'ok', label: 'OK', cssClass: 'btn-primary' }
    ];

    $dialog.messageBox(title, msg, btns, function (result) {
      if (result == 'ok') {
        Restangular.one('transcoder', transcoder.id).remove().then(function () {
          $scope.loadTranscoders();
        });
      }
    });
  };
  
  // Add a transcoder
  $scope.addTranscoder = function() {
    Restangular.one('transcoder').put($scope.transcoder)
        .then(function() {
          // Reset transcoder form
          initForm();

          // Reload transcoders
          $scope.loadTranscoders();
        });
  };
});