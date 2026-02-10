package com.mrtob.srs.dto;

import jakarta.validation.constraints.NotBlank;

public record CardUpdateRequest(
        @NotBlank String front,
        @NotBlank String back
) {}