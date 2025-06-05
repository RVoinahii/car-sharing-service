package com.carshare.rentalsystem.service.aws.s3;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@RequiredArgsConstructor
@Service
public class AwsS3ClientProvider {
    @Value("${aws.access-key}")
    private String awsAccessKey;

    @Value("${aws.secret-access-key}")
    private String awsSecretKey;

    @Value("${aws.region}")
    private String awsRegionName;

    @Value("${aws.s3-bucket-name}")
    private String awsS3BucketName;

    @Getter
    private S3Client s3Client;

    private AwsBasicCredentials credentials;

    @PostConstruct
    public void init() {
        credentials = AwsBasicCredentials.create(awsAccessKey, awsSecretKey);
        this.s3Client = S3Client
                .builder()
                .region(Region.of(awsRegionName))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    public String getBucketName() {
        return awsS3BucketName;
    }

    public S3Presigner getS3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(awsRegionName))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}
