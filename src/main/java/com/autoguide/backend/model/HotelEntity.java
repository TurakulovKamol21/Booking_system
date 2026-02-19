package com.autoguide.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("hotels")
public class HotelEntity {

    @Id
    private UUID id;
    private String code;
    private String name;
    private String city;
    private String country;
    private String addressLine;
    private String imageUrl;
    private Instant createdAt;

    public HotelEntity() {
    }

    public HotelEntity(
            UUID id,
            String code,
            String name,
            String city,
            String country,
            String addressLine,
            String imageUrl,
            Instant createdAt
    ) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.city = city;
        this.country = country;
        this.addressLine = addressLine;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
