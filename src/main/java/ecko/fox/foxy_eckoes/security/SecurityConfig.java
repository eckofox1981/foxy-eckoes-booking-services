package ecko.fox.foxy_eckoes.security;

import ecko.fox.foxy_eckoes.user.UserRepository;
import ecko.fox.foxy_eckoes.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity,
                                           JWTService jwtService,
                                           UserRepository userRepository) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers("/", "/index.html", "/static/**", "/css/**", "/js/**", "/assets/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "/user/create").permitAll()
                                .requestMatchers(HttpMethod.PUT, "/user/login").permitAll()
                                .requestMatchers(HttpMethod.POST, "/event/filter").permitAll()
                                .requestMatchers(HttpMethod.GET, "/user/get-all-usernames-and-id").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.GET, "/booking/get-bookings-by-userId").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.POST, "/event/create").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/event/cancel").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/event/update").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/booking/delete").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/event/control-all-event-availability").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.GET, "/event/*").permitAll()
                                .anyRequest().authenticated()
                )
                .addFilterAfter(new JWTFilter(jwtService, userRepository), OAuth2LoginAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return authenticationProvider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(List.of(
                "http://localhost",
                "http://127.0.0.1"
        ));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setExposedHeaders(List.of("Authorization", "Content-Type"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
