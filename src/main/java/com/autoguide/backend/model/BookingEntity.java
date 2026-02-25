package com.autoguide.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Table("bookings")
public class BookingEntity {

    @Id
    private UUID id;
    private UUID hotelId;
    private UUID guestId;
    private UUID roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BookingStatus status;
    private BigDecimal totalAmount;
    private BigDecimal prepaymentAmount;
    private BookingPaymentStatus paymentStatus;
    private String paymentMethod;
    private String paymentReference;
    private boolean prepaid;
    private Instant createdAt;
    private Instant updatedAt;

    public BookingEntity() {
    }

    public BookingEntity(
            UUID id,
            UUID hotelId,
            UUID guestId,
            UUID roomId,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            BookingStatus status,
            BigDecimal totalAmount,
            BigDecimal prepaymentAmount,
            BookingPaymentStatus paymentStatus,
            String paymentMethod,
            String paymentReference,
            boolean prepaid,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.hotelId = hotelId;
        this.guestId = guestId;
        this.roomId = roomId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.prepaymentAmount = prepaymentAmount;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.paymentReference = paymentReference;
        this.prepaid = prepaid;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public BookingEntity(
            UUID id,
            UUID guestId,
            UUID roomId,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            BookingStatus status,
            BigDecimal totalAmount,
            BigDecimal prepaymentAmount,
            BookingPaymentStatus paymentStatus,
            String paymentMethod,
            String paymentReference,
            boolean prepaid,
            Instant createdAt,
            Instant updatedAt
    ) {
        this(
                id,
                null,
                guestId,
                roomId,
                checkInDate,
                checkOutDate,
                status,
                totalAmount,
                prepaymentAmount,
                paymentStatus,
                paymentMethod,
                paymentReference,
                prepaid,
                createdAt,
                updatedAt
        );
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

    public UUID getGuestId() {
        return guestId;
    }

    public void setGuestId(UUID guestId) {
        this.guestId = guestId;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getPrepaymentAmount() {
        return prepaymentAmount;
    }

    public void setPrepaymentAmount(BigDecimal prepaymentAmount) {
        this.prepaymentAmount = prepaymentAmount;
    }

    public BookingPaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(BookingPaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public boolean isPrepaid() {
        return prepaid;
    }

    public void setPrepaid(boolean prepaid) {
        this.prepaid = prepaid;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
