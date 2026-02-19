package com.autoguide.backend.service;

import org.springframework.stereotype.Component;

@Component
public class RoomPresentationResolver {

    private static final String DEFAULT_IMAGE =
            "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=1600&q=80";
    private static final String DEFAULT_DESCRIPTION = "Balanced comfort with essential in-room amenities.";

    public String resolveImageUrl(String candidateImageUrl, String roomType) {
        if (hasText(candidateImageUrl)) {
            String image = candidateImageUrl.trim();
            if (image.startsWith("/") || image.startsWith("http://") || image.startsWith("https://")) {
                return image;
            }
        }
        return switch (detectTypeKey(roomType)) {
            case "suite" ->
                    "https://images.unsplash.com/photo-1445019980597-93fa8acb246c?auto=format&fit=crop&w=1600&q=80";
            case "deluxe" ->
                    "https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=1600&q=80";
            case "family" ->
                    "https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=1600&q=80";
            default -> DEFAULT_IMAGE;
        };
    }

    public String resolveShortDescription(String candidateDescription, String roomType) {
        if (hasText(candidateDescription)) {
            return candidateDescription.trim();
        }
        return switch (detectTypeKey(roomType)) {
            case "suite" -> "Premium suite with panoramic city view and extended lounge zone.";
            case "deluxe" -> "Refined interior with quiet atmosphere for business and leisure stays.";
            case "family" -> "Spacious layout designed for group travel and longer accommodation.";
            default -> DEFAULT_DESCRIPTION;
        };
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String detectTypeKey(String roomType) {
        String normalized = roomType == null ? "" : roomType.toLowerCase();

        if (normalized.contains("suite")) {
            return "suite";
        }
        if (normalized.contains("deluxe") || normalized.contains("premium")) {
            return "deluxe";
        }
        if (normalized.contains("family")) {
            return "family";
        }
        return "default";
    }
}
