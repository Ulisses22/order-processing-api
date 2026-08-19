package dev.ulisses.highperformanceapi.infrastructure.security.ratelimit;

import dev.ulisses.highperformanceapi.application.service.SecurityAuditService;
import dev.ulisses.highperformanceapi.domain.enums.AuditAction;
import dev.ulisses.highperformanceapi.infrastructure.config.RateLimitProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/api/v1/auth/login";
    private static final String REFRESH_PATH = "/api/v1/auth/refresh";
    private static final String REVOKE_PATH = "/api/v1/auth/revoke";

    private final RateLimitProperties properties;
    private final SecurityAuditService securityAuditService;

    private final Map<String, Bucket> buckets =
            new ConcurrentHashMap<>();

    public RateLimitFilter(
            RateLimitProperties properties,
            SecurityAuditService securityAuditService) {

        this.properties = properties;
        this.securityAuditService = securityAuditService;
    }

    public void clearBuckets() {
        buckets.clear();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        if (!isRateLimitedEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);

        Bucket bucket = buckets.computeIfAbsent(
                clientIp,
                key -> createBucket()
        );

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
            return;
        }

        securityAuditService.record(
                AuditAction.RATE_LIMIT_EXCEEDED,
                null,
                clientIp,
                "Rate limit exceeded for " + request.getRequestURI() + "."
        );

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");

        response.getWriter().write("""
        {
            "status": 429,
            "error": "Too Many Requests",
            "message": "Rate limit exceeded."
        }
        """);
    }

    private Bucket createBucket() {

        Refill refill = Refill.greedy(
                properties.refillTokens(),
                Duration.ofSeconds(
                        properties.refillDurationSeconds()
                )
        );

        Bandwidth limit = Bandwidth.classic(
                properties.capacity(),
                refill
        );

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private boolean isRateLimitedEndpoint(
            HttpServletRequest request) {

        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }

        String path = request.getRequestURI();

        return LOGIN_PATH.equals(path)
                || REFRESH_PATH.equals(path)
                || REVOKE_PATH.equals(path);
    }

    private String getClientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}