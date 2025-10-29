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

        Authentication potentialOAUth2 = SecurityContextHolder.getContext().getAuthentication();

        if (potentialOAUth2 != null) {
            if (potentialOAUth2 instanceof OAuth2AuthenticationToken oAuth2AuthenticationToken) {
                OAuth2User oAuth2User = oAuth2AuthenticationToken.getPrincipal();

                Optional<User> optUser = userRepository.findByOpenId(oAuth2User.getName());

                if (optUser.isEmpty()) {
                    response.sendError(404, "User not found, check your token validity");
                    return;
                }

                User user = optUser.get();

                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                        user, user.getPasswordHash(), user.getAuthorities()
                ));
                filterChain.doFilter(request, response);
                return;
            }
        }

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
