package com.positivo.podcast.services;

import com.positivo.podcast.exceptions.FileUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key.service}")
    private String supabaseServiceKey;

    // RestTemplate é um cliente HTTP síncrono do Spring.
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Faz o upload de um arquivo para um bucket no Supabase Storage.
     * @param file O arquivo a ser enviado.
     * @param bucketName O nome do bucket de destino (ex: "audios", "capas").
     * @return A URL pública e permanente do arquivo que foi salvo.
     */
    public String upload(MultipartFile file, String bucketName) {
        try {
            // Gera um nome de arquivo único para evitar colisões
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            
            // Monta a URL da API do Supabase Storage para upload
            String url = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucketName, fileName);

            // Configura os cabeçalhos da requisição HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(supabaseServiceKey); // Usa a chave de serviço para autorização
            headers.setContentType(MediaType.valueOf(Objects.requireNonNull(file.getContentType())));
            headers.add("x-upsert", "true"); // Opção para sobrescrever se o arquivo já existir

            // Cria a entidade da requisição com os bytes do arquivo e os cabeçalhos
            HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

            // Envia a requisição POST para a API do Supabase
            restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            
            // Monta e retorna a URL pública do arquivo para ser salva no banco de dados
            return String.format("%s/storage/v1/object/public/%s/%s", supabaseUrl, bucketName, fileName);

        } catch (IOException e) {
            throw new FileUploadException("Falha ao ler os bytes do arquivo: " + e.getMessage());
        } catch (Exception e) {
            // Captura qualquer erro de rede ou de API
            throw new FileUploadException("Falha no upload para o Supabase Storage: " + e.getMessage());
        }
    }

    /**
     * Deleta um arquivo do Supabase Storage com base em sua URL pública.
     * @param fileUrl A URL completa do arquivo a ser deletado.
     */
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return; // Não faz nada se a URL for nula ou vazia
        }
        try {
            // Extrai o nome do bucket e o nome do arquivo da URL
            String bucketName = getBucketNameFromUrl(fileUrl);
            String fileName = getFileNameFromUrl(fileUrl);
            
            // Monta a URL da API para deleção
            String url = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucketName, fileName);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(supabaseServiceKey);
            
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            // Envia a requisição DELETE para a API do Supabase
            restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, String.class);
            
        } catch (Exception e) {
            // Logamos o erro mas não lançamos uma exceção para não impedir
            // a deleção do registro do banco caso o arquivo já não exista no storage.
            System.err.println("AVISO: Falha ao deletar arquivo do Supabase (pode já ter sido removido): " + fileUrl);
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
        // Ex: .../public/podcasts/uuid.mp3 -> extrai "podcasts"
        String[] parts = fileUrl.split("/");
        return parts[parts.length - 2];
    }

    private String getFileNameFromUrl(String fileUrl) {
        // Ex: .../public/podcasts/uuid.mp3 -> extrai "uuid.mp3"
        String[] parts = fileUrl.split("/");
        return parts[parts.length - 1];
    }
}
