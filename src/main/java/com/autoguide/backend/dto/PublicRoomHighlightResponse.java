package com.autoguide.backend.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PublicRoomHighlightResponse(
        UUID id,
        UUID hotelId,
        String roomNumber,
        String roomType,
        BigDecimal nightlyRate,
        String imageUrl,
        String shortDescription
) {
}
