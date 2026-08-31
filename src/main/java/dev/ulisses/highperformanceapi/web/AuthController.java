package dev.ulisses.highperformanceapi.web;

import dev.ulisses.highperformanceapi.application.dto.request.LoginRequest;
import dev.ulisses.highperformanceapi.application.dto.request.RefreshTokenRequest;
import dev.ulisses.highperformanceapi.application.dto.response.LoginResponse;
import dev.ulisses.highperformanceapi.application.service.AuthenticationService;
import dev.ulisses.highperformanceapi.application.service.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(
            AuthenticationService authenticationService,
            RefreshTokenService refreshTokenService)
    {
        this.authenticationService = authenticationService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authenticationService.login(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        return ResponseEntity.ok(
                authenticationService.refresh(request.refreshToken())
        );
    }

    @PostMapping("/revoke")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revoke(@RequestBody RefreshTokenRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }
}
