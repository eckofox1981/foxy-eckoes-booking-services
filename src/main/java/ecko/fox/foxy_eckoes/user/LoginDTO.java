package ecko.fox.foxy_eckoes.user;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class LoginDTO {
    private String username;
    private String password;
    private String passwordConfirm;
}
