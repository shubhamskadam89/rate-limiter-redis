package com.shubham.flashsale.idempotency.filter;

import com.shubham.flashsale.common.service.MetricsService;
import com.shubham.flashsale.common.web.CachedBodyHttpServletResponse;
import com.shubham.flashsale.idempotency.IdempotencyRecord;
import com.shubham.flashsale.idempotency.IdempotencyService;
import com.shubham.flashsale.idempotency.IdempotencyState;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(2)
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final String IDEMPOTENCY_HEADER = "X-Idempotency-Key";
    private  final IdempotencyService idempotencyService;
    private final MetricsService metricsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String key = request.getHeader(IDEMPOTENCY_HEADER);

        if (key == null || key.isBlank()) {

            log.warn("Missing X-Idempotency-Key header for URI={}", request.getRequestURI());
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Missing X-Idempotency-Key header"
            );

            return;
        }
        String scopedKey = buildScopedKey(key);
        Optional<IdempotencyRecord> existing = idempotencyService.get(scopedKey);

        if (existing.isPresent()) {
            metricsService.incrementIdempotencyHit();
            IdempotencyRecord record = existing.get();

            if (record.getState() == IdempotencyState.COMPLETED) {

                log.info("Idempotency cache hit (COMPLETED) for scopedKey={}", scopedKey);
                response.setStatus(record.getStatusCode());
                response.setContentType("application/json");
                response.getWriter().write(record.getResponseBody());

                return;
            }

            if (record.getState() == IdempotencyState.PROCESSING) {

                log.info("Idempotency cache hit (PROCESSING) for scopedKey={}", scopedKey);
                response.setStatus(HttpServletResponse.SC_ACCEPTED);
                response.getWriter().write("Request already being processed.");

                return;
            }
        }

        boolean acquired = idempotencyService.tryAcquire(scopedKey);

        if (!acquired) {

            log.warn("Failed to acquire idempotency lock for scopedKey={}", scopedKey);
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            response.getWriter().write("Request already being processed.");

            return;
        }

        CachedBodyHttpServletResponse cachedResponse =
                new CachedBodyHttpServletResponse(response);

        try {
            filterChain.doFilter(request, cachedResponse);

            int statusCode = cachedResponse.getStatus();
            String responseBody = cachedResponse.getCachedBody();

            if (statusCode >= HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
                idempotencyService.release(scopedKey);
            } else {
                idempotencyService.complete(
                        scopedKey,
                        responseBody,
                        statusCode
                );
            }

            cachedResponse.copyBodyToResponse();

        } catch (IOException | ServletException exception) {
            idempotencyService.release(scopedKey);
            throw exception;

        } catch (RuntimeException exception) {
            idempotencyService.release(scopedKey);
            throw exception;
        }

    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        return !(
                request.getMethod().equals("POST")
                        &&
                        request.getRequestURI().matches("^/api/v1/sales/.*/purchase$")
        );

    }

    private String buildScopedKey(String idempotencyKey) {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AccessDeniedException("Authenticated user not found");
        }

        String userUuid = jwt.getSubject();

        if (userUuid == null || userUuid.isBlank()) {
            throw new AccessDeniedException(
                    "Authenticated user UUID is missing from token"
            );
        }

        return userUuid + ":purchase:" + idempotencyKey;
    }


}