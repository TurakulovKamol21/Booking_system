package com.autoguide.backend.service;

import com.autoguide.backend.dto.PublicAmenityResponse;
import com.autoguide.backend.dto.PublicHomeResponse;
import com.autoguide.backend.dto.PublicOfferResponse;
import com.autoguide.backend.dto.PublicRoomHighlightResponse;
import com.autoguide.backend.dto.HotelResponse;
import com.autoguide.backend.model.RoomEntity;
import com.autoguide.backend.repository.r2dbc.HotelRepository;
import com.autoguide.backend.repository.r2dbc.LandingAmenityRepository;
import com.autoguide.backend.repository.r2dbc.LandingOfferRepository;
import com.autoguide.backend.repository.r2dbc.RoomRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class PublicContentService {

    private static final String HERO_TITLE = "Book premium rooms with confident digital flow";
    private static final String HERO_SUBTITLE = "All room cards, prices, offers, and amenities are served from backend data.";
    private static final String HERO_IMAGE_URL =
            "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1800&q=80";

    private final LandingAmenityRepository landingAmenityRepository;
    private final LandingOfferRepository landingOfferRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final RoomPresentationResolver roomPresentationResolver;

    public PublicContentService(
            LandingAmenityRepository landingAmenityRepository,
            LandingOfferRepository landingOfferRepository,
            HotelRepository hotelRepository,
            RoomRepository roomRepository,
            RoomPresentationResolver roomPresentationResolver
    ) {
        this.landingAmenityRepository = landingAmenityRepository;
        this.landingOfferRepository = landingOfferRepository;
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.roomPresentationResolver = roomPresentationResolver;
    }

    public Mono<PublicHomeResponse> getHomeContent() {
        Mono<List<PublicAmenityResponse>> amenitiesMono = landingAmenityRepository
                .findAllByOrderBySortOrderAsc()
                .map(entity -> new PublicAmenityResponse(
                        entity.getId(),
                        entity.getTitle(),
                        entity.getDescription()
                ))
                .collectList();

        Mono<List<PublicOfferResponse>> offersMono = landingOfferRepository
                .findAllByOrderBySortOrderAsc()
                .map(entity -> new PublicOfferResponse(
                        entity.getId(),
                        entity.getTitle(),
                        entity.getNote(),
                        entity.getPriceLabel()
                ))
                .collectList();

        return Mono.zip(amenitiesMono, offersMono)
                .map(tuple -> new PublicHomeResponse(
                        HERO_TITLE,
                        HERO_SUBTITLE,
                        HERO_IMAGE_URL,
                        tuple.getT1(),
                        tuple.getT2()
                ));
    }

    public Flux<PublicRoomHighlightResponse> getRoomHighlights(int limit, UUID hotelId) {
        int safeLimit = Math.min(Math.max(limit, 1), 24);
        Flux<RoomEntity> source = hotelId == null
                ? roomRepository.findAll()
                : roomRepository.findAllByHotelId(hotelId);

        return source
                .sort(Comparator.comparing(RoomEntity::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .take(safeLimit)
                .map(this::toRoomHighlight);
    }

    public Flux<HotelResponse> getPublicHotels() {
        return hotelRepository.findAllByOrderByNameAsc()
                .map(entity -> new HotelResponse(
                        entity.getId(),
                        entity.getCode(),
                        entity.getName(),
                        entity.getCity(),
                        entity.getCountry(),
                        entity.getAddressLine(),
                        entity.getImageUrl(),
                        entity.getCreatedAt()
                ));
    }

    private PublicRoomHighlightResponse toRoomHighlight(RoomEntity entity) {
        return new PublicRoomHighlightResponse(
                entity.getId(),
                entity.getHotelId(),
                entity.getRoomNumber(),
                entity.getRoomType(),
                entity.getNightlyRate(),
                roomPresentationResolver.resolveImageUrl(entity.getImageUrl(), entity.getRoomType()),
                roomPresentationResolver.resolveShortDescription(entity.getShortDescription(), entity.getRoomType())
        );
    }
}
