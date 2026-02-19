package com.autoguide.backend.config;

import com.autoguide.backend.model.GuestEntity;
import com.autoguide.backend.model.HotelEntity;
import com.autoguide.backend.model.BookingEntity;
import com.autoguide.backend.model.RoomEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Configuration
public class R2dbcIdGenerationConfig {

    @Bean
    BeforeConvertCallback<GuestEntity> guestIdGenerator() {
        return (entity, table) -> {
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            return Mono.just(entity);
        };
    }

    @Bean
    BeforeConvertCallback<RoomEntity> roomIdGenerator() {
        return (entity, table) -> {
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            return Mono.just(entity);
        };
    }

    @Bean
    BeforeConvertCallback<HotelEntity> hotelIdGenerator() {
        return (entity, table) -> {
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            return Mono.just(entity);
        };
    }

    @Bean
    BeforeConvertCallback<BookingEntity> bookingIdGenerator() {
        return (entity, table) -> {
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            return Mono.just(entity);
        };
    }
}
