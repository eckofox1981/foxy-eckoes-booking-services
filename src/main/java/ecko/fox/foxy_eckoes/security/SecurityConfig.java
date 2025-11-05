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
                                           OAUth2SuccessHandler oaUth2SuccessHandler,
                                           UserRepository userRepository) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers(HttpMethod.POST, "/user/create").permitAll()
                                .requestMatchers(HttpMethod.PUT, "/user/login").permitAll()
                                .requestMatchers(HttpMethod.POST, "/event/filter").permitAll()
                                .requestMatchers(HttpMethod.POST, "/event/create").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/event/cancel").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/event/update").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/booking/delete").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/event/control-all-event-availability").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.GET, "/event/*").permitAll()
                                .anyRequest().authenticated()
                )
                .oauth2Login(oAuth2 -> oAuth2.successHandler(oaUth2SuccessHandler))
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

    /**
     * sets up the CORS configuration source (through Spring) to allow requests from the 127 http address
     * a new corsconfiguration is created
     * only requests from the http are allowed
     * all methods are permited
     * and allowed headers allows all header requests
     * allow credentials allows cookies to be sent
     *
     * UrlBasedCorsConfigurationSource applies the rule set above
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // Alternative: Use setAllowedOriginPatterns for more flexibility
        corsConfiguration.setAllowedOriginPatterns(List.of("*" /*TODO: change for production*/));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setExposedHeaders(List.of("Authorization", "Content-Type"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return urlBasedCorsConfigurationSource;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
