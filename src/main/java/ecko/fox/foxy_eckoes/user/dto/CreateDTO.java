package ecko.fox.foxy_eckoes.user.dto;

import ecko.fox.foxy_eckoes.booking.BookingDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CreateDTO {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String passwordConfirm;
}
