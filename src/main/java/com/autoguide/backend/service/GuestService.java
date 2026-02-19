package com.autoguide.backend.service;

import com.autoguide.backend.dto.CreateGuestRequest;
import com.autoguide.backend.dto.GuestResponse;
import com.autoguide.backend.exception.NotFoundException;
import com.autoguide.backend.model.GuestEntity;
import com.autoguide.backend.repository.r2dbc.GuestRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
public class GuestService {

    private final GuestRepository guestRepository;

    public GuestService(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    public Mono<GuestResponse> create(CreateGuestRequest request) {
        GuestEntity entity = new GuestEntity();
        entity.setFullName(request.fullName());
        entity.setEmail(request.email());
        entity.setCreatedAt(Instant.now());

        return guestRepository.save(entity)
                .map(this::toResponse);
    }

    public Mono<GuestEntity> getEntityById(UUID id) {
        return guestRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Guest not found: " + id)));
    }

    public Mono<GuestResponse> getById(UUID id) {
        return getEntityById(id).map(this::toResponse);
    }

    public Flux<GuestResponse> getAll() {
        return guestRepository.findAll().map(this::toResponse);
    }

    private GuestResponse toResponse(GuestEntity entity) {
        return new GuestResponse(
                entity.getId(),
                entity.getFullName(),
                entity.getEmail(),
                entity.getCreatedAt()
        );
    }
}
