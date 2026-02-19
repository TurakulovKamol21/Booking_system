package com.autoguide.backend.repository.r2dbc;

import com.autoguide.backend.model.BookingEntity;
import com.autoguide.backend.model.BookingStatus;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface BookingRepository extends ReactiveCrudRepository<BookingEntity, UUID> {

    Flux<BookingEntity> findAllByStatus(BookingStatus status);
}
