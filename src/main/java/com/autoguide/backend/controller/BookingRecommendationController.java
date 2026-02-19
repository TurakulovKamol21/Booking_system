package com.autoguide.backend.controller;

import com.autoguide.backend.dto.BookingRecommendationResponse;
import com.autoguide.backend.service.BookingRecommendationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/booking-recommendations")
public class BookingRecommendationController {

    private final BookingRecommendationService bookingRecommendationService;

    public BookingRecommendationController(BookingRecommendationService bookingRecommendationService) {
        this.bookingRecommendationService = bookingRecommendationService;
    }

    @GetMapping("/{bookingId}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public Flux<BookingRecommendationResponse> getByBookingId(@PathVariable UUID bookingId) {
        return bookingRecommendationService.getByBookingId(bookingId);
    }
}
