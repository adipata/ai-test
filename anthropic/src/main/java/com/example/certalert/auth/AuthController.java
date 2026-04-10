package com.example.certalert.auth;

import com.example.certalert.security.SecurityUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final TokenService tokenService;

    public AuthController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/auth/token")
    public ResponseEntity<TokenResponse> token(@Valid @RequestBody TokenRequest request) {
        String jwt = tokenService.issue(request.username(), request.password());
        return ResponseEntity.ok(new TokenResponse(jwt, "Bearer"));
    }

    @GetMapping("/me")
    public MeResponse me() {
        SecurityUtils.CurrentUser user = SecurityUtils.currentUser();
        JwtAuthenticationToken auth =
                (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        List<String> roles = auth.getToken().getClaimAsStringList("roles");
        return new MeResponse(user.username(), user.group(), roles == null ? List.of() : roles);
    }

    public record TokenRequest(@NotBlank String username, @NotBlank String password) {}
    public record TokenResponse(String accessToken, String tokenType) {}
    public record MeResponse(String username, String group, List<String> roles) {}
}
