'use strict';

/**
 * Audio player directive.
 */
App.directive('audioPlayer', function($rootScope, Playlist) {
  return {
    restrict: 'E',
    controller: function($scope) {
      $scope.audio = new Audio();
      $scope.audio.preload = 'auto';
      $scope.startPlaying = 0;
      $scope.firstPingSent = false;
      $scope.halfwayPingSent = false;

      // Listen for audio-element events, and broadcast stuff
      $scope.audio.addEventListener('play', function() { $rootScope.$broadcast('audio.play'); });
      $scope.audio.addEventListener('pause', function() { $rootScope.$broadcast('audio.pause'); });
      $scope.audio.addEventListener('ended', function() { $rootScope.$broadcast('audio.ended'); $scope.next(); });

      // Send a server ping when we start playing the track
      $scope.audio.addEventListener('play', function() {
        if (!$scope.firstPingSent) {
          // TODO Ping server
          $scope.startPlaying = new Date().getTime();
          $scope.firstPingSent = true;
          console.log('First server ping (' + $scope.audio.currentTime + '), music started at: ' + $scope.startPlaying);
        }
      });

      // Send a server ping halfway
      $scope.audio.addEventListener('timeupdate', _.throttle(function() {
        if ($scope.firstPingSent && !$scope.halfwayPingSent && $scope.audio.currentTime > $scope.audio.duration / 2) {
          // TODO Ping server
          $scope.halfwayPingSent = true;
          console.log('Halfway server ping (' + $scope.audio.currentTime + '), music started at: ' + $scope.startPlaying);
        }
      }, 500));

      // Current track has changed
      $scope.$on('audio.set', function(e, play) {
        var track = Playlist.currentTrack();
        $scope.firstPingSent = false;
        $scope.halfwayPingSent = false;
        $scope.audio.src = 'api/track/' + track.id;
        if (play) {
          $scope.audio.play();
        } else {
          $scope.audio.pause();
          $rootScope.$broadcast('audio.pause');
        }
        $scope.track = track;
      });

      // Update UI on track liked
      $scope.$on('track.liked', function(e, trackId, liked) {
        if ($scope.track != null && $scope.track.id == trackId) {
          $scope.track.liked = liked;
        }
      });

      // Stop the audio
      $scope.$on('audio.stop', function() {
        $scope.track = null;
        $scope.audio.pause();
        $scope.audio.src = '';
        $rootScope.$broadcast('audio.ended');
      });

      // Returns current track progression
      $scope.timeProgress = _.throttle(function() {
        if ($scope.audio.duration) {
          return $scope.audio.currentTime / $scope.audio.duration * 100;
        }
        return 0;
      }, 500);

      // Return current buffer progression
      $scope.bufferProgress = _.throttle(function() {
        var buff = $scope.audio.buffered;
        if (buff.length > 0) {
          var buffered = buff.end(buff.length - 1) - $scope.audio.currentTime;
          return buffered / $scope.audio.duration * 100;
        }
        return 0;
      }, 500);

      // Seek through the current track
      $scope.seek = function(e) {
        if ($scope.audio.duration) {
          var offX = e.clientX - $(e.delegateTarget).offset().left;
          $scope.audio.currentTime = offX / e.delegateTarget.clientWidth * $scope.audio.duration;
        }
      };

      // Toggle repeat
      $scope.repeat = Playlist.isRepeat();
      $scope.toggleRepeat = function() {
        Playlist.toggleRepeat();
        $scope.repeat = Playlist.isRepeat();
      };

      // Toggle shuffle
      $scope.shuffle = Playlist.isShuffle();
      $scope.toggleShuffle = function() {
        Playlist.toggleShuffle();
        $scope.shuffle = Playlist.isShuffle();
      };

      // Tell others to give me my prev/next track (with audio.set message)
      $scope.next = function() {
        Playlist.next();
      };
      $scope.prev = function() {
        Playlist.prev();
      };

      // Tell audio element to play/pause
      $rootScope.playpause = function() {
        if ($scope.track != null) {
          $scope.audio.paused ? $scope.audio.play() : $scope.audio.pause();
        }
      };

      // Mute/unmute volume
      $scope.mute = function() {
        $scope.audio.volume == 0 ? $scope.audio.volume = 1 : $scope.audio.volume = 0;
      };

      // Update display of things - makes time-scrub work
      setInterval(function(){ $scope.$apply(); }, 500);
    },

    templateUrl: 'partial/audioplayer.html'
  };
});