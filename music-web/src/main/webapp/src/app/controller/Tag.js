'use strict';

/**
 * Tag controller.
 */
angular.module('music').controller('Tag', function($scope, $stateParams, $state, Restangular, $dialog, $q, toaster) {
  // Load album
  Restangular.one('album', $stateParams.id).get().then(function(data) {
    $scope.album = data;

    // Copy some data to make model binding easier
    _.each($scope.album.tracks, function(track) {
      track.album_name = $scope.album.name;
      track.album_artist_name = $scope.album.artist.name;
    });

    // Working copy
    $scope.tracks = angular.copy($scope.album.tracks);
  });

  // Back to album
  $scope.back = function() {
    $state.transitionTo('main.album', {
      id: $stateParams.id
    })
  };

  // Return modified tracks
  var pendingTracks = function() {
    return _.filter($scope.tracks, function(track, i) {
      if (!$scope.trackEquals(track, $scope.album.tracks[i])) {
      }
      return !$scope.trackEquals(track, $scope.album.tracks[i]);
    });
  };

  // Alert if there is pending changes on exit
  $scope.$on('$stateChangeStart', function(e, toState, toParams, fromState) {
    if (fromState.name != 'tag') {
      return;
    }

    if (_.size(pendingTracks()) > 0) {
      // There if pending changes, ask what to do
      var title = 'Pending changes';
      var msg = 'You have unsaved songs, save before exit?';
      var btns = [{ result:'save', label: 'Save', cssClass: 'btn-primary' },
        { result:'discard', label: 'Discard', cssClass: 'btn-danger' },];

      $dialog.messageBox(title, msg, btns, function(result) {
        if (result == 'save') {
          $scope.save();
        } else {
          // Discard changes, overwrite changes and go on with the asked state
          $scope.tracks = angular.copy($scope.album.tracks);
          $state.transitionTo(toState.name, toParams);
        }
      });
      e.preventDefault();
    }

    // No pending changes, continue
  });

  // Edit a track
  $scope.track = null;
  $scope.editTrack = function(track) {
    $scope.track = track;
    return false;
  }

  // Return true if 2 tracks are equals
  $scope.trackEquals = function(track1, track2) {
    return angular.equals(track1, track2);
  };

  // Save provided tracks
  $scope.saving = false;
  $scope.save = function() {
    var tracks = pendingTracks();
    var promises = [];
    $scope.saving = true;

    // Update all tracks
    _.each(tracks, function(track) {
      promises.push(Restangular.one('track', track.id).post('', {
        order: track.order,
        title: track.title,
        album: track.album_name,
        artist: track.artist.name,
        album_artist: track.album_artist_name,
        year: track.year
      }));
    });

    // All tracks are saved, override pending changes
    $q.all(promises).then(function() {
      $scope.album.tracks = angular.copy($scope.tracks);
      toaster.pop('success', 'Tagging', 'All tracks saved');
      $scope.saving = false;
    }, function() {
      toaster.pop('error', 'Tagging', 'Error while saving tracks');
      $scope.saving = false;
    });
  };

  // Copy a track's metadata to all others
  $scope.copyMetadata = function(property, track) {
    var value = propByPath(track, property);
    _.each(_.reject($scope.tracks, function(t) {
      return t == track;
    }), function(t) {
      propByPath(t, property, value);
    });
  };
});