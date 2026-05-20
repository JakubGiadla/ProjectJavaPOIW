package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// wskazujemy springbootowi że jest to plik z ustawieniami, który zostanie wczytany piorytetowa podczas uruchamiania aplikacji
@Configuration
// włączenie zabezpieczeń sieciowych, które są przez nas definiowane
@EnableWebSecurity
public class SecurityConfig {

    // Definicja algorytmu szyfrowania haseł
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Dedinicja naszego głównego filtru bezpieczeństwa
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // wyłączamy csrf, ponieważ w naszej aplikacji wykorzystujemy API z
                // JWT więc nie potrzebujemy tego zabezpieczneia
                .csrf(csrf -> csrf.disable())
                //zasady dla zapytań HTTP
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}