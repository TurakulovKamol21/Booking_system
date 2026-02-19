package com.autoguide.backend.dto;

import java.util.UUID;

public record PublicOfferResponse(
        UUID id,
        String title,
        String note,
        String priceLabel
) {
}
