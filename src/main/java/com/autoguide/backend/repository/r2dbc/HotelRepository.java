package com.autoguide.backend.repository.r2dbc;

import com.autoguide.backend.model.HotelEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface HotelRepository extends ReactiveCrudRepository<HotelEntity, UUID> {

    Flux<HotelEntity> findAllByOrderByNameAsc();
}
