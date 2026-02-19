package com.autoguide.backend.dto;

import java.util.UUID;

public record PublicAmenityResponse(
        UUID id,
        String title,
        String description
) {
}
