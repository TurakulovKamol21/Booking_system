package com.autoguide.backend.service;

import com.autoguide.backend.dto.BookingResponse;
import com.autoguide.backend.dto.CreateBookingRequest;
import com.autoguide.backend.dto.PublicCreateBookingRequest;
import com.autoguide.backend.dto.UpdateBookingStatusRequest;
import com.autoguide.backend.exception.NotFoundException;
import com.autoguide.backend.model.BookingEntity;
import com.autoguide.backend.model.BookingPaymentStatus;
import com.autoguide.backend.model.BookingStatus;
import com.autoguide.backend.model.GuestEntity;
import com.autoguide.backend.model.RoomEntity;
import com.autoguide.backend.repository.r2dbc.BookingRepository;
import com.autoguide.backend.repository.r2dbc.GuestRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
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
    private final GuestRepository guestRepository;

    public BookingService(
            BookingRepository bookingRepository,
            GuestService guestService,
            RoomService roomService,
            BookingRecommendationService bookingRecommendationService,
            ReactiveStringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            HotelAccessService hotelAccessService,
            GuestRepository guestRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.guestService = guestService;
        this.roomService = roomService;
        this.bookingRecommendationService = bookingRecommendationService;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.hotelAccessService = hotelAccessService;
        this.guestRepository = guestRepository;
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

                            BookingEntity entity = buildInternalBookingEntity(
                                    guest,
                                    room,
                                    request.checkInDate(),
                                    request.checkOutDate()
                            );

                            return bookingRepository.save(entity)
                                    .flatMap(saved -> bookingRecommendationService.generateInitialRecommendation(saved)
                                            .then(enrichBooking(saved, guest, room)))
                                    .flatMap(response -> cacheResponse(response, scope));
                        }));
    }

    public Mono<BookingResponse> createPublicPrepaid(PublicCreateBookingRequest request) {
        return validateDates(request.checkInDate(), request.checkOutDate())
                .then(roomService.getEntityById(request.roomId(), HotelAccessService.AccessScope.unrestricted()))
                .flatMap(room -> findOrCreatePublicGuest(room.getHotelId(), request)
                        .flatMap(guest -> {
                            BookingEntity entity = buildPrepaidPublicBookingEntity(guest, room, request);

                            return bookingRepository.save(entity)
                                    .flatMap(saved -> bookingRecommendationService.generateInitialRecommendation(saved)
                                            .then(enrichBooking(saved, guest, room)))
                                    .flatMap(response -> cacheResponse(response, HotelAccessService.AccessScope.unrestricted()));
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

    public Flux<BookingResponse> getMyBookings(BookingStatus status) {
        return currentUserEmail()
                .flatMapMany(email -> guestRepository.findAllByEmail(email)
                        .map(GuestEntity::getId)
                        .collectList()
                        .flatMapMany(guestIds -> {
                            if (guestIds.isEmpty()) {
                                return Flux.empty();
                            }

                            Flux<BookingEntity> source = status == null
                                    ? bookingRepository.findAllByGuestIdIn(guestIds)
                                    : bookingRepository.findAllByGuestIdInAndStatus(guestIds, status);

                            return source.flatMap(booking ->
                                    enrichBooking(booking, HotelAccessService.AccessScope.unrestricted()));
                        }));
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
                booking.getTotalAmount(),
                booking.getPrepaymentAmount(),
                booking.getPaymentStatus(),
                booking.getPaymentMethod(),
                booking.getPaymentReference(),
                booking.isPrepaid(),
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

    private BookingEntity buildInternalBookingEntity(
            GuestEntity guest,
            RoomEntity room,
            LocalDate checkInDate,
            LocalDate checkOutDate
    ) {
        Instant now = Instant.now();
        BookingEntity entity = new BookingEntity();
        entity.setHotelId(room.getHotelId());
        entity.setGuestId(guest.getId());
        entity.setRoomId(room.getId());
        entity.setCheckInDate(checkInDate);
        entity.setCheckOutDate(checkOutDate);
        entity.setStatus(BookingStatus.CREATED);
        entity.setTotalAmount(computeTotalAmount(room, checkInDate, checkOutDate));
        entity.setPrepaymentAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        entity.setPaymentStatus(BookingPaymentStatus.UNPAID);
        entity.setPaymentMethod(null);
        entity.setPaymentReference(null);
        entity.setPrepaid(false);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return entity;
    }

    private BookingEntity buildPrepaidPublicBookingEntity(
            GuestEntity guest,
            RoomEntity room,
            PublicCreateBookingRequest request
    ) {
        BigDecimal totalAmount = computeTotalAmount(room, request.checkInDate(), request.checkOutDate());
        BigDecimal prepaymentAmount = normalizeMoney(request.prepaymentAmount());

        if (prepaymentAmount.compareTo(totalAmount) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Prepayment must cover full booking amount: " + totalAmount
            );
        }

        Instant now = Instant.now();
        BookingEntity entity = new BookingEntity();
        entity.setHotelId(room.getHotelId());
        entity.setGuestId(guest.getId());
        entity.setRoomId(room.getId());
        entity.setCheckInDate(request.checkInDate());
        entity.setCheckOutDate(request.checkOutDate());
        entity.setStatus(BookingStatus.CONFIRMED);
        entity.setTotalAmount(totalAmount);
        entity.setPrepaymentAmount(prepaymentAmount);
        entity.setPaymentStatus(BookingPaymentStatus.PAID);
        entity.setPaymentMethod(normalizePaymentMethod(request.paymentMethod()));
        entity.setPaymentReference(resolvePaymentReference(request.paymentReference()));
        entity.setPrepaid(true);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return entity;
    }

    private Mono<GuestEntity> findOrCreatePublicGuest(UUID hotelId, PublicCreateBookingRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        return guestRepository.findByHotelIdAndEmail(hotelId, normalizedEmail)
                .switchIfEmpty(Mono.defer(() -> {
                    GuestEntity entity = new GuestEntity();
                    entity.setHotelId(hotelId);
                    entity.setFullName(request.fullName().trim());
                    entity.setEmail(normalizedEmail);
                    entity.setCreatedAt(Instant.now());
                    return guestRepository.save(entity);
                }));
    }

    private BigDecimal computeTotalAmount(RoomEntity room, LocalDate checkInDate, LocalDate checkOutDate) {
        long nights = Math.max(1L, ChronoUnit.DAYS.between(checkInDate, checkOutDate));
        return normalizeMoney(room.getNightlyRate().multiply(BigDecimal.valueOf(nights)));
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizePaymentMethod(String paymentMethod) {
        return paymentMethod == null
                ? "CARD"
                : paymentMethod.trim().toUpperCase(Locale.ROOT);
    }

    private String resolvePaymentReference(String paymentReference) {
        if (paymentReference != null && !paymentReference.trim().isEmpty()) {
            return paymentReference.trim();
        }

        return "PMT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private Mono<String> currentUserEmail() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required")))
                .flatMap(authentication -> {
                    String email = null;
                    Object principal = authentication.getPrincipal();
                    if (principal instanceof Jwt jwt) {
                        email = jwt.getClaimAsString("email");
                    }

                    if ((email == null || email.isBlank())
                            && principal instanceof String principalName
                            && principalName.contains("@")) {
                        email = principalName;
                    }

                    if (email == null || email.isBlank()) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Authenticated user email is required"
                        ));
                    }

                    return Mono.just(normalizeEmail(email));
                });
    }

    private String cacheKey(UUID bookingId, HotelAccessService.AccessScope scope) {
        String scopeKey = scope.superAdmin() ? "all" : "hotel:" + scope.requiredHotelId();
        return "booking:cache:" + bookingId + ":" + scopeKey;
    }
}
