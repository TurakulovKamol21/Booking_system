package com.autoguide.backend.repository.r2dbc;

import com.autoguide.backend.model.GuestEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface GuestRepository extends ReactiveCrudRepository<GuestEntity, UUID> {
}
