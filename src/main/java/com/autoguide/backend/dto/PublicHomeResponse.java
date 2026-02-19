package com.autoguide.backend.dto;

import java.util.List;

public record PublicHomeResponse(
        String heroTitle,
        String heroSubtitle,
        String heroImageUrl,
        List<PublicAmenityResponse> amenities,
        List<PublicOfferResponse> offers
) {
}
