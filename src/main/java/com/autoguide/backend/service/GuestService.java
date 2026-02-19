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
    private final HotelAccessService hotelAccessService;

    public GuestService(GuestRepository guestRepository, HotelAccessService hotelAccessService) {
        this.guestRepository = guestRepository;
        this.hotelAccessService = hotelAccessService;
    }

    public Mono<GuestResponse> create(CreateGuestRequest request) {
        return hotelAccessService.currentScope()
                .flatMap(scope -> hotelAccessService.resolveHotelForWrite(scope, request.hotelId()))
                .flatMap(hotelId -> {
                    GuestEntity entity = new GuestEntity();
                    entity.setHotelId(hotelId);
                    entity.setFullName(request.fullName());
                    entity.setEmail(request.email());
                    entity.setCreatedAt(Instant.now());

                    return guestRepository.save(entity).map(this::toResponse);
                });
    }

    public Mono<GuestEntity> getEntityById(UUID id) {
        return hotelAccessService.currentScope()
                .flatMap(scope -> getEntityById(id, scope));
    }

    Mono<GuestEntity> getEntityById(UUID id, HotelAccessService.AccessScope scope) {
        Mono<GuestEntity> source = scope.superAdmin()
                ? guestRepository.findById(id)
                : guestRepository.findByIdAndHotelId(id, scope.requiredHotelId());

        return source.switchIfEmpty(Mono.error(new NotFoundException("Guest not found: " + id)));
    }

    public Mono<GuestResponse> getById(UUID id) {
        return getEntityById(id).map(this::toResponse);
    }

    public Flux<GuestResponse> getAll() {
        return hotelAccessService.currentScope()
                .flatMapMany(scope -> {
                    if (scope.superAdmin()) {
                        return guestRepository.findAll();
                    }
                    return guestRepository.findAllByHotelId(scope.requiredHotelId());
                })
                .map(this::toResponse);
    }

    private GuestResponse toResponse(GuestEntity entity) {
        return new GuestResponse(
                entity.getId(),
                entity.getHotelId(),
                entity.getFullName(),
                entity.getEmail(),
                entity.getCreatedAt()
        );
    }
}
