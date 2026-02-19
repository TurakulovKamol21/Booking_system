package com.autoguide.backend.service;

import com.autoguide.backend.dto.CreateRoomRequest;
import com.autoguide.backend.dto.RoomResponse;
import com.autoguide.backend.exception.NotFoundException;
import com.autoguide.backend.model.RoomEntity;
import com.autoguide.backend.repository.r2dbc.RoomRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public Mono<RoomResponse> create(CreateRoomRequest request) {
        RoomEntity entity = new RoomEntity();
        entity.setRoomNumber(request.roomNumber());
        entity.setRoomType(request.roomType());
        entity.setNightlyRate(request.nightlyRate());
        entity.setCreatedAt(Instant.now());

        return roomRepository.save(entity)
                .map(this::toResponse);
    }

    public Mono<RoomEntity> getEntityById(UUID id) {
        return roomRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Room not found: " + id)));
    }

    public Mono<RoomResponse> getById(UUID id) {
        return getEntityById(id).map(this::toResponse);
    }

    public Flux<RoomResponse> getAll() {
        return roomRepository.findAll().map(this::toResponse);
    }

    private RoomResponse toResponse(RoomEntity entity) {
        return new RoomResponse(
                entity.getId(),
                entity.getRoomNumber(),
                entity.getRoomType(),
                entity.getNightlyRate(),
                entity.getCreatedAt()
        );
    }
}
