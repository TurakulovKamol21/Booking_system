package com.autoguide.backend.dto;

import com.autoguide.backend.model.BookingStatus;
import com.autoguide.backend.model.BookingPaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        UUID hotelId,
        UUID guestId,
        String guestFullName,
        UUID roomId,
        String roomNumber,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        BookingStatus status,
        BigDecimal totalAmount,
        BigDecimal prepaymentAmount,
        BookingPaymentStatus paymentStatus,
        String paymentMethod,
        String paymentReference,
        boolean prepaid,
        Instant createdAt,
        Instant updatedAt
) {
}
