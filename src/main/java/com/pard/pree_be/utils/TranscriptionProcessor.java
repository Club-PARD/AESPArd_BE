package com.pard.pree_be.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class TranscriptionProcessor {

    public int countFillers(String text) {
        String[] fillers = {"음", "아", "어", "흠"};
        int count = 0;
        for (String filler : fillers) {
            count += text.split(filler, -1).length - 1;
        }
        return count;
    }

    public int countBlanks(String transcriptionJson, double blankThreshold) {
        int blankCount = 0;
        JSONObject json = new JSONObject(transcriptionJson);
        JSONArray results = json.getJSONObject("results").getJSONArray("items");

        double lastEndTime = -1;  // Track the end time of the last word
        for (int i = 0; i < results.length(); i++) {
            JSONObject wordInfo = results.getJSONObject(i);
            if (wordInfo.has("start_time") && wordInfo.has("end_time")) {
                double startTime = wordInfo.getDouble("start_time");
                double endTime = wordInfo.getDouble("end_time");

                // Check for gaps greater than blankThreshold
                if (lastEndTime != -1 && (startTime - lastEndTime) > blankThreshold) {
                    blankCount++;
                }
                lastEndTime = endTime;
            }
        }

        return blankCount;
    }

    public double calculateSpeechSpeed(String transcriptionJson, double duration) {
        int syllableCount = countSyllables(transcriptionJson);
        return (syllableCount / duration) * 60; // Syllables per minute
    }

    private int countSyllables(String transcriptionJson) {
        JSONObject json = new JSONObject(transcriptionJson);
        JSONArray results = json.getJSONObject("results").getJSONArray("items");
        int syllableCount = 0;

        for (int i = 0; i < results.length(); i++) {
            JSONObject wordInfo = results.getJSONObject(i);
            if (wordInfo.has("alternatives")) {
                // Assuming that each word in the transcription is a syllable for simplicity
                syllableCount += wordInfo.getJSONArray("alternatives").getJSONObject(0).getString("content").length();
            }
        }

        return syllableCount;
    }
}
