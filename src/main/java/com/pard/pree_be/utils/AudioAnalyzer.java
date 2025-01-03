package com.pard.pree_be.utils;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.TarsosDSPAudioInputStream;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class AudioAnalyzer {

    public static double calculateAverageDecibel(InputStream inputStream) throws Exception {
        // Wrap the InputStream for buffering
        InputStream bufferedStream = new BufferedInputStream(inputStream);

        // Convert to AudioInputStream and reformat it if necessary
        AudioInputStream originalStream = AudioSystem.getAudioInputStream(bufferedStream);
        AudioInputStream convertedStream = convertToSupportedFormat(originalStream);

        // Convert javax.sound.sampled.AudioInputStream to TarsosDSPAudioInputStream
        TarsosDSPAudioInputStream tarsosStream = new JVMAudioInputStream(convertedStream);
        TarsosDSPAudioFormat tarsosFormat = new TarsosDSPAudioFormat(
                TarsosDSPAudioFormat.Encoding.PCM_SIGNED,
                44100,
                16,
                1,
                2,
                44100,
                true
        );

        // Create an AudioDispatcher
        int bufferSize = 1024; // Buffer size for audio processing
        int overlap = 512; // Overlap size for buffer chunks
        AudioDispatcher dispatcher = new AudioDispatcher(tarsosStream, bufferSize, overlap);

        // Accumulators for total decibel and frame count
        double[] totalDecibel = {0.0};
        int[] frameCount = {0};

        // Add a processor to calculate decibels
        dispatcher.addAudioProcessor(new AudioProcessor() {
            @Override
            public boolean process(AudioEvent audioEvent) {
                double rms = audioEvent.getRMS(); // Get RMS amplitude
                if (rms > 0) {
                    double decibel = 20 * Math.log10(rms); // Convert RMS to decibel
                    totalDecibel[0] += decibel;
                    frameCount[0]++;
                }
                return true;
            }

            @Override
            public void processingFinished() {
                // No cleanup needed
            }
        });

        // Run the dispatcher
        dispatcher.run();

        // Calculate average decibel
        return frameCount[0] > 0 ? totalDecibel[0] / frameCount[0] : 0.0;
    }

    private static AudioInputStream convertToSupportedFormat(AudioInputStream originalStream) {
        AudioFormat originalFormat = originalStream.getFormat();
        AudioFormat targetFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                44100,        // Sample rate
                16,           // Sample size in bits
                1,            // Channels (mono)
                2,            // Frame size (bytes per frame)
                44100,        // Frame rate
                true          // Big-endian
        );

        return AudioSystem.getAudioInputStream(targetFormat, originalStream);
    }
}
