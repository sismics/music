'use strict';

/**
 * Audio visualization service.
 */
angular.module('music').factory('Visualization', function() {
  return {
    /**
     * Analyse audio for a frame based visualization.
     * @param audioElement Audio element
     * @param onDraw Drawing callback, called at each frame
     */
    analyseAudio: function(audioElement, onDraw) {
      var ctx = new AudioContext();
      var audioSrc = ctx.createMediaElementSource(audioElement);
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

      var draw = function() {
        requestAnimationFrame(draw);

        // update data in frequencyData
        analyser.getByteFrequencyData(frequencyData);

        // render frame based on values in frequencyData
        onDraw(frequencyData);
      };
      draw();
    }
  }
});