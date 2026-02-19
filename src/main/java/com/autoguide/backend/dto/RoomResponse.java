package com.autoguide.backend.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RoomResponse(
        UUID id,
        String roomNumber,
        String roomType,
        BigDecimal nightlyRate,
        Instant createdAt
) {
}
