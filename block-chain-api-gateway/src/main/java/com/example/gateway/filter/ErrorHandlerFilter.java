package com.example.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ErrorHandlerFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(ErrorHandlerFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange)
                .onErrorResume(throwable -> {
                    logger.error("Error during request processing: {}", throwable.getMessage());
                    
                    String path = exchange.getRequest().getURI().getPath();
                    String serviceName = determineServiceName(path);
                    
                    // Set error response
                    exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                    
                    // Forward to appropriate fallback
                    String fallbackUri = "/fallback/" + serviceName;
                    return exchange.getResponse().setComplete();
                });
    }
    
    private String determineServiceName(String path) {
        if (path.startsWith("/auth")) {
            return "auth";
        } else if (path.startsWith("/documents")) {
            return "documents";
        } else if (path.startsWith("/analytics")) {
            return "analytics";
        } else if (path.startsWith("/audit")) {
            return "audit";
        } else if (path.startsWith("/blockchain")) {
            return "blockchain";
        }
        return "unknown";
    }

    @Override
    public int getOrder() {
        return -1; // High priority but after JWT filter
    }
} 