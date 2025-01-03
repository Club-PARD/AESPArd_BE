package com.pard.pree_be.utils;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;

import java.io.File;

public class AudioAnalyzer {

    public static double calculateAverageDecibel(String audioFilePath) throws Exception {
        // Create an audio dispatcher
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromFile(new File(audioFilePath), 1024, 512);

        // Accumulators for total decibel and frame count
        double[] totalDecibel = {0.0};
        int[] frameCount = {0};

        // Audio processor to calculate decibels
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
}
