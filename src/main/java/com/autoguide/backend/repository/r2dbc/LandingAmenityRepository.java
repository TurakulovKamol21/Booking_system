package com.autoguide.backend.repository.r2dbc;

import com.autoguide.backend.model.LandingAmenityEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface LandingAmenityRepository extends ReactiveCrudRepository<LandingAmenityEntity, UUID> {

    Flux<LandingAmenityEntity> findAllByOrderBySortOrderAsc();
}
