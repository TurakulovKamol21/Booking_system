package com.autoguide.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateGuestRequest(
        @NotBlank @Size(max = 120) String fullName,
        @NotBlank @Email @Size(max = 180) String email,
        UUID hotelId
) {
    public CreateGuestRequest(String fullName, String email) {
        this(fullName, email, null);
    }
}
