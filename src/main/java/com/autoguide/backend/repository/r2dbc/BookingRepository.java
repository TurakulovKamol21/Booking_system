package com.autoguide.backend.repository.r2dbc;

import com.autoguide.backend.model.BookingEntity;
import com.autoguide.backend.model.BookingStatus;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.UUID;

public interface BookingRepository extends ReactiveCrudRepository<BookingEntity, UUID> {

    Flux<BookingEntity> findAllByStatus(BookingStatus status);

    Flux<BookingEntity> findAllByHotelId(UUID hotelId);

    Flux<BookingEntity> findAllByHotelIdAndStatus(UUID hotelId, BookingStatus status);

    Flux<BookingEntity> findAllByGuestIdIn(Collection<UUID> guestIds);

    Flux<BookingEntity> findAllByGuestIdInAndStatus(Collection<UUID> guestIds, BookingStatus status);

    Mono<BookingEntity> findByIdAndHotelId(UUID id, UUID hotelId);

    Mono<Long> countByHotelId(UUID hotelId);

    Mono<Boolean> existsByRoomId(UUID roomId);
}
