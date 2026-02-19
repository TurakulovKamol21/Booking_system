package com.autoguide.backend.dto;

import com.autoguide.backend.model.BookingStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        UUID guestId,
        String guestFullName,
        UUID roomId,
        String roomNumber,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        BookingStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
