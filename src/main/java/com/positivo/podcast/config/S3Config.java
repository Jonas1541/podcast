package com.positivo.podcast.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${minio.endpoint}")
    private String minioEndpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Bean
    public S3Client s3Client() {
        // A "Region" é obrigatória, mas para MinIO self-hosted, qualquer uma serve
        Region region = Region.US_EAST_1; 

        return S3Client.builder()
                .region(region)
                .endpointOverride(URI.create(minioEndpoint)) // Aponta para o nosso MinIO
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                // Habilita "path-style access", necessário para MinIO
                .forcePathStyle(true) 
                .build();
    }
}