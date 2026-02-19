package com.autoguide.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public class CreateRoomMultipartRequest {

    @NotBlank
    @Size(max = 20)
    private String roomNumber;

    @NotBlank
    @Size(max = 50)
    private String roomType;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal nightlyRate;

    @Size(max = 280)
    private String shortDescription;

    private UUID hotelId;

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

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public UUID getHotelId() {
        return hotelId;
    }

    public void setHotelId(UUID hotelId) {
        this.hotelId = hotelId;
    }

    public CreateRoomRequest toCreateRoomRequest() {
        return new CreateRoomRequest(
                roomNumber,
                roomType,
                nightlyRate,
                null,
                shortDescription,
                hotelId
        );
    }
}
