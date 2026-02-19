package com.autoguide.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateRoomRequest(
        @NotBlank @Size(max = 20) String roomNumber,
        @NotBlank @Size(max = 50) String roomType,
        @NotNull @DecimalMin("0.0") BigDecimal nightlyRate,
        @Size(max = 280) String shortDescription
) {
}
