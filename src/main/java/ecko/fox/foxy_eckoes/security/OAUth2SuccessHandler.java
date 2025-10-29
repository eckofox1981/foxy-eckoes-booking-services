package ecko.fox.foxy_eckoes.security;

import ecko.fox.foxy_eckoes.user.User;
import ecko.fox.foxy_eckoes.user.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class OAUth2SuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oAuth2Token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oAuth2Token.getPrincipal();

        Optional<User> optUser = userRepository.findByOpenId(oAuth2User.getName());

        if (optUser.isEmpty()) {
            String username = "";

            /*
              since Google and GitHub do not have the same attributes the following if statements gets the username
              based on the authorized client registration ID extracted from the authentication-token.
             */
            if (oAuth2Token.getAuthorizedClientRegistrationId().equals("github")) {
                username = oAuth2User.getAttribute("login");
            }

            if (oAuth2Token.getAuthorizedClientRegistrationId().equals("google")) {
                username = oAuth2User.getAttribute("email");
            }

            var user = new User(UUID.randomUUID(), username, oAuth2User.getName(),
                    oAuth2Token.getAuthorizedClientRegistrationId());
            userRepository.save(user);
            System.out.println(user.getOpenIdProvider() + " - " + user.getUsername() + " saved as " + user.getUserID());
        } else {
            System.out.println(optUser.get().getOpenIdProvider() + " - " + optUser.get().getUsername()
                    + " logged in as " + optUser.get().getUserID());
        }
    }
}
