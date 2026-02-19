package com.autoguide.backend.service;

import com.autoguide.backend.dto.BookingResponse;
import com.autoguide.backend.dto.CreateBookingRequest;
import com.autoguide.backend.dto.UpdateBookingStatusRequest;
import com.autoguide.backend.exception.NotFoundException;
import com.autoguide.backend.model.BookingEntity;
import com.autoguide.backend.model.BookingStatus;
import com.autoguide.backend.model.GuestEntity;
import com.autoguide.backend.model.RoomEntity;
import com.autoguide.backend.repository.r2dbc.BookingRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class BookingService {

    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final BookingRepository bookingRepository;
    private final GuestService guestService;
    private final RoomService roomService;
    private final BookingRecommendationService bookingRecommendationService;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public BookingService(
            BookingRepository bookingRepository,
            GuestService guestService,
            RoomService roomService,
            BookingRecommendationService bookingRecommendationService,
            ReactiveStringRedisTemplate redisTemplate,
            ObjectMapper objectMapper
    ) {
        this.bookingRepository = bookingRepository;
        this.guestService = guestService;
        this.roomService = roomService;
        this.bookingRecommendationService = bookingRecommendationService;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public Mono<BookingResponse> create(CreateBookingRequest request) {
        return validateDates(request.checkInDate(), request.checkOutDate())
                .then(Mono.zip(
                        guestService.getEntityById(request.guestId()),
                        roomService.getEntityById(request.roomId())
                ))
                .flatMap(tuple -> {
                    GuestEntity guest = tuple.getT1();
                    RoomEntity room = tuple.getT2();

                    Instant now = Instant.now();
                    BookingEntity entity = new BookingEntity();
                    entity.setGuestId(guest.getId());
                    entity.setRoomId(room.getId());
                    entity.setCheckInDate(request.checkInDate());
                    entity.setCheckOutDate(request.checkOutDate());
                    entity.setStatus(BookingStatus.CREATED);
                    entity.setCreatedAt(now);
                    entity.setUpdatedAt(now);

                    return bookingRepository.save(entity)
                            .flatMap(saved -> bookingRecommendationService.generateInitialRecommendation(saved)
                                    .then(enrichBooking(saved, guest, room)))
                            .flatMap(this::cacheResponse);
                });
    }

    public Mono<BookingResponse> getById(UUID id) {
        String key = cacheKey(id);
        ReactiveValueOperations<String, String> valueOps = redisTemplate.opsForValue();

        return valueOps.get(key)
                .flatMap(this::deserialize)
                .switchIfEmpty(Mono.defer(() -> loadFromDbAndCache(id)));
    }

    public Flux<BookingResponse> getAll(BookingStatus status) {
        Flux<BookingEntity> source = status == null
                ? bookingRepository.findAll()
                : bookingRepository.findAllByStatus(status);

        return source.flatMap(this::enrichBooking);
    }

    public Mono<BookingResponse> updateStatus(UUID id, UpdateBookingStatusRequest request) {
        return bookingRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Booking not found: " + id)))
                .flatMap(existing -> {
                    existing.setStatus(request.status());
                    existing.setUpdatedAt(Instant.now());
                    return bookingRepository.save(existing);
                })
                .flatMap(this::enrichBooking)
                .flatMap(this::cacheResponse);
    }

    private Mono<BookingResponse> loadFromDbAndCache(UUID id) {
        return bookingRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Booking not found: " + id)))
                .flatMap(this::enrichBooking)
                .flatMap(this::cacheResponse);
    }

    private Mono<BookingResponse> enrichBooking(BookingEntity booking) {
        return Mono.zip(
                        guestService.getEntityById(booking.getGuestId()),
                        roomService.getEntityById(booking.getRoomId())
                )
                .flatMap(tuple -> enrichBooking(booking, tuple.getT1(), tuple.getT2()));
    }

    private Mono<BookingResponse> enrichBooking(BookingEntity booking, GuestEntity guest, RoomEntity room) {
        return Mono.just(new BookingResponse(
                booking.getId(),
                booking.getGuestId(),
                guest.getFullName(),
                booking.getRoomId(),
                room.getRoomNumber(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getStatus(),
                booking.getCreatedAt(),
                booking.getUpdatedAt()
        ));
    }

    private Mono<BookingResponse> cacheResponse(BookingResponse response) {
        try {
            String serialized = objectMapper.writeValueAsString(response);
            return redisTemplate.opsForValue()
                    .set(cacheKey(response.id()), serialized, CACHE_TTL)
                    .thenReturn(response)
                    .onErrorReturn(response);
        } catch (JsonProcessingException e) {
            return Mono.just(response);
        }
    }

    private Mono<BookingResponse> deserialize(String payload) {
        try {
            BookingResponse response = objectMapper.readValue(payload, BookingResponse.class);
            return Mono.just(response);
        } catch (Exception e) {
            return Mono.empty();
        }
    }

    private Mono<Void> validateDates(LocalDate checkInDate, LocalDate checkOutDate) {
        if (!checkOutDate.isAfter(checkInDate)) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "checkOutDate must be after checkInDate"
            ));
        }
        return Mono.empty();
    }

    private String cacheKey(UUID bookingId) {
        return "booking:cache:" + bookingId;
    }
}
