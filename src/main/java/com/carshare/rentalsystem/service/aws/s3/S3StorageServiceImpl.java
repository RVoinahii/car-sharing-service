package com.carshare.rentalsystem.service.aws.s3;

import com.carshare.rentalsystem.exception.InvalidMediaFileException;
import com.carshare.rentalsystem.exception.S3DeleteException;
import com.carshare.rentalsystem.exception.S3UploadException;
import com.carshare.rentalsystem.exception.TooLargeMediaFileException;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@RequiredArgsConstructor
@Service
public class S3StorageServiceImpl implements S3StorageService {
    private static final List<String> ALLOWED_MIME_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "video/mp4"
    );

    private static final long MAX_FILE_SIZE_BYTES = 50 * 1024 * 1024;

    private final AwsS3ClientProvider s3ClientProvider;

    @Override
    public List<String> uploadMedia(Long rentId, List<MultipartFile> files) {
        String bucket = s3ClientProvider.getBucketName();
        List<String> uploadedKeys = new ArrayList<>();

        for (MultipartFile file : files) {
            validateFile(file);

            String extension = getFileExtension(file.getOriginalFilename());
            String key = generateS3Key(rentId, extension);

            uploadToS3(bucket, key, file);
            uploadedKeys.add(key);
        }

        return uploadedKeys;
    }

    @Override
    public List<String> getMedia(List<String> keys) {
        return keys.stream()
                .map(this::generatePresignedUrl)
                .toList();
    }

    @Override
    public void deleteMedia(List<String> s3Keys) {
        if (s3Keys == null || s3Keys.isEmpty()) {
            return;
        }

        String bucket = s3ClientProvider.getBucketName();

        List<ObjectIdentifier> objectsToDelete = s3Keys.stream()
                .map(key -> ObjectIdentifier.builder().key(key).build())
                .toList();

        try {
            s3ClientProvider.getS3Client().deleteObjects(
                    DeleteObjectsRequest.builder()
                            .bucket(bucket)
                            .delete(Delete.builder().objects(objectsToDelete).build())
                            .build()
            );
        } catch (SdkException e) {
            throw new S3DeleteException("Unable to delete media files from S3.");
        }
    }

    private void validateFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new InvalidMediaFileException("Unsupported media type: " + contentType);
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new TooLargeMediaFileException("File size exceeds 50MB limit.");
        }
    }

    private String getFileExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(f.lastIndexOf('.') + 1))
                .orElse("bin");
    }

    private String generateS3Key(Long rentId, String extension) {
        return "returns/" + rentId + "/" + UUID.randomUUID() + "." + extension;
    }

    private void uploadToS3(String bucket, String key, MultipartFile file) {
        try {
            s3ClientProvider.getS3Client().putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );
        } catch (IOException e) {
            throw new S3UploadException(
                    "Unable to upload media file to S3. Please try again later.");
        }
    }

    private String generatePresignedUrl(String key) {
        S3Presigner presigner = s3ClientProvider.getS3Presigner();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3ClientProvider.getBucketName())
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(getObjectRequest)
                .signatureDuration(Duration.ofMinutes(10))
                .build();

        return presigner.presignGetObject(presignRequest).url().toString();
    }
}
