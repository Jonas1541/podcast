package com.positivo.podcast.services;

import com.positivo.podcast.dtos.request.NoticiaRequestDto;
import com.positivo.podcast.dtos.response.NoticiaResponseDto;
import com.positivo.podcast.dtos.upload.NoticiaUploadDto;
import com.positivo.podcast.entities.Noticia;
import com.positivo.podcast.exceptions.ResourceNotFoundException;
import com.positivo.podcast.repositories.NoticiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoticiaService {

    @Autowired
    private NoticiaRepository noticiaRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<NoticiaResponseDto> findAll() {
        return noticiaRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NoticiaResponseDto findById(Long id) {
        Noticia noticia = noticiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notícia não encontrada com o id: " + id));
        return toDto(noticia);
    }

    @Transactional
    public NoticiaResponseDto create(NoticiaRequestDto noticiaDto) {
        Noticia noticia = new Noticia();
        noticia.setTitulo(noticiaDto.titulo());
        noticia.setDescricao(noticiaDto.descricao());
        noticia.setCapaUrl(noticiaDto.capaUrl());

        Noticia savedNoticia = noticiaRepository.save(noticia);
        return toDto(savedNoticia);
    }

    public NoticiaResponseDto createWithUpload(NoticiaUploadDto dto, MultipartFile capa) {
        String capaUrl = (capa != null && !capa.isEmpty())
                ? fileStorageService.upload(capa, "capas-noticias") // bucket
                : null;

        Noticia noticia = new Noticia();
        noticia.setTitulo(dto.titulo());
        noticia.setDescricao(dto.descricao()); // Ajustado para 'descricao'
        noticia.setCapaUrl(capaUrl);

        Noticia savedNoticia = noticiaRepository.save(noticia);
        return toDto(savedNoticia);
    }

    @Transactional
    public NoticiaResponseDto update(Long id, NoticiaRequestDto noticiaDto) {
        Noticia noticia = noticiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notícia não encontrada com o id: " + id));

        noticia.setTitulo(noticiaDto.titulo());
        noticia.setDescricao(noticiaDto.descricao());
        noticia.setCapaUrl(noticiaDto.capaUrl());

        Noticia updatedNoticia = noticiaRepository.save(noticia);
        return toDto(updatedNoticia);
    }

    @Transactional
    public void delete(Long id) {
        Noticia noticia = noticiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notícia não encontrada com o id: " + id));

        fileStorageService.delete(noticia.getCapaUrl());

        noticiaRepository.deleteById(id);
    }

    private NoticiaResponseDto toDto(Noticia noticia) {
        return new NoticiaResponseDto(
                noticia.getId(),
                noticia.getTitulo(),
                noticia.getDescricao(),
                noticia.getCapaUrl());
    }
}
