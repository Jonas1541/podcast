package com.positivo.podcast.controllers;

import com.positivo.podcast.dtos.request.NoticiaRequestDto;
import com.positivo.podcast.dtos.response.NoticiaResponseDto;
import com.positivo.podcast.dtos.upload.NoticiaUploadDto;
import com.positivo.podcast.services.NoticiaService;
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
@RequestMapping("/noticias")
public class NoticiaController {

    @Autowired
    private NoticiaService noticiaService;

    @GetMapping
    public ResponseEntity<List<NoticiaResponseDto>> findAll() {
        return ResponseEntity.ok(noticiaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoticiaResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(noticiaService.findById(id));
    }

    @PostMapping
    public ResponseEntity<NoticiaResponseDto> create(@RequestBody @Valid NoticiaRequestDto createDto) {
        NoticiaResponseDto createdDto = noticiaService.create(createDto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(createdDto.id()).toUri();
        return ResponseEntity.created(uri).body(createdDto);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NoticiaResponseDto> createWithUpload(
            @RequestPart("dados") @Valid NoticiaUploadDto dto,
            @RequestPart(value = "capa", required = false) MultipartFile capa) {

        NoticiaResponseDto createdDto = noticiaService.createWithUpload(dto, capa);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(createdDto.id()).toUri();
        return ResponseEntity.created(uri).body(createdDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoticiaResponseDto> update(@PathVariable Long id, @RequestBody @Valid NoticiaRequestDto updateDto) {
        return ResponseEntity.ok(noticiaService.update(id, updateDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        noticiaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
