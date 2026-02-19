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
    private final HotelAccessService hotelAccessService;

    public BookingService(
            BookingRepository bookingRepository,
            GuestService guestService,
            RoomService roomService,
            BookingRecommendationService bookingRecommendationService,
            ReactiveStringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            HotelAccessService hotelAccessService
    ) {
        this.bookingRepository = bookingRepository;
        this.guestService = guestService;
        this.roomService = roomService;
        this.bookingRecommendationService = bookingRecommendationService;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.hotelAccessService = hotelAccessService;
    }

    public Mono<BookingResponse> create(CreateBookingRequest request) {
        return validateDates(request.checkInDate(), request.checkOutDate())
                .then(hotelAccessService.currentScope())
                .flatMap(scope -> Mono.zip(
                                guestService.getEntityById(request.guestId(), scope),
                                roomService.getEntityById(request.roomId(), scope)
                        )
                        .flatMap(tuple -> {
                            GuestEntity guest = tuple.getT1();
                            RoomEntity room = tuple.getT2();

                            if (!room.getHotelId().equals(guest.getHotelId())) {
                                return Mono.error(new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Guest and room must belong to the same hotel"
                                ));
                            }

                            Instant now = Instant.now();
                            BookingEntity entity = new BookingEntity();
                            entity.setHotelId(room.getHotelId());
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
                                    .flatMap(response -> cacheResponse(response, scope));
                        }));
    }

    public Mono<BookingResponse> getById(UUID id) {
        return hotelAccessService.currentScope()
                .flatMap(scope -> {
                    String key = cacheKey(id, scope);
                    ReactiveValueOperations<String, String> valueOps = redisTemplate.opsForValue();

                    return valueOps.get(key)
                            .flatMap(this::deserialize)
                            .switchIfEmpty(Mono.defer(() -> loadFromDbAndCache(id, scope)));
                });
    }

    public Flux<BookingResponse> getAll(BookingStatus status) {
        return hotelAccessService.currentScope()
                .flatMapMany(scope -> {
                    Flux<BookingEntity> source;
                    if (scope.superAdmin()) {
                        source = status == null
                                ? bookingRepository.findAll()
                                : bookingRepository.findAllByStatus(status);
                    } else {
                        UUID hotelId = scope.requiredHotelId();
                        source = status == null
                                ? bookingRepository.findAllByHotelId(hotelId)
                                : bookingRepository.findAllByHotelIdAndStatus(hotelId, status);
                    }

                    return source.flatMap(booking -> enrichBooking(booking, scope));
                });
    }

    public Mono<BookingResponse> updateStatus(UUID id, UpdateBookingStatusRequest request) {
        return hotelAccessService.currentScope()
                .flatMap(scope -> scopedFindById(id, scope)
                        .flatMap(existing -> {
                            existing.setStatus(request.status());
                            existing.setUpdatedAt(Instant.now());
                            return bookingRepository.save(existing);
                        })
                        .flatMap(booking -> enrichBooking(booking, scope))
                        .flatMap(response -> cacheResponse(response, scope)));
    }

    private Mono<BookingResponse> loadFromDbAndCache(UUID id, HotelAccessService.AccessScope scope) {
        return scopedFindById(id, scope)
                .flatMap(booking -> enrichBooking(booking, scope))
                .flatMap(response -> cacheResponse(response, scope));
    }

    private Mono<BookingEntity> scopedFindById(UUID id, HotelAccessService.AccessScope scope) {
        Mono<BookingEntity> source = scope.superAdmin()
                ? bookingRepository.findById(id)
                : bookingRepository.findByIdAndHotelId(id, scope.requiredHotelId());

        return source.switchIfEmpty(Mono.error(new NotFoundException("Booking not found: " + id)));
    }

    private Mono<BookingResponse> enrichBooking(BookingEntity booking, HotelAccessService.AccessScope scope) {
        return Mono.zip(
                        guestService.getEntityById(booking.getGuestId(), scope),
                        roomService.getEntityById(booking.getRoomId(), scope)
                )
                .flatMap(tuple -> enrichBooking(booking, tuple.getT1(), tuple.getT2()));
    }

    private Mono<BookingResponse> enrichBooking(BookingEntity booking, GuestEntity guest, RoomEntity room) {
        return Mono.just(new BookingResponse(
                booking.getId(),
                booking.getHotelId(),
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

    private Mono<BookingResponse> cacheResponse(BookingResponse response, HotelAccessService.AccessScope scope) {
        try {
            String serialized = objectMapper.writeValueAsString(response);
            return redisTemplate.opsForValue()
                    .set(cacheKey(response.id(), scope), serialized, CACHE_TTL)
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

    private String cacheKey(UUID bookingId, HotelAccessService.AccessScope scope) {
        String scopeKey = scope.superAdmin() ? "all" : "hotel:" + scope.requiredHotelId();
        return "booking:cache:" + bookingId + ":" + scopeKey;
    }
}
