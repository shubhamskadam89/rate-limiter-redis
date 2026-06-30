package com.shubham.flashsale.idempotency.filter;

import com.shubham.flashsale.idempotency.IdempotencyRecord;
import com.shubham.flashsale.idempotency.IdempotencyService;
import com.shubham.flashsale.idempotency.IdempotencyState;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Order(2)
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final String IDEMPOTENCY_HEADER = "X-Idempotency-Key";
    private  final IdempotencyService idempotencyService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String key = request.getHeader(IDEMPOTENCY_HEADER);

        if (key == null || key.isBlank()) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Missing X-Idempotency-Key header"
            );

            return;
        }
        Optional<IdempotencyRecord> existing = idempotencyService.get(key);

        if (existing.isPresent()) {

            IdempotencyRecord record = existing.get();

            if (record.getState() == IdempotencyState.COMPLETED) {

                response.setStatus(record.getStatusCode());
                response.setContentType("application/json");
                response.getWriter().write(record.getResponseBody());

                return;
            }

            if (record.getState() == IdempotencyState.PROCESSING) {

                response.setStatus(HttpServletResponse.SC_ACCEPTED);
                response.getWriter().write("Request already being processed.");

                return;
            }
        }

        boolean acquired = idempotencyService.tryAcquire(key);

        if (!acquired) {

            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            response.getWriter().write("Request already being processed.");

            return;
        }

        filterChain.doFilter(request, response);

    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        return !(
                request.getMethod().equals("POST")
                        &&
                        request.getRequestURI().matches("^/api/v1/sales/.*/purchase$")
        );

    }
}