package com.autoguide.backend.repository.r2dbc;

import com.autoguide.backend.model.HotelUserScopeEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface HotelUserScopeRepository extends ReactiveCrudRepository<HotelUserScopeEntity, String> {

    Mono<Boolean> existsByHotelId(UUID hotelId);
}
