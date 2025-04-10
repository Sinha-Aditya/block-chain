package com.example.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class LoadBalancerDebugFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoadBalancerDebugFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Log before reactive load balancer processing
        URI url = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        if (url != null && "lb".equals(url.getScheme())) {
            logger.debug("LoadBalancer will process URL: {}", url);
            String serviceId = url.getHost();
            logger.debug("Will load balance service: {}", serviceId);
        }
        
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // Execute before ReactiveLoadBalancerClientFilter (10150)
        return 10100;
    }
} 