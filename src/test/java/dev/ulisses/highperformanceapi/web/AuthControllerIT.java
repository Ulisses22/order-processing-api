package dev.ulisses.highperformanceapi.web;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String USERNAME = "user";
    private static final String PASSWORD = "60e90589-1976-4888-94b4-669c8b652732";

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

    // HELPER
    private String authenticate() throws Exception {

        LoginRequest request = new LoginRequest(
                USERNAME,
                PASSWORD
        );

        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response)
                .get("accessToken")
                .asText();
    }

}
