package com.pard.pree_be.utils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile multipartFile, String dirName, Long practiceId) throws IOException {
        String fileName = dirName + "/" + practiceId + "-" + multipartFile.getOriginalFilename();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        try (InputStream inputStream = new BufferedInputStream(multipartFile.getInputStream())) {
            amazonS3.putObject(bucket, fileName, inputStream, metadata);
        }

        // Return the correct URL
        return amazonS3.getUrl(bucket, fileName).toString();
    }




    public void deleteFile(String fileName) {
        amazonS3.deleteObject(bucket, fileName);
    }
}
