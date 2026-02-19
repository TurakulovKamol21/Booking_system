package com.autoguide.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateHotelRequest(
        @NotBlank @Size(max = 40) String code,
        @NotBlank @Size(max = 140) String name,
        @NotBlank @Size(max = 80) String city,
        @NotBlank @Size(max = 80) String country,
        @NotBlank @Size(max = 200) String addressLine,
        @Size(max = 400) String imageUrl
) {
}
