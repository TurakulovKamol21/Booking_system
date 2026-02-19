package com.autoguide.backend.repository.r2dbc;

import com.autoguide.backend.model.LandingOfferEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface LandingOfferRepository extends ReactiveCrudRepository<LandingOfferEntity, UUID> {

    Flux<LandingOfferEntity> findAllByOrderBySortOrderAsc();
}
