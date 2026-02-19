package com.autoguide.backend.service;

import com.autoguide.backend.dto.CreateRoomRequest;
import com.autoguide.backend.dto.RoomResponse;
import com.autoguide.backend.dto.UpdateRoomRequest;
import com.autoguide.backend.exception.NotFoundException;
import com.autoguide.backend.model.RoomEntity;
import com.autoguide.backend.repository.r2dbc.BookingRepository;
import com.autoguide.backend.repository.r2dbc.RoomRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final RoomPresentationResolver roomPresentationResolver;
    private final HotelAccessService hotelAccessService;
    private final RoomImageStorageService roomImageStorageService;

    public RoomService(
            RoomRepository roomRepository,
            BookingRepository bookingRepository,
            RoomPresentationResolver roomPresentationResolver,
            HotelAccessService hotelAccessService,
            RoomImageStorageService roomImageStorageService
    ) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.roomPresentationResolver = roomPresentationResolver;
        this.hotelAccessService = hotelAccessService;
        this.roomImageStorageService = roomImageStorageService;
    }

    public Mono<RoomResponse> create(CreateRoomRequest request) {
        return createInternal(request, request.imageUrl());
    }

    public Mono<RoomResponse> createWithUploadedImage(CreateRoomRequest request, FilePart imageFile) {
        return roomImageStorageService.storeRoomImage(imageFile)
                .flatMap(storedImageUrl -> createInternal(request, storedImageUrl));
    }

    private Mono<RoomResponse> createInternal(CreateRoomRequest request, String imageUrl) {
        return hotelAccessService.currentScope()
                .flatMap(scope -> hotelAccessService.resolveHotelForWrite(scope, request.hotelId()))
                .flatMap(hotelId -> {
                    RoomEntity entity = new RoomEntity();
                    entity.setHotelId(hotelId);
                    entity.setRoomNumber(request.roomNumber());
                    entity.setRoomType(request.roomType());
                    entity.setNightlyRate(request.nightlyRate());
                    entity.setImageUrl(roomPresentationResolver.resolveImageUrl(imageUrl, request.roomType()));
                    entity.setShortDescription(roomPresentationResolver.resolveShortDescription(
                            request.shortDescription(),
                            request.roomType()
                    ));
                    entity.setCreatedAt(Instant.now());

                    return roomRepository.save(entity).map(this::toResponse);
                });
    }

    public Mono<RoomEntity> getEntityById(UUID id) {
        return hotelAccessService.currentScope()
                .flatMap(scope -> getEntityById(id, scope));
    }

    Mono<RoomEntity> getEntityById(UUID id, HotelAccessService.AccessScope scope) {
        Mono<RoomEntity> source = scope.superAdmin()
                ? roomRepository.findById(id)
                : roomRepository.findByIdAndHotelId(id, scope.requiredHotelId());

        return source.switchIfEmpty(Mono.error(new NotFoundException("Room not found: " + id)));
    }

    public Mono<RoomResponse> getById(UUID id) {
        return getEntityById(id).map(this::toResponse);
    }

    public Flux<RoomResponse> getAll() {
        return hotelAccessService.currentScope()
                .flatMapMany(scope -> {
                    if (scope.superAdmin()) {
                        return roomRepository.findAll();
                    }
                    return roomRepository.findAllByHotelId(scope.requiredHotelId());
                })
                .map(this::toResponse);
    }

    public Mono<RoomResponse> update(UUID id, UpdateRoomRequest request) {
        return hotelAccessService.currentScope()
                .flatMap(scope -> getEntityById(id, scope))
                .flatMap(entity -> {
                    entity.setRoomNumber(request.roomNumber().trim());
                    entity.setRoomType(request.roomType().trim());
                    entity.setNightlyRate(request.nightlyRate());
                    entity.setShortDescription(roomPresentationResolver.resolveShortDescription(
                            request.shortDescription(),
                            request.roomType()
                    ));
                    return roomRepository.save(entity).map(this::toResponse);
                });
    }

    public Mono<Void> delete(UUID id) {
        return hotelAccessService.currentScope()
                .flatMap(scope -> getEntityById(id, scope))
                .flatMap(entity -> bookingRepository.existsByRoomId(entity.getId())
                        .flatMap(hasBooking -> {
                            if (hasBooking) {
                                return Mono.error(new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "Cannot delete room that has bookings"
                                ));
                            }
                            return roomRepository.delete(entity);
                        }));
    }

    private RoomResponse toResponse(RoomEntity entity) {
        return new RoomResponse(
                entity.getId(),
                entity.getHotelId(),
                entity.getRoomNumber(),
                entity.getRoomType(),
                entity.getNightlyRate(),
                roomPresentationResolver.resolveImageUrl(entity.getImageUrl(), entity.getRoomType()),
                roomPresentationResolver.resolveShortDescription(entity.getShortDescription(), entity.getRoomType()),
                entity.getCreatedAt()
        );
    }
}
