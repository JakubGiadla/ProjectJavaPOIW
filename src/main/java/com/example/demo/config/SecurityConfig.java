package com.example.demo.config;

import com.example.demo.service.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // DODANO: Import dla metod HTTP
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

// Wskazujemy Spring Bootowi, że jest to plik z ustawieniami, który zostanie wczytany priorytetowo podczas uruchamiania aplikacji
@Configuration
// Włączenie zabezpieczeń sieciowych, które są przez nas definiowane
@EnableWebSecurity
public class SecurityConfig {

    private final JwtService jwtService;

    // Konstruktor wstrzykujący JwtService – dzięki temu rozwiązujemy problem podświetlenia na czerwono w IntelliJ
    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    // Definicja algorytmu szyfrowania haseł użytkowników
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Definicja naszego głównego łańcucha filtrów bezpieczeństwa
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Włączamy obsługę zasad CORS zdefiniowanych w bean na dole pliku
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Wyłączamy csrf, ponieważ w naszej aplikacji wykorzystujemy API bezstanowe z JWT
                .csrf(csrf -> csrf.disable())
                // Definiujemy zasady dla zapytań HTTP
                .authorizeHttpRequests(auth -> auth
                        // POPRAWKA: Puszczamy wolno każde zapytanie testowe OPTIONS (CORS) z przeglądarki internetowej
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Rejestracja i logowanie są ogólnodostępne
                        .requestMatchers("/api/auth/**").permitAll()
                        // Każde inne zapytanie (w tym /api/events, /api/expenses) wymaga poprawnego tokenu JWT
                        .anyRequest().authenticated()
                )
                // REJESTRACJA FILTRA JWT: Wpinamy nasz autorski filtr przed standardowym filtrem logowania formularzem
                .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Pozwalamy na bezproblemowy ruch z Twojego frontendu w React
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        // Pozwalamy na wszystkie standardowe metody HTTP używane w projekcie
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Akceptujemy wszystkie nagłówki wysyłane przez przeglądarkę (w tym kluczowy Authorization)
        configuration.setAllowedHeaders(List.of("*"));
        // Pozwalamy na przesyłanie poświadczeń i nagłówków autoryzacji
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Stosujemy tę konfigurację CORS globalnie do całego API serwera
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}