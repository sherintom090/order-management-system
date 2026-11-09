package com.retail.platform.inventory.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Correlation-ID";
    public static final String MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String incomingId = request.getHeader(HEADER);

        String correlationId = isValid(incomingId)
                ? incomingId
                : UUID.randomUUID().toString();

        response.setHeader(HEADER, correlationId);
        request.setAttribute(MDC_KEY, correlationId);

        try (MDC.MDCCloseable ignored =
                     MDC.putCloseable(MDC_KEY, correlationId)) {

            filterChain.doFilter(request, response);
        }
    }

    private boolean isValid(String value) {
        return value != null
                && value.length() <= 128
                && value.matches("[a-zA-Z0-9._-]+");
    }
}