package com.autoguide.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("hotel_user_scopes")
public class HotelUserScopeEntity {

    @Id
    private String username;
    private UUID hotelId;
    private HotelAccessLevel accessLevel;
    private Instant createdAt;

    public HotelUserScopeEntity() {
    }

    public HotelUserScopeEntity(String username, UUID hotelId, HotelAccessLevel accessLevel, Instant createdAt) {
        this.username = username;
        this.hotelId = hotelId;
        this.accessLevel = accessLevel;
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UUID getHotelId() {
        return hotelId;
    }

    public void setHotelId(UUID hotelId) {
        this.hotelId = hotelId;
    }

    public HotelAccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(HotelAccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
