package com.autoguide.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateRoomRequest(
        @NotBlank @Size(max = 20) String roomNumber,
        @NotBlank @Size(max = 50) String roomType,
        @NotNull @DecimalMin("0.0") BigDecimal nightlyRate,
        @Size(max = 300) String imageUrl,
        @Size(max = 280) String shortDescription,
        UUID hotelId
) {
    public CreateRoomRequest(String roomNumber, String roomType, BigDecimal nightlyRate) {
        this(roomNumber, roomType, nightlyRate, null, null, null);
    }
}
