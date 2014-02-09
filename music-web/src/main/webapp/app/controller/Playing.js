'use strict';

/**
 * Now playing controller.
 */
App.controller('Playing', function($scope, Playlist) {
  var updateScope = function() {
    $scope.currentOrder = Playlist.currentOrder();
    $scope.currentStatus = Playlist.currentStatus();
    $scope.tracks = Playlist.getTracks();
  };

  // Grab current playlist, and listen to future changes
  $scope.tracks = Playlist.getTracks();
  $scope.$on('playlist.updated', updateScope);

  // Grab current track, and listen to future changes
  updateScope();
  $scope.$on('audio.play', updateScope);
  $scope.$on('audio.pause', updateScope);
  $scope.$on('audio.ended', updateScope);

  // Remove a track from the playlist
  $scope.removeTrack = function(order) {
    Playlist.remove(order);
  };

  // Play a specific track from the playlist
  $scope.playTrack = function(order) {
    Playlist.play(order);
  };

  // Clear the playlist
  $scope.clear = function() {
    Playlist.clear(true);
  };

  // Like/unlike a track
  $scope.toggleLikeTrack = function(track) {
    Playlist.likeById(track.id, !track.liked);
  };

  // Configuration for track sorting
  $scope.trackSortableOptions = {
    forceHelperSize: true,
    forcePlaceholderSize: true,
    tolerance: 'pointer',
    handle: '.handle',
    containment: 'parent',
    helper: function(e, ui) {
      ui.children().each(function() {
        $(this).width($(this).width());
      });
      return ui;
    },
    stop: function (e, ui) {
      // Send new positions to server
      $scope.$apply(function () {
        Playlist.moveTrack(ui.item.attr('data-order'), ui.item.index());
      });
    }
  };
});