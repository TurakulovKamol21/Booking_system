package com.autoguide.backend.controller;

import com.autoguide.backend.dto.HotelResponse;
import com.autoguide.backend.dto.PublicHomeResponse;
import com.autoguide.backend.dto.PublicRoomHighlightResponse;
import com.autoguide.backend.service.PublicContentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public")
public class PublicContentController {

    private final PublicContentService publicContentService;

    public PublicContentController(PublicContentService publicContentService) {
        this.publicContentService = publicContentService;
    }

    @GetMapping("/home")
    public Mono<PublicHomeResponse> getHome() {
        return publicContentService.getHomeContent();
    }

    @GetMapping("/rooms/highlights")
    public Flux<PublicRoomHighlightResponse> getRoomHighlights(
            @RequestParam(defaultValue = "6") int limit,
            @RequestParam(required = false) UUID hotelId
    ) {
        return publicContentService.getRoomHighlights(limit, hotelId);
    }

    @GetMapping("/hotels")
    public Flux<HotelResponse> getHotels() {
        return publicContentService.getPublicHotels();
    }
}
