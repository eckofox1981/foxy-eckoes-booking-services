package ecko.fox.foxy_eckoes.user;

import ecko.fox.foxy_eckoes.booking.Booking;
import ecko.fox.foxy_eckoes.security.JWTService;
import ecko.fox.foxy_eckoes.security.PasswordConfig;
import ecko.fox.foxy_eckoes.user.dto.CreateDTO;
import ecko.fox.foxy_eckoes.user.dto.LoginDTO;
import ecko.fox.foxy_eckoes.user.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository repository;
    private final PasswordConfig passwordConfig;
    private final JWTService jwtService;


    public User createUser(CreateDTO userCreated) throws IllegalArgumentException {
        if (!passwordValidation(userCreated.getPassword(), userCreated.getPasswordConfirm())) {
            throw new IllegalArgumentException("Passwords do not match or chosen password is ineligible");
        }

        User user = new User(UUID.randomUUID(),
                userCreated.getUsername(),
                passwordConfig.passwordEncoder().encode(userCreated.getPassword()),
                userCreated.getFirstName(),
                userCreated.getLastName(),
                userCreated.getEmail(),
                "user");

        return repository.save(user);
    }

    public String login(LoginDTO loginDTO) throws NoSuchElementException {
        User user = repository
                .findByUsername(loginDTO.getUsername())
                .orElseThrow(()-> new NoSuchElementException("unable to login, please check username and password"));

        if (!passwordConfig.passwordEncoder().matches(loginDTO.getPassword(), user.getPasswordHash())) {
            throw new NoSuchElementException("unable to login, please check username and password");
        }

        return jwtService.generateToken(user.getUserID());
    }

    public User getUserInfo(User user) {
        return user;
    }

    public User updateUserInfo(User user, UserDTO userDTO) throws IllegalArgumentException {
        Optional<User> userNameExist = repository.findByUsername(userDTO.getUsername());

        if (userNameExist.isPresent() && !user.getUsername().equals(userDTO.getUsername())) {
            throw new IllegalArgumentException("Username unavailable");
        }

        user.setUsername(userDTO.getUsername());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());

        return repository.save(user);
    }

    private boolean passwordValidation(String password, String passwordConfirm) {
        if (!password.equals(passwordConfirm)) {
            return false;
        }
        return (password.length() > 5 && password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z0-9]+$"));
        //lower and uppercase, digit, more than 5 char
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}
