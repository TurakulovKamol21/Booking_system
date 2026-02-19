package com.autoguide.backend.repository.mongo;

import com.autoguide.backend.model.BookingRecommendationDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface BookingRecommendationRepository extends ReactiveMongoRepository<BookingRecommendationDocument, String> {

    Flux<BookingRecommendationDocument> findByBookingIdOrderByGeneratedAtDesc(UUID bookingId);
}
