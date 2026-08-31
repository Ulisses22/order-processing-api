package dev.ulisses.highperformanceapi.application.service;

import dev.ulisses.highperformanceapi.application.dto.request.LoginRequest;
import dev.ulisses.highperformanceapi.application.dto.response.LoginResponse;

public interface AuthenticationService {

    LoginResponse login(LoginRequest request);

    LoginResponse refresh(String refreshToken);

}
