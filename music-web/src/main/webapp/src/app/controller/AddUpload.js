'use strict';

/**
 * Add music from local computer controller.
 */
angular.module('music').controller('AddUpload', function($scope) {
  $scope.queue = [];

  // Add files from form to the queue
  $scope.$watch('files', function() {
    if (_.size($scope.files) == 0) {
      return;
    }

    for (var i = 0; i < _.size($scope.files); i++) {
      $scope.queue.push({
        file: $scope.files[i],
        status: 'PENDING',
        message: 'Pending',
        progress: 0
      });
    }
    $scope.files = [];
    $scope.startUpload();
  });

  $scope.startUpload = function () {
    // Find a suitable item to upload
    var item = null;
    for (var i = 0; i < $scope.queue.length; i++) {
      if ($scope.queue[i].status == 'INPROGRESS') break;
      if ($scope.queue[i].status == 'PENDING') {
        item = $scope.queue[i];
        break;
      }
    }

    // An upload is in progress or nothing is uploadable, stop there
    if (item == null) {
      return;
    }

    // Build the payload
    item.status = 'INPROGRESS';
    item.message = 'Uploading';
    var formData = new FormData();
    formData.append('file', item.file);

    // Send the file
    $.ajax({
      type: 'PUT',
      url: '../api/import/upload',
      data: formData,
      cache: false,
      contentType: false,
      processData: false,
      success: function() {
        item.status = 'DONE';
        item.message = 'OK';
        $scope.startUpload();
      },
      error: function(jqXHR) {
        item.status = 'ERROR';
        if (!_.isUndefined(jqXHR.responseJSON)) {
          item.message = jqXHR.responseJSON.message;
        }
        $scope.startUpload();
      },
      xhr: function() {
        var myXhr = $.ajaxSettings.xhr();
        myXhr.upload.addEventListener(
            'progress', function(e) {
              $scope.$apply(function() {
                item.progress = (1.0 - (e.total - e.loaded) / e.total) * 100;
              });
            }, false);
        return myXhr;
      }
    });
  };
});