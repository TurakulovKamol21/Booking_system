package com.autoguide.backend.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String REALM_ACCESS = "realm_access";
    private static final String ROLES = "roles";

    @Override
    public Collection<GrantedAuthority> convert(Jwt source) {
        Object realmAccess = source.getClaims().get(REALM_ACCESS);
        if (!(realmAccess instanceof Map<?, ?> realmMap)) {
            return Collections.emptySet();
        }

        Object rolesObj = realmMap.get(ROLES);
        if (!(rolesObj instanceof Collection<?> roles)) {
            return Collections.emptySet();
        }

        return roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(role -> "ROLE_" + role.toUpperCase())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
}
