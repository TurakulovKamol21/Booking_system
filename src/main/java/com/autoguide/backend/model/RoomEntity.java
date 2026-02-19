package com.autoguide.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Table("rooms")
public class RoomEntity {

    @Id
    private UUID id;
    private UUID hotelId;
    private String roomNumber;
    private String roomType;
    private BigDecimal nightlyRate;
    private String imageUrl;
    private String shortDescription;
    private Instant createdAt;

    public RoomEntity() {
    }

    public RoomEntity(
            UUID id,
            UUID hotelId,
            String roomNumber,
            String roomType,
            BigDecimal nightlyRate,
            String imageUrl,
            String shortDescription,
            Instant createdAt
    ) {
        this.id = id;
        this.hotelId = hotelId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.nightlyRate = nightlyRate;
        this.imageUrl = imageUrl;
        this.shortDescription = shortDescription;
        this.createdAt = createdAt;
    }

    public RoomEntity(
            UUID id,
            String roomNumber,
            String roomType,
            BigDecimal nightlyRate,
            String imageUrl,
            String shortDescription,
            Instant createdAt
    ) {
        this(id, null, roomNumber, roomType, nightlyRate, imageUrl, shortDescription, createdAt);
    }

    public RoomEntity(UUID id, String roomNumber, String roomType, BigDecimal nightlyRate, Instant createdAt) {
        this(id, null, roomNumber, roomType, nightlyRate, null, null, createdAt);
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

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public BigDecimal getNightlyRate() {
        return nightlyRate;
    }

    public void setNightlyRate(BigDecimal nightlyRate) {
        this.nightlyRate = nightlyRate;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
