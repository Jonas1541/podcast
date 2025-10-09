package com.positivo.podcast.dtos.upload;

import jakarta.validation.constraints.NotBlank;

public record NoticiaUploadDto(
    @NotBlank String titulo,
    @NotBlank String descricao
) {}
