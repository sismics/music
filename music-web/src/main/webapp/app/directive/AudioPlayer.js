'use strict';

/**
 * Audio player directive.
 */
App.directive('audioPlayer', function($rootScope, Playlist) {
  return {
    restrict: 'E',
    controller: function($scope, $element) {
      $scope.audio = new Audio();
      $scope.currentNum = 0;

      // Tell others to give me my prev/next track (with audio.set message)
      $scope.next = function() {
        Playlist.next();
      };
      $scope.prev = function() {
        Playlist.prev();
      };

      // Tell audio element to play/pause, you can also use $scope.audio.play() or $scope.audio.pause();
      $scope.playpause = function() {
        if ($scope.track != null) {
          $scope.audio.paused ? $scope.audio.play() : $scope.audio.pause();
        }
      };

      // Mute/unmute volume
      $scope.mute = function() {
        $scope.audio.volume == 0 ? $scope.audio.volume = 1 : $scope.audio.volume = 0;
      };

      // Listen for audio-element events, and broadcast stuff
      $scope.audio.addEventListener('play', function(){ $rootScope.$broadcast('audio.play'); });
      $scope.audio.addEventListener('pause', function(){ $rootScope.$broadcast('audio.pause'); });
      $scope.audio.addEventListener('timeupdate', function(){ $rootScope.$broadcast('audio.time'); });
      $scope.audio.addEventListener('ended', function(){ $rootScope.$broadcast('audio.ended'); $scope.next(); });

      // Current track has changed
      $scope.$on('audio.set', function(e, play) {
        var track = Playlist.currentTrack();
        $scope.audio.src = 'api/track/' + track.id;
        if (play) {
          $scope.audio.play();
        } else {
          $scope.audio.pause();
          $rootScope.$broadcast('audio.pause');
        }
        $scope.track = track;
      });

      // Stop the audio
      $scope.$on('audio.stop', function() {
        $scope.track = null;
        $scope.audio.pause();
        $scope.audio.src = '';
        $rootScope.$broadcast('audio.ended');
      });

      // Returns current track progression
      $scope.timeProgress = function() {
        return $scope.audio.currentTime / $scope.audio.duration * 100;
      };

      // Seek through the current track
      $scope.seek = function(e) {
        var offX = e.clientX - $(e.delegateTarget).offset().left;
        $scope.audio.currentTime = offX / e.delegateTarget.clientWidth * $scope.audio.duration;
      };

      // Toggle repeat
      $scope.repeat = Playlist.isRepeat();
      $scope.toggleRepeat = function() {
        Playlist.toggleRepeat();
        $scope.repeat = Playlist.isRepeat();
      }

      // Toggle shuffle
      $scope.shuffle = Playlist.isShuffle();
      $scope.toggleShuffle = function() {
        Playlist.toggleShuffle();
        $scope.shuffle = Playlist.isShuffle();
      };

      // Update display of things - makes time-scrub work
      setInterval(function(){ $scope.$apply(); }, 250);
    },

    templateUrl: 'partial/audioplayer.html'
  };
});