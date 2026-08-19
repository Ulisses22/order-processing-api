package dev.ulisses.highperformanceapi.application.service.impl;

import dev.ulisses.highperformanceapi.application.dto.request.LoginRequest;
import dev.ulisses.highperformanceapi.application.dto.response.LoginResponse;
import dev.ulisses.highperformanceapi.application.service.AccountLockoutService;
import dev.ulisses.highperformanceapi.application.service.AuthenticationService;
import dev.ulisses.highperformanceapi.application.service.RefreshTokenService;
import dev.ulisses.highperformanceapi.application.service.SecurityAuditService;
import dev.ulisses.highperformanceapi.domain.entity.User;
import dev.ulisses.highperformanceapi.domain.enums.AuditAction;
import dev.ulisses.highperformanceapi.domain.repository.UserRepository;
import dev.ulisses.highperformanceapi.infrastructure.config.JwtProperties;
import dev.ulisses.highperformanceapi.infrastructure.security.jwt.JwtService;
import dev.ulisses.highperformanceapi.infrastructure.security.user.CustomUserDetailsService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final String TOKEN_TYPE = "Bearer";
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final CustomUserDetailsService userDetailsService;
    private final AccountLockoutService accountLockoutService;
    private final SecurityAuditService securityAuditService;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public AuthenticationServiceImpl(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            JwtProperties jwtProperties,
            RefreshTokenService refreshTokenService,
            UserRepository userRepository,
            CustomUserDetailsService userDetailsService,
            AccountLockoutService accountLockoutService,
            SecurityAuditService securityAuditService) {

        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
        this.accountLockoutService = accountLockoutService;
        this.securityAuditService = securityAuditService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() ->
                        new BadCredentialsException("Invalid credentials.")
                );

        if (accountLockoutService.isLocked(user)) {
            throw new LockedException("Account is temporarily locked.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.username(),
                            request.password()
                    )
            );

            accountLockoutService.handleSuccessfulLogin(user);

            UserDetails userDetails =
                    (UserDetails) authentication.getPrincipal();

            String accessToken = jwtService.generateToken(userDetails);
            String refreshToken = refreshTokenService.create(user);

            securityAuditService.record(
                    AuditAction.LOGIN_SUCCESS,
                    user.getId(),
                    user.getUsername(),
                    "User logged in successfully."
            );

            return new LoginResponse(
                    accessToken,
                    refreshToken,
                    TOKEN_TYPE,
                    jwtProperties.expiration()
            );

        } catch (BadCredentialsException ex) {

            accountLockoutService.handleFailedLogin(request.username());

            securityAuditService.record(
                    AuditAction.LOGIN_FAILURE,
                    user.getId(),
                    user.getUsername(),
                    "Invalid credentials."
            );

            throw ex;
        }
    }

    @Override
    @Transactional
    public LoginResponse refresh(String rawRefreshToken) {

        User user = refreshTokenService.validate(rawRefreshToken);

        String newRefreshToken =
                refreshTokenService.rotate(rawRefreshToken);

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(user.getUsername());

        String accessToken =
                jwtService.generateToken(userDetails);

        return new LoginResponse(
                accessToken,
                newRefreshToken,
                TOKEN_TYPE,
                jwtProperties.expiration()
        );
    }
}
