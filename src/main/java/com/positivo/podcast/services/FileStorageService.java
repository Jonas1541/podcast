package com.positivo.podcast.services;

import com.positivo.podcast.exceptions.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class FileStorageService {

    @Autowired
    private S3Client s3Client;

    @Value("${minio.url.public}")
    private String minioPublicUrl;

    public String upload(MultipartFile file, String bucketName) {
        try {
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            
            // Cria a requisição de upload para o S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            // Envia o arquivo para o MinIO
            s3Client.putObject(putObjectRequest, 
                    RequestBody.fromBytes(file.getBytes()));

            // Retorna a URL pública completa do arquivo
            return String.format("%s/%s/%s", minioPublicUrl, bucketName, fileName);

        } catch (IOException e) {
            throw new FileUploadException("Falha ao ler os bytes do arquivo: " + e.getMessage());
        } catch (Exception e) {
            throw new FileUploadException("Falha no upload para o MinIO: " + e.getMessage());
        }
    }

    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        try {
            String bucketName = getBucketNameFromUrl(fileUrl);
            String fileName = getFileNameFromUrl(fileUrl);

            // Cria a requisição de deleção
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            // Envia o comando de deleção
            s3Client.deleteObject(deleteObjectRequest);

        } catch (Exception e) {
            System.err.println("AVISO: Falha ao deletar arquivo do MinIO (pode já ter sido removido): " + fileUrl);
            System.err.println("Erro: " + e.getMessage());
        }
    }

    private String generateUniqueFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    private String getBucketNameFromUrl(String fileUrl) {
        // Ex: http://localhost:9000/audios/uuid.mp3 -> extrai "audios"
        String[] parts = fileUrl.split("/");
        return parts[parts.length - 2];
    }

    private String getFileNameFromUrl(String fileUrl) {
        // Ex: http://localhost:9000/audios/uuid.mp3 -> extrai "uuid.mp3"
        String[] parts = fileUrl.split("/");
        return parts[parts.length - 1];
    }
}
