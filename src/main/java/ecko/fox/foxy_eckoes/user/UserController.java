package ecko.fox.foxy_eckoes.user;

import ecko.fox.foxy_eckoes.user.dto.CreateDTO;
import ecko.fox.foxy_eckoes.user.dto.LoginDTO;
import ecko.fox.foxy_eckoes.user.dto.UserDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    //TODO: remove
    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok("ping");
    }

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody CreateDTO userCreated) {
        try {
            User user = service.createUser(userCreated);
            return ResponseEntity.ok(UserDTO.fromUser(user));
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

    @Transactional
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal User user) {
        try {
            UserDTO userDTO = UserDTO.fromUser(service.getUserInfo(user));
            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUserInfo(@AuthenticationPrincipal User user, @RequestBody UserDTO userDTO) {
        try {
            UserDTO updatedUser = UserDTO.fromUser(service.updateUserInfo(user, userDTO));
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.status(204).body(service.deleteUser(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Deletion error: " + e.getMessage());
        }
    }
}
