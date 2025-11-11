package ecko.fox.foxy_eckoes.security;

import ecko.fox.foxy_eckoes.user.User;
import ecko.fox.foxy_eckoes.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Component
public class JWTFilter extends OncePerRequestFilter {
    private final JWTService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {


        if (request.getHeader("Authorization") == null || request.getHeader("Authorization").isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        UUID userID;
        try {
            userID = jwtService.verifyToken(request.getHeader("Authorization"));
        } catch (Exception e) {
            response.sendError(401, "Invalid token: " + e.getMessage());
            return;
        }

        User user = userRepository.findById(userID).get();

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                user, user.getPasswordHash(), user.getAuthorities()
        ));
        filterChain.doFilter(request, response);

    }
}
