package com.pard.pree_be.utils;

import com.amazonaws.services.transcribe.AmazonTranscribe;
import com.amazonaws.services.transcribe.model.*;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class TranscriptionService {

    private final AmazonTranscribe amazonTranscribe;

    public TranscriptionService(AmazonTranscribe amazonTranscribe) {
        this.amazonTranscribe = amazonTranscribe;
    }

    public String transcribeAudio(String bucketName, String audioFileKey) {
        String jobName = "TranscriptionJob-" + UUID.randomUUID();

        // Create transcription job request with Korean language code as a string
        StartTranscriptionJobRequest request = new StartTranscriptionJobRequest()
                .withTranscriptionJobName(jobName)
                .withMedia(new Media().withMediaFileUri("s3://" + bucketName + "/" + audioFileKey))
                .withMediaFormat(MediaFormat.Wav)
                .withLanguageCode("ko-KR"); // Set to Korean (ko-KR) as a string

        // Start transcription job
        amazonTranscribe.startTranscriptionJob(request);

        // Create a request to check job status
        GetTranscriptionJobRequest getRequest = new GetTranscriptionJobRequest()
                .withTranscriptionJobName(jobName);

        TranscriptionJob job;
        do {
            job = amazonTranscribe.getTranscriptionJob(getRequest).getTranscriptionJob();

            // Get the transcription job status
            String status = job.getTranscriptionJobStatus();

            if (TranscriptionJobStatus.COMPLETED.toString().equals(status)) {
                // If completed, fetch the transcript URI
                String transcriptUri = job.getTranscript().getTranscriptFileUri();
                return fetchTranscript(transcriptUri);  // You need to implement this method
            } else if (TranscriptionJobStatus.FAILED.toString().equals(status)) {
                // If failed, provide reason
                throw new RuntimeException("Transcription failed: " + job.getFailureReason());
            }

            try {
                Thread.sleep(5000); // Wait for transcription job to finish
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (TranscriptionJobStatus.IN_PROGRESS.toString().equals(job.getTranscriptionJobStatus()));

        // Fallback: If status isn't completed or failed, return an error message
        throw new RuntimeException("Transcription job did not complete successfully.");
    }

    private String fetchTranscript(String transcriptUri) {
        // Implement fetching the transcript from the URL
        return ""; // Placeholder: You can use an HTTP client like RestTemplate to download the transcript.
    }
}
