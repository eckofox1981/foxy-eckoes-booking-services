package ecko.fox.foxy_eckoes.user;

import ecko.fox.foxy_eckoes.user.dto.LoginDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.NoSuchElementException;
import java.util.Optional;

public class UserService implements UserDetailsService {
    private UserRepository repository;


    public User createUser(User user, String password) throws IllegalArgumentException {
        if (!passwordValidation(user.getPasswordHash(), password)) {
            throw new IllegalArgumentException("Passwords do not match or chosen password is ineligible");
        }

        return repository.save(user);
    }

    public String login(LoginDTO loginDTO) throws NoSuchElementException {
        User user = repository
                .findByUsername(loginDTO.getUsername())
                .orElseThrow(()-> new NoSuchElementException("unable to login, please check username and password"));

        //TODO: change to proper password check
        if (!loginDTO.getPassword().equals(user.getPasswordHash())) {
            throw new NoSuchElementException("unable to login, please check username and password");
        }
        //TODO: implement JWT
        return "jwtToken";
    }

    private boolean passwordValidation(String password, String passwordConfirm) {
        if (!password.equals(passwordConfirm)) {
            return false;
        }
        return (password.length() > 5 && password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z0-9]+$"));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}
