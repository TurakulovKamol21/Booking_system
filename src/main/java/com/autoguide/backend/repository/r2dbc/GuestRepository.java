package com.autoguide.backend.repository.r2dbc;

import com.autoguide.backend.model.GuestEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GuestRepository extends ReactiveCrudRepository<GuestEntity, UUID> {

    Flux<GuestEntity> findAllByHotelId(UUID hotelId);

    Flux<GuestEntity> findAllByEmail(String email);

    Mono<GuestEntity> findByIdAndHotelId(UUID id, UUID hotelId);

    Mono<GuestEntity> findByHotelIdAndEmail(UUID hotelId, String email);

    Mono<Long> countByHotelId(UUID hotelId);
}
