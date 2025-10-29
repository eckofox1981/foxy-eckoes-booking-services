package ecko.fox.foxy_eckoes.user;

import ecko.fox.foxy_eckoes.user.dto.LoginDTO;
import ecko.fox.foxy_eckoes.user.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/user")
public class UserController {
    private UserService service;

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody User user, @RequestBody String passwordConfirm) {
        try {
            return ResponseEntity.ok(service.createUser(user, passwordConfirm));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(406).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        try {
            return ResponseEntity.ok(service.login(loginDTO));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
