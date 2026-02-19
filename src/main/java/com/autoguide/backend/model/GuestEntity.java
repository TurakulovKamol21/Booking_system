package com.autoguide.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("guests")
public class GuestEntity {

    @Id
    private UUID id;
    private UUID hotelId;
    private String fullName;
    private String email;
    private Instant createdAt;

    public GuestEntity() {
    }

    public GuestEntity(UUID id, UUID hotelId, String fullName, String email, Instant createdAt) {
        this.id = id;
        this.hotelId = hotelId;
        this.fullName = fullName;
        this.email = email;
        this.createdAt = createdAt;
    }

    public GuestEntity(UUID id, String fullName, String email, Instant createdAt) {
        this(id, null, fullName, email, createdAt);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getHotelId() {
        return hotelId;
    }

    public void setHotelId(UUID hotelId) {
        this.hotelId = hotelId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
