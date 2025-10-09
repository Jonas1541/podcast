package com.positivo.podcast.dtos.upload;

import jakarta.validation.constraints.NotBlank;

public record PodcastUploadDto(
    @NotBlank String titulo,
    @NotBlank String descricao
) {}
