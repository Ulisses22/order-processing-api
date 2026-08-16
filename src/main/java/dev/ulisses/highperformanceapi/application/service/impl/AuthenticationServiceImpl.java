package dev.ulisses.highperformanceapi.application.service.impl;

import dev.ulisses.highperformanceapi.application.dto.request.LoginRequest;
import dev.ulisses.highperformanceapi.application.dto.response.LoginResponse;
import dev.ulisses.highperformanceapi.application.service.AuthenticationService;
import dev.ulisses.highperformanceapi.infrastructure.config.JwtProperties;
import dev.ulisses.highperformanceapi.infrastructure.security.jwt.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final String TOKEN_TYPE = "Bearer";

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public AuthenticationServiceImpl(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            JwtProperties jwtProperties) {

        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        request.username(),
                        request.password()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String accessToken = jwtService.generateToken(userDetails);

        return new LoginResponse(
                accessToken,
                TOKEN_TYPE,
                jwtProperties.expiration()
        );
    }
}
