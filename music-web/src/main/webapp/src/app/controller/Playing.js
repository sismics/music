'use strict';

/**
 * Now playing controller.
 */
angular.module('music').controller('Playing', function($scope, Playlist) {
  var updateScope = function() {
    $scope.currentOrder = Playlist.currentOrder();
    $scope.currentStatus = Playlist.currentStatus();
    $scope.tracks = Playlist.getTracks();
    $scope.partyMode = Playlist.isPartyMode();
    $scope.duration = _.reduce($scope.tracks,
        function(duration, track){
          return duration + track.length;
        }, 0);
  };

  // Listen to playlist changes
  $scope.$on('playlist.updated', updateScope);

  // Grab current track, and listen to future changes
  updateScope();
  $scope.$on('audio.play', updateScope);
  $scope.$on('audio.pause', updateScope);
  $scope.$on('audio.ended', updateScope);
  $scope.$on('playlist.party', updateScope);

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