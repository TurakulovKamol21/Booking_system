package com.autoguide.backend.controller;

import com.autoguide.backend.dto.BookingResponse;
import com.autoguide.backend.dto.CreateBookingRequest;
import com.autoguide.backend.dto.UpdateBookingStatusRequest;
import com.autoguide.backend.model.BookingStatus;
import com.autoguide.backend.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATOR')")
    public Mono<BookingResponse> create(@Valid @RequestBody CreateBookingRequest request) {
        return bookingService.create(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATOR')")
    public Mono<BookingResponse> getById(@PathVariable UUID id) {
        return bookingService.getById(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATOR')")
    public Flux<BookingResponse> getAll(@RequestParam(required = false) BookingStatus status) {
        return bookingService.getAll(status);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATOR')")
    public Mono<BookingResponse> updateStatus(@PathVariable UUID id,
                                              @Valid @RequestBody UpdateBookingStatusRequest request) {
        return bookingService.updateStatus(id, request);
    }
}
