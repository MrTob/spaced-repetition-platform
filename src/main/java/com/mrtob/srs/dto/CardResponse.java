package com.mrtob.srs.dto;

import java.time.Instant;
import java.util.UUID;

public record CardResponse(
        UUID id,
        String front,
        String back,
        Instant nextReview,
        Instant createdAt
) {
}
