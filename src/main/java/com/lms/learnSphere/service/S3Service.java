package com.lms.learnSphere.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.UUID;

@Service
public class S3Service {
    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public S3Service(@Value("${aws.s3.region}") String regionValue,
                     @Value("${aws.s3.endpoint-override:#{null}}") String endpointOverride) {
        // Initialize S3Client builder
        S3ClientBuilder clientBuilder = S3Client.builder()
                .region(Region.of(regionValue))
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create());

        // Initialize S3Presigner builder
        S3Presigner.Builder presignerBuilder = S3Presigner.builder()
                .region(Region.of(regionValue))
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create());

        // Configure endpoint override if provided
        if (endpointOverride != null && !endpointOverride.isEmpty()) {
            try {
                URI uri = new URI(endpointOverride);
                clientBuilder.endpointOverride(uri);
                presignerBuilder.endpointOverride(uri);
            } catch (URISyntaxException e) {
                logger.error("Invalid S3 endpoint override URI: {}", endpointOverride, e);
                throw new RuntimeException("Invalid S3 endpoint override URI", e);
            }
        }

        this.s3Client = clientBuilder.build();
        this.s3Presigner = presignerBuilder.build();
    }

    public String uploadFile(MultipartFile file, String courseIdPathSegment, String lessonIdPathSegment) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        String originalFileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String fileExtension = originalFileName.contains(".")
                ? originalFileName.substring(originalFileName.lastIndexOf('.'))
                : "";

        // Sanitize path segments
        String safeCourseId = courseIdPathSegment.replaceAll("[^a-zA-Z0-9-_]", "");
        String safeLessonId = lessonIdPathSegment.replaceAll("[^a-zA-Z0-9-_]", "");

        String objectKey = String.format("courses/%s/lessons/%s/%s%s",
                safeCourseId, safeLessonId, UUID.randomUUID(), fileExtension);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            logger.info("File uploaded successfully to S3: {}", objectKey);
            return objectKey;
        } catch (S3Exception e) {
            logger.error("Error uploading file to S3: {}", e.getMessage(), e);
            throw new IOException("Failed to upload file to S3", e);
        }
    }

    public String getPresignedUrl(String objectKey) {
        if (objectKey == null || objectKey.isEmpty()) {
            logger.warn("Requested presigned URL for null or empty objectKey");
            return null;
        }

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(
                    GetObjectPresignRequest.builder()
                            .signatureDuration(Duration.ofMinutes(60))
                            .getObjectRequest(getObjectRequest)
                            .build());

            logger.info("Generated presigned URL for key: {}", objectKey);
            return presignedRequest.url().toString();
        } catch (S3Exception e) {
            logger.error("Error generating presigned URL for key {}: {}", objectKey, e.getMessage(), e);
            return null;
        }
    }

    public void deleteFile(String objectKey) {
        if (objectKey == null || objectKey.isEmpty()) {
            logger.warn("Attempted to delete null or empty objectKey");
            return;
        }

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build());

            logger.info("Successfully deleted file from S3: {}", objectKey);
        } catch (S3Exception e) {
            logger.error("Error deleting file from S3 key {}: {}", objectKey, e.getMessage(), e);
            // Consider throwing a custom exception here if needed
        }
    }
}