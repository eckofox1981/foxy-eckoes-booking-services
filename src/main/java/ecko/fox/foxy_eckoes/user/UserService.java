package ecko.fox.foxy_eckoes.user;

import ecko.fox.foxy_eckoes.security.JWTService;
import ecko.fox.foxy_eckoes.security.PasswordConfig;
import ecko.fox.foxy_eckoes.user.dto.CreateDTO;
import ecko.fox.foxy_eckoes.user.dto.LoginDTO;
import ecko.fox.foxy_eckoes.user.dto.UserDTO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

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
        System.out.println("login: user: " + user.getUsername() + user.getAuthorities());
        return jwtService.generateToken(user.getUserID());
    }

    public User getUserInfo(User user) throws NoSuchElementException {
        //fetches fresh database user to allow fresh booking state
        Optional<User> userResponse = repository.findById(user.getUserID());
        if (!userResponse.isPresent()) {
            throw new NoSuchElementException("User not found");
        }
        return userResponse.get();
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

    public String deleteUser(User user) {
        repository.delete(user);
        return "Account deleted.";
    }

    public HashMap<String, String> getAllUserIDAndUserName() {
        List<User> allUsers = repository.findAll();
        HashMap<String, String> userList = new HashMap<>();

        for (User user : allUsers) {
            userList.put(user.getUserID().toString(), user.getUsername());
        }

        return userList;
    }

    public User getUserById(UUID userId) throws NoSuchElementException {
        return repository.findById(userId).orElseThrow(() -> new NoSuchElementException("ID: " + userId+ " not found"));
    }

    private boolean passwordValidation(String password, String passwordConfirm) {
        if (!password.equals(passwordConfirm)) {
            return false;
        }
        return (password.length() > 5 && password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z0-9]+$"));
        //lower and uppercase, digit, more than 5 char
    }

    //FOLLOWING ADDED ONLY TO MAKE CORRECTION PROCESS EASIER

    /**
     * creates an ADMIN user and a normal user and saves them to database upon first launch.
     * WILL PRINT THEIR PASSWORD OBVIOUSLY NOT FOR PRODUCTION
     */
    @PostConstruct
    public void createAdminAndUserIfneeded() {
        List<User> userList = repository.findAll();

        try {
            //if list completely empty assumed to be first time application is running
            if (userList.isEmpty()) {
                System.out.println(lines + "\nUser List empty: creating filler users...");
                User admin = new User(
                        UUID.randomUUID(),
                        "admin",
                        passwordConfig.passwordEncoder().encode("Test123"),
                        "Bob",
                        "Bobson",
                        "bob.bobson@foxy-echoes.com",
                        "admin");
                User user = new User(
                        UUID.randomUUID(),
                        "charlie",
                        passwordConfig.passwordEncoder().encode("Test123"),
                        "Charlie",
                        "Seinfeld",
                        "ilovemusic@gmail.com",
                        "user");
                repository.save(admin);
                repository.save(user);
                System.out.println("Users: \"admin\" and \"charlie\" created. Passwords: \"Test123\"\n" + lines);
            } else {
                System.out.println(lines + "\nDatabase used before, no user created.\n" + lines);
            }
        } catch (Exception e) {

        }
    }

    private String lines = "==============================================================";

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}
