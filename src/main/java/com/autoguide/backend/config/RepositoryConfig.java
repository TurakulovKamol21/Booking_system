package com.autoguide.backend.config;

import com.autoguide.backend.repository.mongo.BookingRecommendationRepository;
import com.autoguide.backend.repository.r2dbc.GuestRepository;
import com.autoguide.backend.repository.r2dbc.BookingRepository;
import com.autoguide.backend.repository.r2dbc.HotelRepository;
import com.autoguide.backend.repository.r2dbc.HotelUserScopeRepository;
import com.autoguide.backend.repository.r2dbc.LandingAmenityRepository;
import com.autoguide.backend.repository.r2dbc.LandingOfferRepository;
import com.autoguide.backend.repository.r2dbc.RoomRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@EnableR2dbcRepositories(basePackageClasses = {
        GuestRepository.class,
        RoomRepository.class,
        BookingRepository.class,
        HotelRepository.class,
        HotelUserScopeRepository.class,
        LandingAmenityRepository.class,
        LandingOfferRepository.class
})
@EnableReactiveMongoRepositories(basePackageClasses = BookingRecommendationRepository.class)
public class RepositoryConfig {
}
