package com.autoguide.backend.dto;

import java.time.Instant;
import java.util.UUID;

public record GuestResponse(
        UUID id,
        UUID hotelId,
        String fullName,
        String email,
        Instant createdAt
) {
}
