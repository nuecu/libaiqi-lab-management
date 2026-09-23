package org.example.libaiqi2023021108.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS 配置。
 * 修复点：原配置同时使用 addAllowedOriginPattern("*") 与 setAllowCredentials(true)，
 * 会把任意来源（如 evil.example.com）反射回 Access-Control-Allow-Origin 且允许携带凭据，
 * 构成跨域凭据泄露。改为显式白名单：仅允许本机前端来源携带凭据访问。
 * 通过 SecurityConfig 的 .cors(Customizer.withDefaults()) 生效，并自动放行预检 OPTIONS。
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 显式允许的 origins（Vite 开发服务器 + 后端直连），不再使用通配 + 凭据的危险组合
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "http://localhost:8080",
                "http://127.0.0.1:8080"
        ));
        config.setAllowCredentials(true);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
