package com.autoguide.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "booking_recommendations")
public class BookingRecommendationDocument {

    @Id
    private String id;
    private UUID bookingId;
    private String suggestion;
    private String model;
    private double confidence;
    private Instant generatedAt;

    public BookingRecommendationDocument() {
    }

    public BookingRecommendationDocument(String id, UUID bookingId, String suggestion, String model, double confidence, Instant generatedAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.suggestion = suggestion;
        this.model = model;
        this.confidence = confidence;
        this.generatedAt = generatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }
}
