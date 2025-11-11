package com.positivo.podcast.services;

import com.positivo.podcast.dtos.request.PodcastRequestDto;
import com.positivo.podcast.dtos.response.PodcastResponseDto;
import com.positivo.podcast.dtos.upload.PodcastUploadDto;
import com.positivo.podcast.entities.Podcast;
import com.positivo.podcast.exceptions.ResourceNotFoundException;
import com.positivo.podcast.repositories.PodcastRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // IMPORTAR @Value
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PodcastService {

    @Autowired
    private PodcastRepository podcastRepository;

    @Autowired
    private FileStorageService fileStorageService;

    // INJETA OS NOMES DOS BUCKETS
    @Value("${minio.bucket.audios}")
    private String audioBucket;

    @Value("${minio.bucket.capas}")
    private String capaBucket;

    @Transactional(readOnly = true)
    public List<PodcastResponseDto> findAll() {
        return podcastRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PodcastResponseDto findById(Long id) {
        Podcast podcast = podcastRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Podcast não encontrado com o id: " + id));
        return toDto(podcast);
    }

    @Transactional
    public PodcastResponseDto create(PodcastRequestDto podcastDto) {
        Podcast podcast = new Podcast();
        podcast.setTitulo(podcastDto.titulo());
        podcast.setDescricao(podcastDto.descricao());
        podcast.setCapaUrl(podcastDto.capaUrl());
        podcast.setAudioUrl(podcastDto.audioUrl());

        Podcast savedPodcast = podcastRepository.save(podcast);
        return toDto(savedPodcast);
    }

    public PodcastResponseDto createWithUpload(PodcastUploadDto dto, MultipartFile audio, MultipartFile capa) {
        // USA AS VARIÁVEIS INJETADAS
        String audioUrl = fileStorageService.upload(audio, audioBucket);
        String capaUrl = (capa != null && !capa.isEmpty())
                ? fileStorageService.upload(capa, capaBucket)
                : null;

        Podcast podcast = new Podcast();
        podcast.setTitulo(dto.titulo());
        podcast.setDescricao(dto.descricao());
        podcast.setAudioUrl(audioUrl);
        podcast.setCapaUrl(capaUrl);

        Podcast savedPodcast = podcastRepository.save(podcast);
        return toDto(savedPodcast);
    }

    @Transactional
    public PodcastResponseDto update(Long id, PodcastRequestDto podcastDto) {
        Podcast podcast = podcastRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Podcast não encontrado com o id: " + id));

        podcast.setTitulo(podcastDto.titulo());
        podcast.setDescricao(podcastDto.descricao());
        podcast.setCapaUrl(podcastDto.capaUrl());
        podcast.setAudioUrl(podcastDto.audioUrl());

        Podcast updatedPodcast = podcastRepository.save(podcast);
        return toDto(updatedPodcast);
    }

    @Transactional
    public void delete(Long id) {
        // OTIMIZAÇÃO: Busca o podcast uma única vez
        Podcast podcast = podcastRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Podcast não encontrado com o id: " + id));
        
        // Deleta os arquivos do storage
        fileStorageService.delete(podcast.getAudioUrl());
        fileStorageService.delete(podcast.getCapaUrl());

        // Deleta a entidade do banco
        podcastRepository.delete(podcast);
    }

    private PodcastResponseDto toDto(Podcast podcast) {
        return new PodcastResponseDto(
                podcast.getId(),
                podcast.getTitulo(),
                podcast.getDescricao(),
                podcast.getCapaUrl(),
                podcast.getAudioUrl());
    }
}
