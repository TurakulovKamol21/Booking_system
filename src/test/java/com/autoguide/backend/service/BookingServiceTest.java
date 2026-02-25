package com.autoguide.backend.service;

import com.autoguide.backend.dto.BookingResponse;
import com.autoguide.backend.dto.CreateBookingRequest;
import com.autoguide.backend.model.BookingEntity;
import com.autoguide.backend.model.BookingPaymentStatus;
import com.autoguide.backend.model.BookingStatus;
import com.autoguide.backend.model.GuestEntity;
import com.autoguide.backend.model.RoomEntity;
import com.autoguide.backend.repository.r2dbc.BookingRepository;
import com.autoguide.backend.repository.r2dbc.GuestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private GuestService guestService;

    @Mock
    private RoomService roomService;

    @Mock
    private BookingRecommendationService bookingRecommendationService;

    @Mock
    private ReactiveStringRedisTemplate redisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOperations;

    @Mock
    private HotelAccessService hotelAccessService;

    @Mock
    private GuestRepository guestRepository;

    private BookingService bookingService;
    private ObjectMapper objectMapper;
    private UUID hotelId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        hotelId = UUID.randomUUID();

        bookingService = new BookingService(
                bookingRepository,
                guestService,
                roomService,
                bookingRecommendationService,
                redisTemplate,
                objectMapper,
                hotelAccessService,
                guestRepository
        );

        lenient().when(hotelAccessService.currentScope())
                .thenReturn(Mono.just(new HotelAccessService.AccessScope("test_user", null, true)));
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(valueOperations.set(anyString(), anyString(), any(Duration.class))).thenReturn(Mono.just(true));
    }

    @Test
    void createBookingShouldSaveAndGenerateRecommendation() {
        UUID guestId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();

        GuestEntity guest = new GuestEntity(guestId, hotelId, "John Guest", "john@example.com", Instant.now());
        RoomEntity room = new RoomEntity(roomId, hotelId, "501", "DELUXE", new BigDecimal("120.00"), null, null, Instant.now());

        when(guestService.getEntityById(eq(guestId), any(HotelAccessService.AccessScope.class))).thenReturn(Mono.just(guest));
        when(roomService.getEntityById(eq(roomId), any(HotelAccessService.AccessScope.class))).thenReturn(Mono.just(room));
        when(bookingRepository.save(any(BookingEntity.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(bookingRecommendationService.generateInitialRecommendation(any(BookingEntity.class))).thenReturn(Mono.empty());

        CreateBookingRequest request = new CreateBookingRequest(
                guestId,
                roomId,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3)
        );

        StepVerifier.create(bookingService.create(request))
                .assertNext(response -> {
                    org.junit.jupiter.api.Assertions.assertEquals(hotelId, response.hotelId());
                    org.junit.jupiter.api.Assertions.assertEquals(guestId, response.guestId());
                    org.junit.jupiter.api.Assertions.assertEquals(roomId, response.roomId());
                    org.junit.jupiter.api.Assertions.assertEquals(BookingStatus.CREATED, response.status());
                    org.junit.jupiter.api.Assertions.assertEquals(BookingPaymentStatus.UNPAID, response.paymentStatus());
                    org.junit.jupiter.api.Assertions.assertEquals("John Guest", response.guestFullName());
                    org.junit.jupiter.api.Assertions.assertEquals("501", response.roomNumber());
                })
                .verifyComplete();

        verify(bookingRecommendationService).generateInitialRecommendation(any(BookingEntity.class));
    }

    @Test
    void getByIdShouldReturnValueFromRedisCacheFirst() throws Exception {
        UUID bookingId = UUID.randomUUID();
        BookingResponse cachedResponse = new BookingResponse(
                bookingId,
                hotelId,
                UUID.randomUUID(),
                "Cached Guest",
                UUID.randomUUID(),
                "1202",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                BookingStatus.CREATED,
                new BigDecimal("120.00"),
                new BigDecimal("0.00"),
                BookingPaymentStatus.UNPAID,
                null,
                null,
                false,
                Instant.now(),
                Instant.now()
        );

        String payload = objectMapper.writeValueAsString(cachedResponse);
        when(valueOperations.get(eq("booking:cache:" + bookingId + ":all"))).thenReturn(Mono.just(payload));

        StepVerifier.create(bookingService.getById(bookingId))
                .expectNextMatches(response -> response.id().equals(bookingId) && response.guestFullName().equals("Cached Guest"))
                .verifyComplete();

        verifyNoInteractions(bookingRepository);
    }
}
