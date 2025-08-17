package com.provaweb.jogosinternos.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000") // Permite apenas o frontend React
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH") // Todos os métodos necessários
                .allowedHeaders("*") // Todos os headers
                .allowCredentials(true) // Permite credenciais (cookies, auth)
                .maxAge(3600); // Cache de configurações CORS por 1 hora
    }
}
