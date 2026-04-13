package com.api.apigateway.security;

import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    private static final List<String> OPEN_API_ENDPOINTS = List.of(
            "/student/auth/**", "/student/createNewUser", "/staff/admin/login", "/staff/admin/register", "/staff/admin/forget-password"
            // You can also use patterns like "/student/auth/**"
    );

    private boolean isOpenEndpoint(String path) {
        return  path.startsWith("/student/auth/login") ||
                path.startsWith("/student/createNewUser") ||
                path.startsWith("/staff/admin/login") ||
                path.startsWith("/staff/admin/register") ||
                path.startsWith("/staff/admin/forget-password");
    }



    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        System.out.println("👉 PATH: " + path);

        if (isOpenEndpoint(path)) {
            System.out.println("✅ OPEN API - No Token Required");
            return chain.filter(exchange); // ✅ CORRECT
        }


            // ✅ Step 2: Get Authorization Header
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("❌ Missing or Invalid Header");
            return writeError(exchange, HttpStatus.UNAUTHORIZED, "Missing Authorization Header");
        }

        String token = authHeader.substring(7);
        System.out.println("token = " + token);
        try {
            Claims claims = jwtUtil.validateToken(token);
            System.out.println("✅ TOKEN VALID");

////             ✅ Step 3: Role-based Authorization
//            if (!jwtUtil.isRequestAuthorized(path, claims)) {
//                System.out.println("❌ ROLE NOT MATCHED");
//                return writeError(exchange, HttpStatus.FORBIDDEN, "Access Denied");
//            }

        } catch (Exception e) {
            e.printStackTrace();
            return writeError(exchange, HttpStatus.UNAUTHORIZED, "Invalid Token");
        }

        // ✅ Step 4: Continue filter chain
        return chain.filter(
                exchange.mutate()
                        .request(exchange.getRequest()
                                .mutate()
                                .header("Authorization", authHeader)
                                .build())
                        .build()
        );
    }


    private Mono<Void> writeError(ServerWebExchange exchange, HttpStatus status, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] payload = ("{\"error\":\"" + message + "\"}")
                .getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse()
                .writeWith(Flux.just(exchange.getResponse().bufferFactory().wrap(payload)));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}