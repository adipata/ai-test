package com.example.certalert.auth;

import com.example.certalert.config.JwtProperties;
import com.example.certalert.user.Role;
import com.example.certalert.user.User;
import com.example.certalert.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Issues JWTs for locally-defined users. Designed as a dev fallback so the test is runnable
 * without an external IdP — the resource server side (jwtDecoder) validates these the same way
 * it would validate tokens from e.g. Keycloak.
 */
@Service
public class TokenService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;

    public TokenService(UserRepository users,
                        PasswordEncoder passwordEncoder,
                        JwtEncoder jwtEncoder,
                        JwtProperties properties) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
    }

    public String issue(String username, String rawPassword) {
        User user = users.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("unknown user"));
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BadCredentialsException("invalid password");
        }
        Instant now = Instant.now();
        List<String> roles = user.getRoles().stream().map(Role::name).toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .issuedAt(now)
                .expiresAt(now.plus(properties.ttlMinutes(), ChronoUnit.MINUTES))
                .subject(user.getUsername())
                .claim("group", user.getGroup())
                .claim("roles", roles)
                .build();

        JwsHeader header = JwsHeader.with(() -> "HS256").build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public static class BadCredentialsException extends RuntimeException {
        public BadCredentialsException(String message) { super(message); }
    }
}
