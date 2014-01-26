'use strict';

/**
 * Audio player directive.
 */
App.directive('audioPlayer', function($rootScope, AudioPlayer) {
  return {
    restrict: 'E',
    controller: function($scope, $element) {
      $scope.audio = new Audio();
      $scope.currentNum = 0;

      // Tell others to give me my prev/next track (with audio.set message)
      $scope.next = function(){ $rootScope.$broadcast('audio.next'); };
      $scope.prev = function(){ $rootScope.$broadcast('audio.prev'); };

      // Tell audio element to play/pause, you can also use $scope.audio.play() or $scope.audio.pause();
      $scope.playpause = function(){ $scope.audio.paused ? $scope.audio.play() : $scope.audio.pause(); };

      // Listen for audio-element events, and broadcast stuff
      $scope.audio.addEventListener('play', function(){ $rootScope.$broadcast('audio.play', this); });
      $scope.audio.addEventListener('pause', function(){ $rootScope.$broadcast('audio.pause', this); });
      $scope.audio.addEventListener('timeupdate', function(){ $rootScope.$broadcast('audio.time', this); });
      $scope.audio.addEventListener('ended', function(){ $rootScope.$broadcast('audio.ended', this); $scope.next(); });

      // Current track has changed
      $rootScope.$on('audio.set', function() {
        var track = AudioPlayer.currentTrack();
        $scope.audio.src = track.url;
        $scope.audio.play();
        $scope.track = track;
      });

      // Returns current track progression
      $scope.timeProgress = function() {
        return $scope.audio.currentTime / $scope.audio.duration * 100;
      };

      $scope.seek = function(e) {
        $scope.audio.currentTime = e.offsetX / e.target.clientWidth * $scope.audio.duration;
      }

      // Update display of things - makes time-scrub work
      setInterval(function(){ $scope.$apply(); }, 250);
    },

    templateUrl: 'partial/audioplayer.html'
  };
});