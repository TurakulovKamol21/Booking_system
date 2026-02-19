package com.autoguide.backend.service;

import com.autoguide.backend.dto.CreateHotelRequest;
import com.autoguide.backend.dto.HotelResponse;
import com.autoguide.backend.dto.UpdateHotelRequest;
import com.autoguide.backend.exception.NotFoundException;
import com.autoguide.backend.repository.r2dbc.BookingRepository;
import com.autoguide.backend.repository.r2dbc.GuestRepository;
import com.autoguide.backend.repository.r2dbc.HotelRepository;
import com.autoguide.backend.repository.r2dbc.HotelUserScopeRepository;
import com.autoguide.backend.repository.r2dbc.RoomRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
public class HotelService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final BookingRepository bookingRepository;
    private final HotelUserScopeRepository hotelUserScopeRepository;
    private final HotelAccessService hotelAccessService;

    public HotelService(
            HotelRepository hotelRepository,
            RoomRepository roomRepository,
            GuestRepository guestRepository,
            BookingRepository bookingRepository,
            HotelUserScopeRepository hotelUserScopeRepository,
            HotelAccessService hotelAccessService
    ) {
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.guestRepository = guestRepository;
        this.bookingRepository = bookingRepository;
        this.hotelUserScopeRepository = hotelUserScopeRepository;
        this.hotelAccessService = hotelAccessService;
    }

    public Flux<HotelResponse> getAll() {
        return hotelAccessService.currentScope()
                .flatMapMany(scope -> {
                    if (scope.superAdmin()) {
                        return hotelRepository.findAllByOrderByNameAsc();
                    }
                    return hotelRepository.findById(scope.requiredHotelId()).flux();
                })
                .map(this::toResponse);
    }

    public Mono<HotelResponse> create(CreateHotelRequest request) {
        return requireSuperAdmin()
                .then(Mono.defer(() -> {
                    var entity = new com.autoguide.backend.model.HotelEntity();
                    entity.setCode(normalize(request.code()));
                    entity.setName(normalize(request.name()));
                    entity.setCity(normalize(request.city()));
                    entity.setCountry(normalize(request.country()));
                    entity.setAddressLine(normalize(request.addressLine()));
                    entity.setImageUrl(normalizeNullable(request.imageUrl()));
                    entity.setCreatedAt(Instant.now());
                    return hotelRepository.save(entity).map(this::toResponse);
                }));
    }

    public Mono<HotelResponse> update(UUID id, UpdateHotelRequest request) {
        return requireSuperAdmin()
                .then(hotelRepository.findById(id)
                        .switchIfEmpty(Mono.error(new NotFoundException("Hotel not found: " + id))))
                .flatMap(entity -> {
                    entity.setCode(normalize(request.code()));
                    entity.setName(normalize(request.name()));
                    entity.setCity(normalize(request.city()));
                    entity.setCountry(normalize(request.country()));
                    entity.setAddressLine(normalize(request.addressLine()));
                    entity.setImageUrl(normalizeNullable(request.imageUrl()));
                    return hotelRepository.save(entity).map(this::toResponse);
                });
    }

    public Mono<Void> delete(UUID id) {
        return requireSuperAdmin()
                .then(hotelRepository.findById(id)
                        .switchIfEmpty(Mono.error(new NotFoundException("Hotel not found: " + id))))
                .flatMap(entity -> assertNoDependentData(id).then(hotelRepository.delete(entity)));
    }

    private Mono<Void> requireSuperAdmin() {
        return hotelAccessService.currentScope()
                .flatMap(scope -> scope.superAdmin()
                        ? Mono.empty()
                        : Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Only super admin can manage hotels")));
    }

    private Mono<Void> assertNoDependentData(UUID hotelId) {
        return Mono.zip(
                        roomRepository.countByHotelId(hotelId),
                        guestRepository.countByHotelId(hotelId),
                        bookingRepository.countByHotelId(hotelId),
                        hotelUserScopeRepository.existsByHotelId(hotelId)
                )
                .flatMap(tuple -> {
                    long roomCount = tuple.getT1();
                    long guestCount = tuple.getT2();
                    long bookingCount = tuple.getT3();
                    boolean hasScopes = tuple.getT4();

                    if (roomCount == 0 && guestCount == 0 && bookingCount == 0 && !hasScopes) {
                        return Mono.empty();
                    }

                    String message = "Cannot delete hotel with related data. "
                            + "rooms=" + roomCount
                            + ", guests=" + guestCount
                            + ", bookings=" + bookingCount
                            + ", userScopes=" + (hasScopes ? 1 : 0);
                    return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, message));
                });
    }

    private HotelResponse toResponse(com.autoguide.backend.model.HotelEntity entity) {
        return new HotelResponse(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getCity(),
                entity.getCountry(),
                entity.getAddressLine(),
                entity.getImageUrl(),
                entity.getCreatedAt()
        );
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
