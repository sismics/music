'use strict';

/**
 * Audio player directive.
 */
angular.module('music').directive('audioPlayer', function($rootScope, Playlist, Restangular) {
  return {
    restrict: 'E',
    controller: function($scope) {
      $scope.audio = new Audio();
      $scope.audio.preload = 'auto';
      $scope.startPlaying = 0;
      $scope.firstPingSent = false;
      $scope.halfwayPingSent = false;


      ///
      var ctx = new AudioContext();
      var audioSrc = ctx.createMediaElementSource($scope.audio);
      var analyser = ctx.createAnalyser();
      audioSrc.connect(analyser);

      // Analyser configuration
      analyser.fftSize = 2048;

      // frequencyBinCount tells you how many values you'll receive from the analyser
      var frequencyData = new Uint8Array(analyser.frequencyBinCount);

      // This is necessary, or else no sound (?)
      var gainNode = ctx.createGain();
      audioSrc.connect(gainNode);
      gainNode.connect(ctx.destination);

      // we're ready to receive some data!
      var canvasCtx = $('#visual')[0].getContext('2d');
      var draw = function() {
        requestAnimationFrame(draw);

        // update data in frequencyData
        analyser.getByteFrequencyData(frequencyData);
        // render frame based on values in frequencyData
        canvasCtx.clearRect(0, 0, 1024, 256);
        canvasCtx.fillStyle = 'rgba(50, 50, 50, 0.2)';

        var x = 0;
        for(var i = 0; i < frequencyData.length; i++) {
          var freq = frequencyData[i];
          canvasCtx.fillRect(x, 256 - freq, 1, freq);
          x++;
        }
      };
      draw();
      ///


      // Restore saved volume
      $scope.savedVolume = _.isUndefined(localStorage.savedVolume) ? 1 : parseFloat(localStorage.savedVolume);
      $scope.volume = $scope.audio.volume = $scope.savedVolume;

      // Listen for audio-element events, and broadcast stuff
      $scope.audio.addEventListener('play', function() { $rootScope.$broadcast('audio.play'); });
      $scope.audio.addEventListener('pause', function() { $rootScope.$broadcast('audio.pause'); });
      $scope.audio.addEventListener('ended', function() { $rootScope.$broadcast('audio.ended'); $scope.next(); });

      // Restart music on error
      $scope.audio.addEventListener('error', function() {
        var currentTime = $scope.audio.currentTime;
        $scope.audio.play();
        var f = function() {
          $scope.audio.currentTime = currentTime;
          $scope.audio.removeEventListener('loadeddata', f);
        };
        $scope.audio.addEventListener('loadeddata', f);
      });

      // Ping the server
      var pingServer = function() {
        if (!$scope.track) {
          return;
        }

        Restangular.one('player').post('listening', {
          id: $scope.track.id,
          date: $scope.startPlaying,
          duration: parseInt($scope.audio.currentTime)
        });
      };

      // Send a server ping when we start playing the track
      $scope.audio.addEventListener('play', function() {
        if (!$scope.firstPingSent) {
          $scope.startPlaying = new Date().getTime();
          $scope.firstPingSent = true;
          pingServer();
        }
      });

      // Send a server ping halfway
      $scope.audio.addEventListener('timeupdate', _.throttle(function() {
        if ($scope.firstPingSent && !$scope.halfwayPingSent && $scope.audio.currentTime > $scope.audio.duration / 2) {
          $scope.halfwayPingSent = true;
          pingServer();
        }
      }, 500));

      // Current track has changed
      $scope.$on('audio.set', function(e, play) {
        var track = Playlist.currentTrack();
        $scope.firstPingSent = false;
        $scope.halfwayPingSent = false;
        $scope.track = track;
        $scope.audio.src = '../api/track/' + track.id;

        if (play) {
          $scope.audio.play();
        } else {
          $scope.audio.pause();
          $rootScope.$broadcast('audio.pause');
        }
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
        var buffers = [];
        var buff = $scope.audio.buffered;
        for (var i = 0; i < buff.length; i++) {
          buffers.push({
            start: buff.start(i) / $scope.audio.duration * 100,
            end: buff.end(i) / $scope.audio.duration * 100
          });
        }
        return buffers;
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
        if ($scope.audio.currentTime > 5) {
          $scope.audio.currentTime = 0;
        } else {
          Playlist.prev();
        }
      };

      // Tell audio element to play/pause
      $rootScope.playpause = function() {
        if ($scope.track != null) {
          $scope.audio.paused ? $scope.audio.play() : $scope.audio.pause();
        }
      };

      // Mute/unmute volume
      $scope.mute = function() {
        $scope.audio.volume == 0 ? $scope.audio.volume = $scope.savedVolume : $scope.audio.volume = 0;
        $scope.volume = $scope.audio.volume;
      };

      // Update display of things - makes time-scrub work
      setInterval(function(){ $scope.$apply(); }, 500);

      // Save volume
      $scope.saveVolume = function() {
        localStorage.savedVolume = $scope.audio.volume;
        $scope.savedVolume = $scope.audio.volume;
        $scope.audio.volume = $scope.volume;
      };
    },

    templateUrl: 'partial/audioplayer.html'
  };
});