package com.autoguide.backend.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateBookingRequest(
        @NotNull UUID guestId,
        @NotNull UUID roomId,
        @NotNull LocalDate checkInDate,
        @NotNull LocalDate checkOutDate
) {
}
