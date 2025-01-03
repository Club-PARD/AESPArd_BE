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

    public double calculateDurationFromJson(String transcriptionJson) {
        JSONObject json = new JSONObject(transcriptionJson);
        JSONArray items = json.getJSONObject("results").getJSONArray("items");

        double startTime = -1;
        double endTime = -1;

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);

            // Skip items without start_time or end_time
            if (!item.has("start_time") || !item.has("end_time")) {
                continue;
            }

            // Update startTime for the first valid item
            if (startTime == -1) {
                startTime = item.getDouble("start_time");
            }

            // Update endTime for the last valid item
            endTime = item.getDouble("end_time");
        }

        if (startTime == -1 || endTime == -1) {
            throw new IllegalArgumentException("No valid pronunciation items found in transcription JSON.");
        }

        return endTime - startTime;
    }


    public double calculateSpeechSpeedFromJson(String transcriptionJson, double duration) {
        JSONObject json = new JSONObject(transcriptionJson);
        JSONArray items = json.getJSONObject("results").getJSONArray("items");

        int wordCount = 0;

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);

            // Count only pronunciation items
            if ("pronunciation".equals(item.getString("type"))) {
                wordCount++;
            }
        }

        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be greater than zero.");
        }

        return wordCount / (duration / 60.0); // Words per minute
    }


    public int countFillersFromJson(String transcriptionJson) {
        JSONObject json = new JSONObject(transcriptionJson);
        JSONArray items = json.getJSONObject("results").getJSONArray("items");

        String[] fillers = {"음", "아", "어", "흠"};
        int fillerCount = 0;

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            if ("pronunciation".equals(item.getString("type"))) {
                String content = item.getJSONArray("alternatives").getJSONObject(0).getString("content");
                for (String filler : fillers) {
                    if (content.equals(filler)) {
                        fillerCount++;
                    }
                }
            }
        }

        return fillerCount;
    }

    public int countBlanksFromJson(String transcriptionJson, double blankThreshold) {
        JSONObject json = new JSONObject(transcriptionJson);
        JSONArray items = json.getJSONObject("results").getJSONArray("items");

        int blankCount = 0;
        double lastEndTime = -1;

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);

            // Skip items without valid start_time or end_time
            if (!item.has("start_time") || !item.has("end_time")) {
                continue;
            }

            double startTime = item.getDouble("start_time");
            double endTime = item.getDouble("end_time");

            if (lastEndTime != -1 && (startTime - lastEndTime) > blankThreshold) {
                blankCount++;
            }

            lastEndTime = endTime;
        }

        return blankCount;
    }



}
