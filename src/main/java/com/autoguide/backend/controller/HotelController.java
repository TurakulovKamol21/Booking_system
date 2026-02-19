package com.autoguide.backend.controller;

import com.autoguide.backend.dto.CreateHotelRequest;
import com.autoguide.backend.dto.HotelResponse;
import com.autoguide.backend.dto.UpdateHotelRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import com.autoguide.backend.service.HotelService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hotels")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATOR')")
    public Flux<HotelResponse> getAll() {
        return hotelService.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Mono<HotelResponse> create(@Valid @RequestBody CreateHotelRequest request) {
        return hotelService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Mono<HotelResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateHotelRequest request) {
        return hotelService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Mono<Void> delete(@PathVariable UUID id) {
        return hotelService.delete(id);
    }
}
