package com.example.certalert.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Thin wrapper around {@link SecurityContextHolder} to fetch the authenticated user's
 * identity and group without sprinkling JWT internals across controllers.
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    public static CurrentUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken token)) {
            throw new IllegalStateException("No authenticated JWT principal in context");
        }
        Jwt jwt = token.getToken();
        String username = jwt.getSubject();
        String group = jwt.getClaimAsString("group");
        if (group == null || group.isBlank()) {
            throw new IllegalStateException("JWT is missing the 'group' claim");
        }
        return new CurrentUser(username, group);
    }

    public record CurrentUser(String username, String group) {}
}
