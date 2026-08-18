package dev.ulisses.highperformanceapi.web;

import dev.ulisses.highperformanceapi.support.IntegrationTest;
import org.springframework.http.HttpHeaders;
import tools.jackson.databind.ObjectMapper;
import dev.ulisses.highperformanceapi.application.dto.request.LoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIT extends IntegrationTest {

    private String accessToken;

    @Test
    @DisplayName("Should login successfully")
    void shouldLoginSuccessfully() throws Exception {

        LoginRequest request = new LoginRequest(
                USERNAME,
                PASSWORD
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600000));
    }

    @Test
    @DisplayName("Should return 401 when password is invalid")
    void shouldReturn401WhenPasswordIsInvalid() throws Exception {

        LoginRequest request = new LoginRequest(
                USERNAME,
                "wrong-password"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 when username does not exist")
    void shouldReturn401WhenUsernameDoesNotExist() throws Exception {

        LoginRequest request = new LoginRequest(
                "unknown-user",
                PASSWORD
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 when accessing protected endpoint without JWT")
    void shouldReturn401WhenAccessingProtectedEndpointWithoutToken() throws Exception {

        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should access protected endpoint with JWT")
    void shouldAccessProtectedEndpointWithJwt() throws Exception {

        String token = authenticate();

        mockMvc.perform(get("/api/v1/customers")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should login and return refresh token")
    void shouldLoginAndReturnRefreshToken() throws Exception {

        LoginRequest request = new LoginRequest(
                USERNAME,
                PASSWORD
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("Should refresh access token successfully")
    void shouldRefreshAccessTokenSuccessfully() throws Exception {

        LoginRequest loginRequest = new LoginRequest(USERNAME, PASSWORD);

        String loginResponse = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken = objectMapper
                .readTree(loginResponse)
                .get("refreshToken")
                .asString();

        mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "refreshToken": "%s"
                                    }
                                    """.formatted(refreshToken))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600000));
    }

    @Test
    @DisplayName("Should rotate refresh token")
    void shouldRotateRefreshToken() throws Exception {

        LoginRequest loginRequest =
                new LoginRequest(USERNAME, PASSWORD);

        String loginResponse = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String oldRefreshToken = objectMapper
                .readTree(loginResponse)
                .get("refreshToken")
                .asString();

        String refreshResponse = mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "refreshToken": "%s"
                                    }
                                    """.formatted(oldRefreshToken))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String newRefreshToken = objectMapper
                .readTree(refreshResponse)
                .get("refreshToken")
                .asString();

        assertNotEquals(oldRefreshToken, newRefreshToken);
    }

    @Test
    @DisplayName("Should reject old refresh token after rotation")
    void shouldRejectOldRefreshTokenAfterRotation() throws Exception {

        LoginRequest loginRequest =
                new LoginRequest(USERNAME, PASSWORD);

        String loginResponse = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String oldRefreshToken = objectMapper
                .readTree(loginResponse)
                .get("refreshToken")
                .asString();

        // Rotate the token
        mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "refreshToken": "%s"
                                    }
                                    """.formatted(oldRefreshToken))
                )
                .andExpect(status().isOk());

        // The old token must no longer work
        mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "refreshToken": "%s"
                                    }
                                    """.formatted(oldRefreshToken))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should revoke refresh token")
    void shouldRevokeRefreshToken() throws Exception {

        LoginRequest loginRequest =
                new LoginRequest(USERNAME, PASSWORD);

        String loginResponse = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken = objectMapper
                .readTree(loginResponse)
                .get("refreshToken")
                .asString();

        mockMvc.perform(
                        post("/api/v1/auth/revoke")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "refreshToken": "%s"
                                    }
                                    """.formatted(refreshToken))
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should reject revoked refresh token")
    void shouldRejectRevokedRefreshToken() throws Exception {

        LoginRequest loginRequest =
                new LoginRequest(USERNAME, PASSWORD);

        String loginResponse = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken = objectMapper
                .readTree(loginResponse)
                .get("refreshToken")
                .asString();

        // Revoke the token
        mockMvc.perform(
                        post("/api/v1/auth/revoke")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "refreshToken": "%s"
                                    }
                                    """.formatted(refreshToken))
                )
                .andExpect(status().isNoContent());

        // The revoked token must no longer work
        mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "refreshToken": "%s"
                                    }
                                    """.formatted(refreshToken))
                )
                .andExpect(status().isUnauthorized());
    }

    // HELPER
    protected String authenticate() throws Exception {

        if (accessToken != null) {
            return accessToken;
        }

        LoginRequest request = new LoginRequest(USERNAME, PASSWORD);

        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        accessToken = objectMapper.readTree(response)
                .get("accessToken")
                .asString();

        return accessToken;
    }

}
