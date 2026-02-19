package com.autoguide.backend.service;

import com.autoguide.backend.exception.NotFoundException;
import com.autoguide.backend.model.HotelEntity;
import com.autoguide.backend.repository.r2dbc.HotelRepository;
import com.autoguide.backend.repository.r2dbc.HotelUserScopeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HotelAccessService {

    private final boolean securityEnabled;
    private final HotelRepository hotelRepository;
    private final HotelUserScopeRepository hotelUserScopeRepository;

    public HotelAccessService(
            @Value("${app.security.enabled:true}") boolean securityEnabled,
            HotelRepository hotelRepository,
            HotelUserScopeRepository hotelUserScopeRepository
    ) {
        this.securityEnabled = securityEnabled;
        this.hotelRepository = hotelRepository;
        this.hotelUserScopeRepository = hotelUserScopeRepository;
    }

    public Mono<AccessScope> currentScope() {
        if (!securityEnabled) {
            return Mono.just(AccessScope.unrestricted());
        }

        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required")))
                .flatMap(this::fromAuthentication);
    }

    public Mono<UUID> resolveHotelForWrite(AccessScope scope, UUID requestedHotelId) {
        if (scope.superAdmin()) {
            return resolveRequestedOrDefaultHotel(requestedHotelId);
        }
        return Mono.just(scope.requiredHotelId());
    }

    public Mono<Void> assertCanAccessHotel(AccessScope scope, UUID hotelId) {
        if (scope.superAdmin() || scope.matchesHotel(hotelId)) {
            return Mono.empty();
        }
        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied for hotel: " + hotelId));
    }

    public Mono<UUID> ensureHotelExists(UUID hotelId) {
        return hotelRepository.existsById(hotelId)
                .flatMap(exists -> exists
                        ? Mono.just(hotelId)
                        : Mono.error(new NotFoundException("Hotel not found: " + hotelId)));
    }

    private Mono<AccessScope> fromAuthentication(Authentication authentication) {
        String username = normalizeUsername(authentication.getName());
        if (username.isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated username is empty"));
        }

        Set<String> authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        if (authorities.contains("ROLE_SUPER_ADMIN")) {
            return Mono.just(AccessScope.superAdmin(username));
        }

        return hotelUserScopeRepository.findById(username)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "User is not assigned to any hotel: " + username
                )))
                .map(scope -> AccessScope.scoped(username, scope.getHotelId()));
    }

    private Mono<UUID> resolveRequestedOrDefaultHotel(UUID requestedHotelId) {
        if (requestedHotelId != null) {
            return ensureHotelExists(requestedHotelId);
        }

        return hotelRepository.findAllByOrderByNameAsc()
                .next()
                .map(HotelEntity::getId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "hotelId is required because no hotels are configured"
                )));
    }

    private String normalizeUsername(String username) {
        if (username == null) {
            return "";
        }
        return username.trim().toLowerCase(Locale.ROOT);
    }

    public record AccessScope(String username, UUID hotelId, boolean superAdmin) {

        static AccessScope unrestricted() {
            return new AccessScope("system", null, true);
        }

        static AccessScope superAdmin(String username) {
            return new AccessScope(username, null, true);
        }

        static AccessScope scoped(String username, UUID hotelId) {
            return new AccessScope(username, hotelId, false);
        }

        UUID requiredHotelId() {
            if (hotelId == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No hotel scope assigned for user: " + username);
            }
            return hotelId;
        }

        boolean matchesHotel(UUID requestedHotelId) {
            return hotelId != null && hotelId.equals(requestedHotelId);
        }
    }
}
