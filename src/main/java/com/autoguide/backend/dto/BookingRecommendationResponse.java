package com.autoguide.backend.dto;

import java.time.Instant;
import java.util.UUID;

public record BookingRecommendationResponse(
        String id,
        UUID bookingId,
        String suggestion,
        String model,
        double confidence,
        Instant generatedAt
) {
}
