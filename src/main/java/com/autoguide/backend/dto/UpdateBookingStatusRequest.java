package com.autoguide.backend.dto;

import com.autoguide.backend.model.BookingStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateBookingStatusRequest(
        @NotNull BookingStatus status
) {
}
