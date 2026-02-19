package com.autoguide.backend.repository.r2dbc;

import com.autoguide.backend.model.RoomEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RoomRepository extends ReactiveCrudRepository<RoomEntity, UUID> {

    Flux<RoomEntity> findAllByHotelId(UUID hotelId);

    Mono<RoomEntity> findByIdAndHotelId(UUID id, UUID hotelId);

    Mono<Long> countByHotelId(UUID hotelId);
}
