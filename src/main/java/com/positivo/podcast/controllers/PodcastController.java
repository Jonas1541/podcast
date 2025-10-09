package com.positivo.podcast.controllers;

import com.positivo.podcast.dtos.request.PodcastRequestDto;
import com.positivo.podcast.dtos.response.PodcastResponseDto;
import com.positivo.podcast.dtos.upload.PodcastUploadDto;
import com.positivo.podcast.services.PodcastService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/podcasts")
public class PodcastController {

    @Autowired
    private PodcastService podcastService;

    @GetMapping
    public ResponseEntity<List<PodcastResponseDto>> findAll() {
        List<PodcastResponseDto> list = podcastService.findAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PodcastResponseDto> findById(@PathVariable Long id) {
        PodcastResponseDto dto = podcastService.findById(id);
        return ResponseEntity.ok(dto);
    }
    
    @PostMapping
    public ResponseEntity<PodcastResponseDto> create(@RequestBody @Valid PodcastRequestDto createDto) {
        PodcastResponseDto createdDto = podcastService.create(createDto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(createdDto.id()).toUri();
        return ResponseEntity.created(uri).body(createdDto);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PodcastResponseDto> createWithUpload(
            @RequestPart("dados") @Valid PodcastUploadDto dto,
            @RequestPart("audio") MultipartFile audio,
            @RequestPart(value = "capa", required = false) MultipartFile capa) {

        PodcastResponseDto createdDto = podcastService.createWithUpload(dto, audio, capa);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(createdDto.id()).toUri();
        return ResponseEntity.created(uri).body(createdDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PodcastResponseDto> update(@PathVariable Long id, @RequestBody @Valid PodcastRequestDto updateDto) {
        PodcastResponseDto updatedDto = podcastService.update(id, updateDto);
        return ResponseEntity.ok(updatedDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        podcastService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
