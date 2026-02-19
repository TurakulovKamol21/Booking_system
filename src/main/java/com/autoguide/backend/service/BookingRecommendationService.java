package com.autoguide.backend.service;

import com.autoguide.backend.dto.BookingRecommendationResponse;
import com.autoguide.backend.model.BookingEntity;
import com.autoguide.backend.model.BookingRecommendationDocument;
import com.autoguide.backend.repository.mongo.BookingRecommendationRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
public class BookingRecommendationService {

    private final BookingRecommendationRepository bookingRecommendationRepository;

    public BookingRecommendationService(BookingRecommendationRepository bookingRecommendationRepository) {
        this.bookingRecommendationRepository = bookingRecommendationRepository;
    }

    public Mono<Void> generateInitialRecommendation(BookingEntity booking) {
        String suggestion = "Offer breakfast bundle and airport pickup for stay from "
                + booking.getCheckInDate() + " to " + booking.getCheckOutDate();

        BookingRecommendationDocument document = new BookingRecommendationDocument();
        document.setBookingId(booking.getId());
        document.setSuggestion(suggestion);
        document.setModel("rule-based-ai-assistant");
        document.setConfidence(0.88);
        document.setGeneratedAt(Instant.now());

        return bookingRecommendationRepository.save(document).then();
    }

    public Flux<BookingRecommendationResponse> getByBookingId(UUID bookingId) {
        return bookingRecommendationRepository.findByBookingIdOrderByGeneratedAtDesc(bookingId)
                .map(this::toResponse);
    }

    private BookingRecommendationResponse toResponse(BookingRecommendationDocument document) {
        return new BookingRecommendationResponse(
                document.getId(),
                document.getBookingId(),
                document.getSuggestion(),
                document.getModel(),
                document.getConfidence(),
                document.getGeneratedAt()
        );
    }
}
