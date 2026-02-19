package com.autoguide.backend.repository.r2dbc;

import com.autoguide.backend.model.RoomEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface RoomRepository extends ReactiveCrudRepository<RoomEntity, UUID> {
}
