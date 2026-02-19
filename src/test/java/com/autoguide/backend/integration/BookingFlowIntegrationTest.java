package com.autoguide.backend.integration;

import com.autoguide.backend.dto.BookingRecommendationResponse;
import com.autoguide.backend.dto.BookingResponse;
import com.autoguide.backend.dto.CreateBookingRequest;
import com.autoguide.backend.dto.CreateGuestRequest;
import com.autoguide.backend.dto.CreateRoomRequest;
import com.autoguide.backend.dto.GuestResponse;
import com.autoguide.backend.dto.RoomResponse;
import com.autoguide.backend.service.BookingRecommendationService;
import com.autoguide.backend.service.BookingService;
import com.autoguide.backend.service.GuestService;
import com.autoguide.backend.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class BookingFlowIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("autoguide")
            .withUsername("autoguide")
            .withPassword("autoguide");

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7.0");

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> String.format("r2dbc:postgresql://%s:%d/%s",
                POSTGRES.getHost(),
                POSTGRES.getMappedPort(5432),
                POSTGRES.getDatabaseName()));
        registry.add("spring.r2dbc.username", POSTGRES::getUsername);
        registry.add("spring.r2dbc.password", POSTGRES::getPassword);

        registry.add("spring.flyway.url", POSTGRES::getJdbcUrl);
        registry.add("spring.flyway.user", POSTGRES::getUsername);
        registry.add("spring.flyway.password", POSTGRES::getPassword);

        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);

        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    }

    @Autowired
    private GuestService guestService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRecommendationService bookingRecommendationService;

    @Test
    void shouldCreateBookingAndRecommendationAcrossPostgresMongoRedis() {
        GuestResponse guest = guestService.create(
                new CreateGuestRequest("Integration Guest", "integration.guest@example.com")
        ).block();

        RoomResponse room = roomService.create(
                new CreateRoomRequest("905", "SUITE", new BigDecimal("250.00"))
        ).block();

        BookingResponse createdBooking = bookingService.create(
                new CreateBookingRequest(
                        guest.id(),
                        room.id(),
                        LocalDate.now().plusDays(1),
                        LocalDate.now().plusDays(4)
                )
        ).block();

        BookingResponse loadedBooking = bookingService.getById(createdBooking.id()).block();
        List<BookingRecommendationResponse> recommendations = bookingRecommendationService
                .getByBookingId(createdBooking.id())
                .collectList()
                .block();

        assertThat(createdBooking).isNotNull();
        assertThat(loadedBooking).isNotNull();
        assertThat(loadedBooking.id()).isEqualTo(createdBooking.id());
        assertThat(loadedBooking.guestFullName()).isEqualTo("Integration Guest");
        assertThat(loadedBooking.roomNumber()).isEqualTo("905");
        assertThat(recommendations).isNotEmpty();
        assertThat(recommendations.get(0).bookingId()).isEqualTo(createdBooking.id());
    }
}
