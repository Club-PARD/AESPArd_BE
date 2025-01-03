package com.pard.pree_be.utils;

import com.amazonaws.services.transcribe.AmazonTranscribe;
import com.amazonaws.services.transcribe.model.*;
import org.springframework.stereotype.Service;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

@Service
public class TranscriptionService {

    private final AmazonTranscribe amazonTranscribe;

    public TranscriptionService(AmazonTranscribe amazonTranscribe) {
        this.amazonTranscribe = amazonTranscribe;
    }

    public String transcribeAudio(String bucketName, String s3Uri) {
        String jobName = "TranscriptionJob-" + UUID.randomUUID();

        StartTranscriptionJobRequest request = new StartTranscriptionJobRequest()
                .withTranscriptionJobName(jobName)
                .withMedia(new Media().withMediaFileUri(s3Uri))
                .withMediaFormat(MediaFormat.Wav) // Ensure correct format
                .withLanguageCode("ko-KR");

        try {
            // Start transcription job
            amazonTranscribe.startTranscriptionJob(request);

            // Check job status
            GetTranscriptionJobRequest getRequest = new GetTranscriptionJobRequest()
                    .withTranscriptionJobName(jobName);

            TranscriptionJob job;
            do {
                job = amazonTranscribe.getTranscriptionJob(getRequest).getTranscriptionJob();
                String status = job.getTranscriptionJobStatus();

                if (TranscriptionJobStatus.COMPLETED.toString().equals(status)) {
                    String transcriptUri = job.getTranscript().getTranscriptFileUri();
                    return fetchTranscript(transcriptUri);
                } else if (TranscriptionJobStatus.FAILED.toString().equals(status)) {
                    throw new RuntimeException("Transcription failed: " + job.getFailureReason());
                }

                Thread.sleep(5000); // Poll every 5 seconds
            } while (TranscriptionJobStatus.IN_PROGRESS.toString().equals(job.getTranscriptionJobStatus()));

            throw new RuntimeException("Transcription job did not complete.");
        } catch (Exception e) {
            throw new RuntimeException("Error during transcription: " + e.getMessage(), e);
        }
    }


    private String fetchTranscript(String transcriptUri) {
        try {
            URL url = new URL(transcriptUri);
            InputStream inputStream = url.openStream();
            return IOUtils.toString(inputStream, "UTF-8"); // Return the transcription JSON
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch transcript: " + e.getMessage(), e);
        }
    }
}
