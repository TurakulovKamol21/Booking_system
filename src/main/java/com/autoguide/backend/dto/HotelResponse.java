package com.autoguide.backend.dto;

import java.time.Instant;
import java.util.UUID;

public record HotelResponse(
        UUID id,
        String code,
        String name,
        String city,
        String country,
        String addressLine,
        String imageUrl,
        Instant createdAt
) {
}
